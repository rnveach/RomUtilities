package com.github.rnveach;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.github.rnveach.sector.CD;
import com.github.rnveach.sector.CdDirectoryListing;
import com.github.rnveach.utils.Util;

public final class ExtractFiles {

	private ExtractFiles() {
	}

	public static void main(String[] arguments) throws Exception {
		if ((arguments == null) || (arguments.length < 2)) {
			throw new IllegalArgumentException("Missing file and folder names");
		}

		final File inputFile = new File(arguments[0]);
		final File outputFolder = new File(arguments[1]);

		extract(inputFile, outputFolder);
	}

	private static void extract(File inputFile, File outputFolder) throws FileNotFoundException, IOException {
		try (final RandomAccessFile reader = new RandomAccessFile(inputFile, "r");
				FileWriter fw = new FileWriter(new File(outputFolder, "cd-info.txt"), StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(fw)) {
			extract(new CD(reader), writer, outputFolder);
		}
	}

	private static void extract(CD cd, Writer writer, File outputFolder) throws IOException {
		System.out.println("Reading Sector 16");

		cd.seek(16);
		cd.readSector();

		final byte[] data = cd.getCurrentData();
		final int sector = Util.read32LE(data, 158);
		final int size = Util.read32LE(data, 166);

		extract(cd, writer, sector, size, "", outputFolder);
	}

	private static void extract(CD cd, Writer writer, int sector, int size, String directoryPath, File outputFolder)
			throws IOException {
		System.out.println("Reading Directory at " + sector + " (\\" + directoryPath + ")");

		final CdDirectoryListing listing = new CdDirectoryListing(cd, sector, size);

		writer.write("Directory: " + directoryPath + "\r\n");
		writer.write("\tSector: " + sector + "\r\n");
		writer.write("\tSize: " + size + "\r\n");
		writer.write("\r\n");

		final List<DelayDirectory> delays = new ArrayList<>();
		int position = -1;

		while (listing.hasNext()) {
			listing.nextEntry();

			position++;

			if (listing.getEntryName().isEmpty()) {
				continue;
			}

			final String entryName = getNormalFileName(listing.getEntryName());
			final File entryFile = new File(outputFolder, entryName);

			writer.write("Directory Entry #" + position + "\r\n");
			writer.write("\tName: " + entryName + "\r\n");
			writer.write("\tSector: " + listing.getEntrySector() + "\r\n");
			writer.write("\tSize: " + listing.getEntryLength() + "\r\n");

			if (listing.isEntryDirectory()) {
				delays.add(
						new DelayDirectory(listing.getEntrySector(), listing.getEntryLength(), entryName, entryFile));
			} else {
				extractFile(cd, writer, entryFile, entryName, listing.getEntrySector(), listing.getEntryLength(),
						directoryPath);
			}

			writer.write("\r\n");
		}

		for (final DelayDirectory delay : delays) {
			delay.entryFile.mkdir();

			extract(cd, writer, delay.entrySector, delay.entryLength, directoryPath + "\\" + delay.entryName,
					delay.entryFile);
		}
	}

	private static void extractFile(CD cd, Writer textWriter, File file, String fileName, int sector, int size,
			String directoryPath) throws FileNotFoundException, IOException {
		System.out.println("Extracting File \\" + (directoryPath.isEmpty() ? "" : directoryPath + "\\") + fileName
				+ " at " + sector);

		cd.seek(sector);
		cd.readSector();

		try (FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream writer = new BufferedOutputStream(fos)) {
			// audio/video/real time
			if ((cd.getCurrentMode() == 2) && (cd.getCurrentModeForm() > 0) && ((cd.getCurrentSh()[2] & 0x46) > 0)) {
				textWriter.write("\tSector Mode\r\n");

				// not sure I understand why we assume 2048, but we don't get the whole file if
				// it is larger
				extractFileSectors(writer, cd, size / 2048);
			} else {
				extractFileData(writer, cd, size);
			}
		}
	}

	private static void extractFileSectors(BufferedOutputStream writer, CD cd, int sectors) throws IOException {
		boolean first = true;

		while (sectors > 0) {
			if (first) {
				first = false;
			} else {
				cd.readSector();
			}

			cd.writeSectorTo(writer);

			sectors--;
		}
	}

	private static void extractFileData(BufferedOutputStream writer, CD cd, int size) throws IOException {
		boolean first = true;

		while (size > 0) {
			if (first) {
				first = false;
			} else {
				cd.readSector();
			}

			final byte[] data = cd.getCurrentData();
			final int toWrite = Math.min(data.length, size);

			writer.write(data, 0, toWrite);
			size -= toWrite;
		}
	}

	private static String getNormalFileName(String name) {
		final int semicolon = name.lastIndexOf(';');
		if (semicolon != -1) {
			return name.substring(0, semicolon);
		}

		return name;
	}

	private static final class DelayDirectory {

		private final int entrySector;
		private final int entryLength;
		private final String entryName;
		private final File entryFile;

		private DelayDirectory(int entrySector, int entryLength, String entryName, File entryFile) {
			this.entrySector = entrySector;
			this.entryLength = entryLength;
			this.entryName = entryName;
			this.entryFile = entryFile;
		}

	}

}
