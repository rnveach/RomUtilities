package com.github.rnveach.zones;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import com.github.rnveach.utils.Util;

public final class Exporter {

	private static final int BUFFER_LENGTH = 1024 * 10;
	private static final byte[] BUFFER = new byte[BUFFER_LENGTH];

	private static byte[] CLUT_DATA;
	private static byte[] IMAGE_DATA;

	private static int BPP;
	private static int COLORS_PER_CLUT;
	private static int NUMBER_OF_CLUTS;
	private static int WIDTH;
	private static int HEIGHT;

	private Exporter() {
	}

	public static void process(File file, List<TimZone> zones) throws FileNotFoundException, IOException {
		if (loadTim(file) && validate(zones)) {
			final File parent = file.getParentFile();
			final String exportFilePrefix = Util.getFileNameWithoutExtension(file);

			for (final TimZone zone : zones) {
				export(parent, exportFilePrefix, zone);
			}
		}
	}

	private static boolean loadTim(File file) throws FileNotFoundException, IOException {
		try (final RandomAccessFile reader = new RandomAccessFile(file, "r")) {
			readHeader(reader);

			if ((BPP == 8) || (BPP == 9)) {
				readClut(reader);
			}

			readImage(reader);
		}

		return true;
	}

	private static boolean validate(List<TimZone> zones) {
		for (final TimZone zone : zones) {
			if ((BPP == 8) || (BPP == 9)) {
				// has clut

				if ((zone.getClutNumber() == null) && (zone.getClutUri() == null)) {
					if ((NUMBER_OF_CLUTS > 1) || (NUMBER_OF_CLUTS == 0)) {
						System.err.println(
								"Zone " + zone.getName() + " specifies no CLUTs but the TIM has multiple CLUTs");
						return false;
					}
				} else if (zone.getClutNumber() != null) {
					if (zone.getClutNumber() >= NUMBER_OF_CLUTS) {
						System.err.println("Zone " + zone.getName() + " specifies the CLUT #" + zone.getClutNumber()
								+ " but the TIM only has " + NUMBER_OF_CLUTS + " CLUTs");
						return false;
					}
				}
			}

			// TODO: overlapping zones

			// TODO: missing gaps
		}

		return true;
	}

	private static void export(File parent, String exportFilePrefix, TimZone zone) {
		// TODO
	}

	private static void readHeader(RandomAccessFile reader) throws IOException {
		if (reader.read(BUFFER, 0, 8) != 8) {
			throw new IndexOutOfBoundsException("File too short for header");
		}

		if ((BUFFER[0] != 0x10) || (BUFFER[1] != 0) || (BUFFER[2] != 0) || (BUFFER[3] != 0)) {
			throw new IllegalStateException("Missing TIM header");
		}

		BPP = BUFFER[4];

		if ((BPP != 8) && (BPP != 9) && (BPP != 2) && (BPP != 3)) {
			throw new IllegalStateException("Unknown BPP: " + BPP);
		}

		if ((BUFFER[5] != 0) || (BUFFER[6] != 0) || (BUFFER[7] != 0)) {
			throw new IllegalStateException("Missing TIM header");
		}
	}

	private static void readClut(RandomAccessFile reader) throws IOException {
		if (reader.read(BUFFER, 0, 12) != 12) {
			throw new IndexOutOfBoundsException("File too short for clut header");
		}

		final int clutDataSize = Util.read32LE(BUFFER, 0) - 12;
		COLORS_PER_CLUT = Util.read16LE(BUFFER, 8);
		NUMBER_OF_CLUTS = Util.read16LE(BUFFER, 10);

		CLUT_DATA = new byte[clutDataSize];

		if (reader.read(CLUT_DATA) != clutDataSize) {
			throw new IndexOutOfBoundsException("File too short for clut data");
		}
	}

	private static void readImage(RandomAccessFile reader) throws IOException {
		if (reader.read(BUFFER, 0, 12) != 12) {
			throw new IndexOutOfBoundsException("File too short for image data");
		}

		final int imageDataSize = Util.read32LE(BUFFER, 0) - 12;
		WIDTH = getActualImageWidth(BPP, Util.read16LE(BUFFER, 8));
		HEIGHT = Util.read16LE(BUFFER, 10);

		IMAGE_DATA = new byte[imageDataSize];

		if (reader.read(IMAGE_DATA) != imageDataSize) {
			throw new IndexOutOfBoundsException("File too short for image data");
		}
	}

	private static int getActualImageWidth(int bpp, int encodedImageWidth) {
		switch (bpp) {
		case 8:
			return encodedImageWidth * 4;
		case 9:
			return encodedImageWidth * 2;
		case 2:
			return encodedImageWidth;
		case 3:
			return (int) (encodedImageWidth * 1.5);
		}

		throw new IllegalStateException("Unknown BPP: " + bpp);
	}
}
