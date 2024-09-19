package com.github.rnveach;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.rnveach.sector.CD;
import com.github.rnveach.sector.CdDirectoryListing;
import com.github.rnveach.utils.Util;

public final class InsertFiles {

	private InsertFiles() {
	}

	public static void main(String[] arguments) throws Exception {
		if ((arguments == null) || (arguments.length < 2)) {
			throw new IllegalArgumentException("Missing file and folder names");
		}

		final File inputFolder = new File(arguments[0]);
		final File outputFile = new File(arguments[1]);

		insert(inputFolder, outputFile);
	}

	private static void insert(File inputFolder, File outputFile) throws FileNotFoundException, IOException {
		try (final BufferedReader reader = new BufferedReader(new FileReader(new File(inputFolder, "cd-info.txt")));
				final RandomAccessFile writer = new RandomAccessFile(outputFile, "rw")) {
			insert(reader, new CD(writer), inputFolder);
		}
	}

	private static void insert(BufferedReader reader, CD cd, File inputFolder) throws IOException {
		final String startLine = reader.readLine();

		if (!startLine.startsWith("Directory: ")) {
			throw new IllegalStateException("Failed to find the first directory, but found: " + startLine);
		}

		insertDirectory(reader, cd, startLine, inputFolder);
	}

	private static void insertDirectory(BufferedReader reader, CD cd, String startDirectoryLine, File inputFolder)
			throws IOException {
		final String directoryPath = Util.readStringDataFromInput(startDirectoryLine.trim(), "Directory");
		final int directorySector = Integer.parseInt(Util.readStringDataFromInput(reader.readLine().trim(), "Sector"));
		final int directorySize = Integer.parseInt(Util.readStringDataFromInput(reader.readLine().trim(), "Size"));

		if (!"".equals(reader.readLine())) {
			throw new IllegalStateException("Did not find an empty line before next entry");
		}

		final File inputDirectory = new File(inputFolder, directoryPath);

		final CdDirectoryListing listing = new CdDirectoryListing(cd, directorySector, directorySize);
		int lastEntryPosition = 0;
		boolean writeDirectoryBack = false;

		String line;

		while ((line = reader.readLine()) != null) {
			if (line.startsWith("Directory: ")) {
				insertDirectory(reader, cd, startDirectoryLine, inputFolder);
				break;
			}

			if (!line.startsWith("Directory Entry #")) {
				throw new IllegalStateException("Failed to find a Directory Entry, but found: " + line);
			}

			final int entryNumber = Integer.parseInt(line.substring(17));
			final String entryName = Util.readStringDataFromInput(reader.readLine().trim(), "Name");
			final int entrySector = Integer.parseInt(Util.readStringDataFromInput(reader.readLine().trim(), "Sector"));
			final int entrySize = Integer.parseInt(Util.readStringDataFromInput(reader.readLine().trim(), "Size"));
			boolean entrySectorMode = false;

			final String nextLine = reader.readLine().trim();

			if ("Sector Mode".equals(nextLine)) {
				entrySectorMode = true;

				if (!"".equals(reader.readLine())) {
					throw new IllegalStateException("Did not find an empty line before next entry");
				}
			} else if (!"".equals(nextLine)) {
				throw new IllegalStateException("Did not find an empty line before next entry");
			}

			for (; lastEntryPosition < (entryNumber - 1); lastEntryPosition++) {
				listing.nextEntry();
			}

			final File insertFile = new File(inputDirectory, entryName);

			if (insertFile.exists()) {
				final long insertFileLength = insertFile.length();
				final long insertSize;

				if (entrySectorMode) {
					insertSize = (long) (Math.floor(insertFileLength / CD.SECTOR_SIZE)) * 2048;
				} else {
					insertSize = insertFileLength;
				}

				if ((insertSize / 2048) > (entrySize / 2048)) {
					System.err.println("File " + directoryPath + "\\" + entryName
							+ " wants to insert a file bigger than the original but can't");
				} else {
					if (insertSize != entrySize) {
						if (listing.updateNextEntrySimple(entryName, entrySector, (int) insertSize)) {
							writeDirectoryBack = true;
						}
					}

					cd.seek(entrySector);
					cd.overlayWithFile(insertFile, entrySectorMode);
				}
			} else {
				System.err.println("Failed to find file to insert, skipping: " + directoryPath + "\\" + entryName);
			}
		}

		if (writeDirectoryBack) {
			cd.seek(directorySector);
			// cd.updateSector(listing.getData());
			throw new IllegalStateException("Not implemented");
		}
	}

}
