package com.github.rnveach.structs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.rnveach.utils.Util;

public final class TimImage {

	private static final int BUFFER_LENGTH = 1024;
	private static final byte[] BUFFER = new byte[BUFFER_LENGTH];

	private byte[] clutData;
	private byte[] imageData;

	private int bpp;
	private int colorsPerClut;
	private int numberOfCluts;
	private int width;
	private int height;

	private TimImage() {
	}

	public TimImage(byte[] clutData, byte[] imageData, int bpp, int colorsPerClut, int numberOfCluts, int width,
			int height) {
		this.clutData = clutData;
		this.imageData = imageData;
		this.bpp = bpp;
		this.colorsPerClut = colorsPerClut;
		this.numberOfCluts = numberOfCluts;
		this.width = width;
		this.height = height;
	}

	public static TimImage load(File file) throws FileNotFoundException, IOException {
		final TimImage result = new TimImage();

		result.loadFile(file);

		return result;
	}

	private void loadFile(File file) throws IOException, FileNotFoundException {
		try (final RandomAccessFile reader = new RandomAccessFile(file, "r")) {
			readHeader(reader);

			if (hasClut()) {
				readClut(reader);
			}

			readImage(reader);
		}
	}

	public void save(File saveFile) throws FileNotFoundException, IOException {
		try (FileOutputStream writer = new FileOutputStream(saveFile)) {
			writeHeader(writer);

			if (hasClut()) {
				writeClut(writer);
			}

			writeImage(writer);
		}
	}

	private void readHeader(RandomAccessFile reader) throws IOException {
		if (reader.read(BUFFER, 0, 8) != 8) {
			throw new IndexOutOfBoundsException("File too short for header");
		}

		if ((BUFFER[0] != 0x10) || (BUFFER[1] != 0) || (BUFFER[2] != 0) || (BUFFER[3] != 0)) {
			throw new IllegalStateException("Missing TIM header");
		}

		this.bpp = BUFFER[4];

		if ((this.bpp != 8) && (this.bpp != 9) && (this.bpp != 2) && (this.bpp != 3)) {
			throw new IllegalStateException("Unknown BPP: " + this.bpp);
		}

		if ((BUFFER[5] != 0) || (BUFFER[6] != 0) || (BUFFER[7] != 0)) {
			throw new IllegalStateException("Missing TIM header");
		}
	}

	private void writeHeader(FileOutputStream writer) throws IOException {
		writer.write(0x10);
		writer.write(0);
		writer.write(0);
		writer.write(0);
		writer.write(this.bpp);
		writer.write(0);
		writer.write(0);
		writer.write(0);
	}

	private void readClut(RandomAccessFile reader) throws IOException {
		if (reader.read(BUFFER, 0, 12) != 12) {
			throw new IndexOutOfBoundsException("File too short for clut header");
		}

		final int clutDataSize = Util.read32LE(BUFFER, 0) - 12;
		this.colorsPerClut = Util.read16LE(BUFFER, 8);
		this.numberOfCluts = Util.read16LE(BUFFER, 10);

		this.clutData = new byte[clutDataSize];

		if (reader.read(this.clutData) != clutDataSize) {
			throw new IndexOutOfBoundsException("File too short for clut data");
		}
	}

	private void writeClut(FileOutputStream writer) throws IOException {
		Util.write32LE(BUFFER, 0, 12 + this.clutData.length);
		// palette org x
		Util.write16LE(BUFFER, 4, 0);
		// palette org y
		Util.write16LE(BUFFER, 6, 0);
		Util.write16LE(BUFFER, 8, this.colorsPerClut);
		// new number of cluts
		Util.write16LE(BUFFER, 10, 1);

		writer.write(BUFFER, 0, 12);
		writer.write(this.clutData);
	}

	private void readImage(RandomAccessFile reader) throws IOException {
		if (reader.read(BUFFER, 0, 12) != 12) {
			throw new IndexOutOfBoundsException("File too short for image data");
		}

		final int imageDataSize = Util.read32LE(BUFFER, 0) - 12;
		this.width = getActualImageWidth(Util.read16LE(BUFFER, 8));
		this.height = Util.read16LE(BUFFER, 10);

		this.imageData = new byte[imageDataSize];

		if (reader.read(this.imageData) != imageDataSize) {
			throw new IndexOutOfBoundsException("File too short for image data");
		}
	}

	private void writeImage(FileOutputStream writer) throws IOException {
		Util.write32LE(BUFFER, 0, 12 + this.imageData.length);
		// image org x
		Util.write16LE(BUFFER, 4, 0);
		// image org y
		Util.write16LE(BUFFER, 6, 0);
		Util.write16LE(BUFFER, 8, getEncodedImageWidth());
		Util.write16LE(BUFFER, 10, this.height);

		writer.write(BUFFER, 0, 12);
		writer.write(this.imageData);
	}

	private int getActualImageWidth(int encodedImageWidth) {
		switch (this.bpp) {
		case 8:
			return encodedImageWidth * 4;
		case 9:
			return encodedImageWidth * 2;
		case 2:
			return encodedImageWidth;
		case 3:
			return (int) (encodedImageWidth * 1.5);
		}

		throw new IllegalStateException("Unknown BPP: " + this.bpp);
	}

	public int getEncodedImageWidth() {
		switch (this.bpp) {
		case 8:
			return this.width / 4;
		case 9:
			return this.width / 2;
		case 2:
			return this.width;
		case 3:
			return (int) (this.width / 1.5);
		}

		throw new IllegalStateException("Unknown BPP: " + this.bpp);
	}

	public float getBytesPerColor() {
		switch (this.bpp) {
		case 8:
			return 0.5f;
		case 9:
			return 1;
		case 2:
			return 2;
		case 3:
			return 3;
		}

		throw new IllegalStateException("Unknown BPP: " + this.bpp);
	}

	public boolean hasClut() {
		return (this.bpp == 8) || (this.bpp == 9);
	}

	public byte[] getClutData() {
		return this.clutData;
	}

	public byte[] getImageData() {
		return this.imageData;
	}

	public int getBpp() {
		return this.bpp;
	}

	public int getColorsPerClut() {
		return this.colorsPerClut;
	}

	public int getNumberOfCluts() {
		return this.numberOfCluts;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

}
