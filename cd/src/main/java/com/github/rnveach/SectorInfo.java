package com.github.rnveach;

import java.io.File;
import java.io.RandomAccessFile;

import com.github.rnveach.utils.Util;

public final class SectorInfo {

	private static final int BUFFER_LENGTH = 0x930;
	private static final byte[] BUFFER = new byte[BUFFER_LENGTH];

	private static final byte[] SYNC = new byte[] { //
			0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00 //
	};

	private SectorInfo() {
	}

	public static void main(String... arguments) throws Exception {
		if ((arguments == null) || (arguments.length < 1)) {
			throw new IllegalArgumentException("Missing CD file");
		}

		final File file = new File(arguments[0]);

		print(file);
	}

	private static void print(File file) throws Exception {
		try (final RandomAccessFile reader = new RandomAccessFile(file, "r")) {
			final long cdLength = reader.length();
			int sectorNumber = 0;

			while (reader.getFilePointer() < cdLength) {
				final long startPosition = reader.getFilePointer();

				reader.read(BUFFER);

				System.out.println("Sector #" + sectorNumber + " (" + Util.hex(startPosition) + ")");

				if (!hasSync()) {
					System.out.println("\tInvalid Sector");
					continue;
				}

				System.out.println("\tHeader");

				System.out.println("\t\tMinute: " + Util.hex(Util.read8(BUFFER, 12)));
				System.out.println("\t\tSecond: " + Util.hex(Util.read8(BUFFER, 13)));
				System.out.println("\t\tFrame: " + Util.hex(Util.read8(BUFFER, 14)));

				final int mode = Util.read8(BUFFER, 15);

				System.out.println("\t\tMode: " + mode);

				switch (mode) {
				case 0:
					// TODO: check if it is all 0
					break;
				case 1:
					// TODO: print 2048 byte user data, and 288 edc/ecc
					break;
				case 2:
					printMode2();
					break;
				default:
					System.err.println("Unknown mode: " + mode);
					break;
				}

				System.out.println();

				sectorNumber++;
			}
		}
	}

	// https://github.com/libyal/libodraw/blob/main/documentation/Optical%20disc%20RAW%20format.asciidoc

	private static void printMode2() {
		final int form;

		if ((BUFFER[16] == BUFFER[20]) && ((BUFFER[17] == BUFFER[21])) && (BUFFER[18] == BUFFER[22])
				&& (BUFFER[19] == BUFFER[23])) {
			if (hasBitMask(BUFFER[18], 0x20)) {
				form = 2;
			} else {
				form = 1;
			}
		} else {
			form = 0;
		}

		System.out.println("\t\tForm: " + form);
		System.out.println();

		if (form == 0) {
			System.out.println();
			System.out.println("\t\tUser Data (2336):");
			printHexTable("\t\t\t", 24, 2336);
		} else {
			System.out.println("\tSub Header");
			System.out.println("\t\tFile Number: " + Util.read8(BUFFER, 16));
			System.out.println("\t\tChannel Number: " + Util.read8(BUFFER, 17));

			final int subMode = Util.read8(BUFFER, 18);

			System.out.println("\t\tSub-Mode: " + Util.hex(subMode));

			if (hasBitMask(subMode, 1)) {
				System.out.println("\t\t\tEnd of Record");
			}
			if (hasBitMask(subMode, 2)) {
				System.out.println("\t\t\tVideo");
			}
			if (hasBitMask(subMode, 4)) {
				System.out.println("\t\t\tAudio");
			}
			if (hasBitMask(subMode, 8)) {
				System.out.println("\t\t\tData");
			}
			if (hasBitMask(subMode, 0x10)) {
				System.out.println("\t\t\tTrigger");
			}
			if (hasBitMask(subMode, 0x20)) {
				System.out.println("\t\t\tForm 2");
			}
			if (hasBitMask(subMode, 0x40)) {
				System.out.println("\t\t\tReal Time");
			}
			if (hasBitMask(subMode, 0x80)) {
				System.out.println("\t\t\tEnd of File");
			}

			final int encoding = Util.read8(BUFFER, 19);

			System.out.println("\t\tEncoding: " + Util.hex(encoding));

			switch (encoding) {
			case 0:
				System.out.println("\t\t\t16-bit ADPCM");
				break;
			case 1:
				System.out.println("\t\t\t8-bit ADPCM");
				break;
			case 2:
				System.out.println("\t\t\t12-bit ADPCM");
				break;
			case 3:
				System.out.println("\t\t\t16-bit ADPCM");
				break;
			default:
				System.out.println("\t\t\tReserved");
				break;
			}

			System.out.println("\t\tRepeat: " + Util.hex(Util.read32LE(BUFFER, 20)));

			if (form == 1) {
				System.out.println();
				System.out.println("\t\tUser Data (2048):");
				printHexTable("\t\t\t", 24, 2048);

				System.out.println();
				System.out.println("\t\tEDC:");
				printHexTable("\t\t\t", 2072, 4);

				System.out.println();
				System.out.println("\t\tEDC/ECC (276):");
				printHexTable("\t\t\t", 2076, 276);
			} else {
				System.out.println();
				System.out.println("\t\tUser Data (2324)");
				printHexTable("\t\t\t", 24, 2324);

				System.out.println();
				System.out.println("\t\tEDC:");
				printHexTable("\t\t\t", 2348, 4);
			}
		}
	}

	private static void printHexTable(String lineStart, int start, int length) {
		final int end = start + length;

		for (int i = start; i < end;) {
			System.out.print(lineStart);

			for (int j = 0; (j < 16) && (i < end); j++) {
				System.out.print(Util.hexRaw((BUFFER[i]) & 0xFF, 2) + " ");
				i++;
			}

			System.out.println();
		}

	}

	private static boolean hasSync() {
		for (int i = 0; i < 12; i++) {
			if (BUFFER[i] != SYNC[i]) {
				System.err.println("Sync mismatch, " + BUFFER[i] + " versus " + SYNC[i]);
				return false;
			}
		}

		return true;
	}

	private static boolean hasBitMask(int bits, int mask) {
		return (bits & mask) == mask;
	}

}
