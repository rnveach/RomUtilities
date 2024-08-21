package com.github.rnveach.zones;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
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

				// check zone has required clut number
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
		}

		return true;
	}

	private static void export(File parent, String exportFilePrefix, TimZone zone)
			throws FileNotFoundException, IOException {
		final File saveFile = new File(parent, exportFilePrefix + "_" + zone.getName() + ".tim");

		try (FileOutputStream writer = new FileOutputStream(saveFile)) {
			writeHeader(writer);

			if ((BPP == 8) || (BPP == 9)) {
				final byte[] newClutData = getClutToWrite(zone);

				writeClut(writer, newClutData);
			}

			final byte[] newImageData = getImageToWrite(zone);

			writeImage(writer, zone, newImageData);
		}
	}

	private static byte[] getClutToWrite(TimZone zone) throws MalformedURLException, IOException {
		// assume there is just the one since we pre-checked
		if ((zone.getClutNumber() == null) && (zone.getClutUri() == null)) {
			return CLUT_DATA;
		}

		if (zone.getClutNumber() != null) {
			// get internal clut by number

			if ((zone.getClutNumber() == 1) && (NUMBER_OF_CLUTS == 1)) {
				return CLUT_DATA;
			}

			final byte[] results = new byte[2 * COLORS_PER_CLUT];

			System.arraycopy(CLUT_DATA, results.length * zone.getClutNumber(), results, 0, results.length);

			return results;
		} else {
			// read in new clut

			try (InputStream is = zone.getClutUri().toURL().openStream()) {
				final ByteArrayOutputStream content = new ByteArrayOutputStream();

				while (true) {
					final int size = is.read(BUFFER);
					if (size == -1) {
						break;
					}

					content.write(BUFFER, 0, size);
				}

				return content.toByteArray();
			}
		}
	}

	private static byte[] getImageToWrite(TimZone zone) {
		// TODO Auto-generated method stub
		return null;
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

	private static void writeHeader(FileOutputStream writer) throws IOException {
		writer.write(0x10);
		writer.write(0);
		writer.write(0);
		writer.write(0);
		writer.write(BPP);
		writer.write(0);
		writer.write(0);
		writer.write(0);
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

	private static void writeClut(FileOutputStream writer, byte[] newClutData) throws IOException {
		Util.write32LE(BUFFER, 0, 12 + newClutData.length);
		// palette org x
		Util.write16LE(BUFFER, 4, 0);
		// palette org y
		Util.write16LE(BUFFER, 6, 0);
		Util.write16LE(BUFFER, 8, COLORS_PER_CLUT);
		// new number of cluts
		Util.write16LE(BUFFER, 10, 1);

		writer.write(BUFFER, 0, 12);
		writer.write(newClutData);
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

	private static void writeImage(FileOutputStream writer, TimZone zone, byte[] newImageData) throws IOException {
		Util.write32LE(BUFFER, 0, 12 + newImageData.length);
		// image org x
		Util.write16LE(BUFFER, 4, 0);
		// image org y
		Util.write16LE(BUFFER, 6, 0);
		Util.write16LE(BUFFER, 8, zone.getWidth());
		Util.write16LE(BUFFER, 10, zone.getHeight());

		writer.write(BUFFER, 0, 12);
		writer.write(newImageData);
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
