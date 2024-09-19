package com.github.rnveach.sector;

import java.io.IOException;

import com.github.rnveach.utils.Util;

public final class CdDirectoryListing {

	private final int directorySize;

	private final CD cd;

	private int sectorPosition;

	private byte[] data;
	private int dataPosition;

	private int entrySector;
	private int entryLength;
	private int entryFlags;
	private int entrySequenceNumber;
	private String entryName;

	public CdDirectoryListing(CD cd, int startSector, int directorySize) throws IOException {
		this.cd = cd;

		this.sectorPosition = startSector;

		this.directorySize = directorySize;

		readNextSector();
	}

	private void readNextSector() throws IOException {
		this.cd.seek(this.sectorPosition);
		this.cd.readSector();
		this.data = this.cd.getCurrentData();
		this.dataPosition = 0;
		this.sectorPosition++;
	}

	public boolean hasNext() {
		return ((this.dataPosition < this.directorySize) && (this.data[this.dataPosition] > 0));
	}

	public void nextEntry() {
		final int entryLength = this.data[this.dataPosition];

		if ((entryLength > 0) && hasNext()) {
			// Extended Attribute Record Length
			this.entrySector = Util.read32LE(this.data, this.dataPosition + 2);
			// MSB form
			this.entryLength = Util.read32LE(this.data, this.dataPosition + 10);
			// MSB form
			// date and time
			this.entryFlags = this.data[this.dataPosition + 25];
			this.entrySequenceNumber = Util.read16LE(this.data, this.dataPosition + 28);

			final int nameLength = this.data[this.dataPosition + 32];

			this.entryName = Util.readString(this.data, this.dataPosition + 33, nameLength);

			// Padding and Extended Attributes/Reserved fields

			this.dataPosition += entryLength;
		} else {
			this.entrySector = 0;
			this.entryLength = 0;
			this.entryFlags = 0;
			this.entrySequenceNumber = 0;
			this.entryName = "";
		}
	}

	public boolean updateNextEntrySimple(String verifyName, int verifyEntrySector, int newEntrySize) {
		if (!hasNext()) {
			throw new IllegalStateException("Could not update " + verifyName + " as it doesn't exist");
		}

		final int nameLength = this.data[this.dataPosition + 32];

		// verifies only start of name since it can have extra data on it
		if (!Util.readString(this.data, this.dataPosition + 33, nameLength).startsWith(verifyName)) {
			throw new IllegalStateException(
					"Could not update " + verifyName + " as the name wasn't found at the same position");
		}

		if (Util.read32LE(this.data, this.dataPosition + 2) != verifyEntrySector) {
			throw new IllegalStateException("Could not update " + verifyName + " as the sector position didn't match");
		}

		if (Util.read32LE(this.data, this.dataPosition + 10) != newEntrySize) {
			Util.write32LE(this.data, this.dataPosition + 10, newEntrySize);
			Util.write32BE(this.data, this.dataPosition + 14, newEntrySize);

			return true;
		} else {
			return false;
		}
	}

	public boolean isEntryDirectory() {
		return ((this.entryFlags & 0x02) == 0x02);
	}

	public int getEntrySectorLength() {
		return (this.entryLength / 2048);
	}

	// //////////////////////////////

	public byte[] getData() {
		return this.data;
	}

	public int getDataPosition() {
		return this.dataPosition;
	}

	public int getEntrySector() {
		return this.entrySector;
	}

	public int getEntryLength() {
		return this.entryLength;
	}

	public int getEntryFlags() {
		return this.entryFlags;
	}

	public int getEntrySequenceNumber() {
		return this.entrySequenceNumber;
	}

	public String getEntryName() {
		return this.entryName;
	}

}
