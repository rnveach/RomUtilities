package com.github.rnveach;

import java.io.File;
import java.io.RandomAccessFile;

import com.github.rnveach.utils.Util;

public final class XaSectorInfo {

	private static final int BUFFER_LENGTH = 0x930;
	private static final byte[] BUFFER = new byte[BUFFER_LENGTH];

	private static final byte[] SYNC = new byte[] { //
			0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00 //
	};

	private static final int[] START_XA = new int[256];

	private XaSectorInfo() {
	}

	public static void main(String... arguments) throws Exception {
		if ((arguments == null) || (arguments.length < 1)) {
			throw new IllegalArgumentException("Missing CD file");
		}

		final File file = new File(arguments[0]);

		print(file);
	}

	private static void print(File file) throws Exception {
		for (int i = 0; i < START_XA.length; i++) {
			START_XA[i] = -1;
		}

		try (final RandomAccessFile reader = new RandomAccessFile(file, "r")) {
			final long cdLength = reader.length();
			int sectorNumber = 0;

			while (reader.getFilePointer() < cdLength) {
				reader.read(BUFFER);

				if (!hasSync()) {
					System.out.println("\tInvalid Sector");
					continue;
				}

				final int mode = Util.read8(BUFFER, 15);

				switch (mode) {
				case 0:
					// TODO
					break;
				case 1:
					// TODO
					break;
				case 2:
					printMode2(sectorNumber);
					break;
				default:
					System.err.println("Unknown mode: " + mode);
					break;
				}

				sectorNumber++;
			}

			for (int i = 0; i < START_XA.length; i++) {
				if (START_XA[i] != -1) {
					System.out.println("Audio - " + i + "|Sectors:" + START_XA[i] + "-" + sectorNumber);
				}
			}
		}
	}

	// https://github.com/libyal/libodraw/blob/main/documentation/Optical%20disc%20RAW%20format.asciidoc

	private static void printMode2(int sectorNumber) {
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

		final String MSF = Util.hexRaw(Util.read8(BUFFER, 12), 2) + ":" + Util.hexRaw(Util.read8(BUFFER, 13), 2) + ":"
				+ Util.hexRaw(Util.read8(BUFFER, 14), 2);
		boolean printSector = false;

		if ((form == 0) || (form == 1)) {
			// ignore form 1 for now
			printSector = true;
		} else {
			// ignore file number for now
			final int channel = Util.read8(BUFFER, 17);
			final int subMode = Util.read8(BUFFER, 18);

			if (hasBitMask(subMode, 4)) {
				// audio

				if (hasBitMask(subMode, 1) || hasBitMask(subMode, 0x80)) {
					// end of file/record

					if (START_XA[channel] == -1) {
						printSector = true;
					} else {
						System.out.println("Audio - " + channel + "|Sectors:" + START_XA[channel] + "-" + sectorNumber);

						START_XA[channel] = -1;

						boolean groupEnd = true;

						for (int i = 0; i < START_XA.length; i++) {
							if (START_XA[i] != -1) {
								groupEnd = false;
								break;
							}
						}

						if (groupEnd) {
							System.out.println("Audio Group End");
						}
					}
				} else if (START_XA[channel] == -1) {
					boolean groupStart = true;

					for (int i = 0; i < START_XA.length; i++) {
						if (START_XA[i] != -1) {
							groupStart = false;
							break;
						}
					}

					if (groupStart) {
						System.out.println("Audio Group Start - " + sectorNumber);
					}

					START_XA[channel] = sectorNumber;
				}
			} else {
				printSector = true;
			}
		}

		if (printSector) {
			System.out.println("Sector #" + sectorNumber + " (" + MSF + "), Mode 2 Form " + form);
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
