package com.github.rnveach;

import java.io.File;
import java.io.RandomAccessFile;

import com.github.rnveach.sector.CD;
import com.github.rnveach.utils.Util;

public final class SectorInfo {

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
			final CD cd = new CD(reader);
			final long cdLength = reader.length();
			int sectorNumber = 0;

			while (reader.getFilePointer() < cdLength) {
				final long startPosition = reader.getFilePointer();

				cd.readSector();

				System.out.println("Sector #" + sectorNumber + " (" + Util.hex(startPosition) + ")");

				System.out.println("\tHeader");

				System.out.println("\t\tMinute: " + Util.hex(cd.getCurrentMinute()));
				System.out.println("\t\tSecond: " + Util.hex(cd.getCurrentSecond()));
				System.out.println("\t\tFrame: " + Util.hex(cd.getCurrentFrame()));

				System.out.println("\t\tMode: " + cd.getCurrentMode());

				switch (cd.getCurrentMode()) {
				case 0:
					// TODO: check if it is all 0
					break;
				case 1:
					// TODO: print 2048 byte user data, and 288 edc/ecc
					break;
				case 2:
					printMode2(cd);
					break;
				default:
					System.err.println("Unknown mode: " + cd.getCurrentMode());
					break;
				}

				System.out.println();

				sectorNumber++;
			}
		}
	}

	// https://github.com/libyal/libodraw/blob/main/documentation/Optical%20disc%20RAW%20format.asciidoc

	private static void printMode2(CD cd) {
		final int form = cd.getCurrentModeForm();

		System.out.println("\t\tForm: " + form);
		System.out.println();

		if (form == 0) {
			System.out.println();
			System.out.println("\t\tUser Data (2336):");
			printHexTable("\t\t\t", cd.getCurrentData());
		} else {
			final byte[] sh = cd.getCurrentSh();

			System.out.println("\tSub Header");
			System.out.println("\t\tFile Number: " + Util.read8(sh, 0));
			System.out.println("\t\tChannel Number: " + Util.read8(sh, 1));

			final int subMode = Util.read8(sh, 2);

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

			final int encoding = Util.read8(sh, 3);

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

			System.out.println("\t\tRepeat: " + Util.hex(Util.read32LE(sh, 4)));

			if (form == 1) {
				System.out.println();
				System.out.println("\t\tUser Data (2048):");
//				printHexTable("\t\t\t", cd.getCurrentData());

				System.out.println();
				System.out.println("\t\tEDC:");
//				printHexTable("\t\t\t", 2072, 4);

				System.out.println();
				System.out.println("\t\tEDC/ECC (276):");
//				printHexTable("\t\t\t", 2076, 276);
			} else {
				System.out.println();
				System.out.println("\t\tUser Data (2324)");
//				printHexTable("\t\t\t", cd.getCurrentData());

				System.out.println();
				System.out.println("\t\tEDC:");
//				printHexTable("\t\t\t", 2348, 4);
			}
		}
	}

	private static void printHexTable(String lineStart, byte[] data) {
		for (int i = 0; i < data.length;) {
			System.out.print(lineStart);

			for (int j = 0; (j < 16) && (i < data.length); j++) {
				System.out.print(Util.hexRaw((data[i]) & 0xFF, 2) + " ");
				i++;
			}

			System.out.println();
		}

	}

	private static boolean hasBitMask(int bits, int mask) {
		return (bits & mask) == mask;
	}

}
