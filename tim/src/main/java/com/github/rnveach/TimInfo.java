package com.github.rnveach;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.rnveach.utils.Util;

public final class TimInfo {

	private static final int BUFFER_LENGTH = 1024 * 10;
	private static final byte[] BUFFER = new byte[BUFFER_LENGTH];

	private TimInfo() {
	}

	public static void main(String... arguments) throws Exception {
		if ((arguments == null) || (arguments.length < 1)) {
			throw new IllegalArgumentException("Missing tim file");
		}

		final File file = new File(arguments[0]);

		print(file);
	}

	private static void print(File file) throws Exception {
		try (final RandomAccessFile reader = new RandomAccessFile(file, "r")) {
			int timSize = 0;

			final int bpp = readHeader(reader);
			timSize += 8;

			System.out.println();

			if ((bpp == 8) || (bpp == 9)) {
				timSize += readClut(reader, timSize);
			} else {
				System.out.println("No CLUT");
			}

			System.out.println();

			timSize += readImage(reader, bpp, timSize);

			System.out.println();
			System.out.println("Total Image Size: " + Util.hex(timSize));
			System.out.println(
					"Total Image Size Formula: Total Header Size + Actual Image Width * Bytes per Image Color * Image Height");
			System.out.println("Total File Size: " + Util.hex(reader.length()));
			System.out.println("Size Difference: " + Util.hex(reader.length() - timSize));
		}
	}

	private static int readHeader(RandomAccessFile reader) throws IOException {
		if (reader.read(BUFFER, 0, 8) != 8) {
			throw new IndexOutOfBoundsException("File too short for header");
		}

		if ((BUFFER[0] != 0x10) || (BUFFER[1] != 0) || (BUFFER[2] != 0) || (BUFFER[3] != 0)) {
			throw new IllegalStateException("Missing TIM header");
		}

		final int result = BUFFER[4];

		if ((result != 8) && (result != 9) && (result != 2) && (result != 3)) {
			throw new IllegalStateException("Unknown BPP: " + result);
		}

		if ((BUFFER[5] != 0) || (BUFFER[6] != 0) || (BUFFER[7] != 0)) {
			throw new IllegalStateException("Missing TIM header");
		}

		System.out.println("BPP Mode: " + result);

		return result;
	}

	private static int readClut(RandomAccessFile reader, int timSize) throws IOException {
		if (reader.read(BUFFER, 0, 12) != 12) {
			throw new IndexOutOfBoundsException("File too short for clut header");
		}

		System.out.println("CLUT Header starts at: " + Util.hex(timSize));

		final int clutDataSize = Util.read32LE(BUFFER, 0) - 12;
		final int paletteOrgX = Util.read16LE(BUFFER, 4);
		final int paletteOrgY = Util.read16LE(BUFFER, 6);
		final int colorsPerClut = Util.read16LE(BUFFER, 8);
		final int numberOfCluts = Util.read16LE(BUFFER, 10);

		System.out.println("CLUT Data Size: " + Util.hex(clutDataSize));
		System.out.println("Palette Org X: " + Util.hex(paletteOrgX));
		System.out.println("Palette Org Y: " + Util.hex(paletteOrgY));

		// 4 bpp is 16, and for 8 bpp is 256.
		System.out.println("Number of Colors Per CLUT: " + colorsPerClut);

		System.out.println("Number of CLUTs: " + numberOfCluts);
		System.out.println("Bytes per Color: " + 2);
		System.out.println("Bytes per CLUT: " + (2 * colorsPerClut));

		System.out.println("CLUT Data starts at: " + Util.hex(reader.getFilePointer()));

		if (reader.skipBytes(clutDataSize) != clutDataSize) {
			throw new IndexOutOfBoundsException("File too short for clut data");
		}

		return 12 + clutDataSize;
	}

	private static int readImage(RandomAccessFile reader, int bpp, int timSize) throws IOException {
		if (reader.read(BUFFER, 0, 12) != 12) {
			throw new IndexOutOfBoundsException("File too short for image data");
		}

		System.out.println("Image Header starts at: " + Util.hex(timSize));

		final int imageDataSize = Util.read32LE(BUFFER, 0) - 12;
		final int imageOrgX = Util.read16LE(BUFFER, 4);
		final int imageOrgY = Util.read16LE(BUFFER, 6);
		final int encodedImageWidth = Util.read16LE(BUFFER, 8);
		final int imageHeight = Util.read16LE(BUFFER, 8);

		System.out.println("Image Data Size: " + Util.hex(imageDataSize));
		System.out.println("Image Org X: " + Util.hex(imageOrgX));
		System.out.println("Image Org Y: " + Util.hex(imageOrgY));

		System.out.println("Encoded Image Width: " + encodedImageWidth);
		System.out.println("Actual Image Width: " + getActualImageWidth(bpp, encodedImageWidth));
		System.out.println("Image Height: " + imageHeight);
		System.out.println("Bytes per Image Color: " + getBytesPerColor(bpp));
		System.out.println("Bytes per Row: " + (getBytesPerColor(bpp) * getActualImageWidth(bpp, encodedImageWidth)));

		System.out.println("Image Data starts at: " + Util.hex(reader.getFilePointer()));

		if (reader.skipBytes(imageDataSize) != imageDataSize) {
			throw new IndexOutOfBoundsException("File too short for image data");
		}

		return 12 + imageDataSize;
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

	private static float getBytesPerColor(int bpp) {
		switch (bpp) {
		case 8:
			return 0.5f;
		case 9:
			return 1;
		case 2:
			return 2;
		case 3:
			return 3;
		}

		throw new IllegalStateException("Unknown BPP: " + bpp);
	}

}
