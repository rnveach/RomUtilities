package com.github.rnveach.utils;

import java.io.File;
import java.util.Arrays;

public final class Util {

	private Util() {
	}

	public static String getFileNameWithoutExtension(File file) {
		final String fileName = file.getName();
		final int pos = fileName.lastIndexOf(".");
		if (pos != -1) {
			return fileName.substring(0, pos);
		}

		return fileName;
	}

	public static String readStringDataFromInput(String line, String lineText) {
		if (!line.startsWith(lineText + ":")) {
			throw new IllegalStateException("Unexpected input, expected '" + lineText + "' in: " + line);
		}

		return line.substring(lineText.length() + 1).trim();
	}

	public static String readString(byte[] buffer, int start, int length) {
		final StringBuilder result = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			final char ch = (char) buffer[start + i];

			if ((ch >= 32) && (ch <= 126)) {
				result.append(ch);
			}
		}

		return result.toString();
	}

	public static byte[] createBytesFrom(byte[] buffer, int start, int length) {
		return Arrays.copyOfRange(buffer, start, start + length);
	}

	public static int read8(byte[] buffer, int start) {
		final int b1 = buffer[start + 0] & 0xFF;

		return b1;
	}

	public static int read16BE(byte[] buffer, int start) {
		final int b1 = buffer[start + 0] & 0xFF;
		final int b2 = buffer[start + 1] & 0xFF;

		return (b1 << 8) | b2;
	}

	public static int read16LE(byte[] buffer, int start) {
		final int b1 = buffer[start + 0] & 0xFF;
		final int b2 = buffer[start + 1] & 0xFF;

		return (b2 << 8) | b1;
	}

	public static void write16LE(byte[] buffer, int start, int value) {
		buffer[start + 0] = (byte) (value & 0xFF);
		buffer[start + 1] = (byte) ((value >> 8) & 0xFF);
	}

	public static int read24LE(byte[] buffer, int start) {
		final int b1 = buffer[start + 0] & 0xFF;
		final int b2 = buffer[start + 1] & 0xFF;
		final int b3 = buffer[start + 2] & 0xFF;

		return (b3 << 16) | (b2 << 8) | b1;
	}

	public static int read32LE(byte[] buffer, int start) {
		final int b1 = buffer[start + 0] & 0xFF;
		final int b2 = buffer[start + 1] & 0xFF;
		final int b3 = buffer[start + 2] & 0xFF;
		final int b4 = buffer[start + 3] & 0xFF;

		return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
	}

	public static int read32BE(byte[] buffer, int start) {
		final int b1 = buffer[start + 0] & 0xFF;
		final int b2 = buffer[start + 1] & 0xFF;
		final int b3 = buffer[start + 2] & 0xFF;
		final int b4 = buffer[start + 3] & 0xFF;

		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	public static void write32LE(byte[] buffer, int start, int value) {
		buffer[start + 0] = (byte) (value & 0xFF);
		buffer[start + 1] = (byte) ((value >> 8) & 0xFF);
		buffer[start + 2] = (byte) ((value >> 16) & 0xFF);
		buffer[start + 3] = (byte) ((value >> 24) & 0xFF);
	}

	public static void write32BE(byte[] buffer, int start, int value) {
		buffer[start + 0] = (byte) ((value >> 24) & 0xFF);
		buffer[start + 1] = (byte) ((value >> 16) & 0xFF);
		buffer[start + 2] = (byte) ((value >> 8) & 0xFF);
		buffer[start + 3] = (byte) (value & 0xFF);
	}

	public static String hexRaw(int number) {
		return String.format("%X", number);
	}

	public static String hexRaw(long number) {
		return String.format("%X", number);
	}

	public static String hexRaw(int number, int size) {
		return String.format("%0" + size + "X", number);
	}

	public static String hex(int number) {
		return String.format("0x%X", number);
	}

	public static String hex(long number) {
		return String.format("0x%X", number);
	}

	public static String hex(int number, int size) {
		return String.format("0x%0" + size + "X", number);
	}
}
