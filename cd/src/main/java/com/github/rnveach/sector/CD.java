package com.github.rnveach.sector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

public final class CD {

	public static final int SECTOR_SIZE = 2352;

	public static final byte[] EMPTY_SYNC = new byte[] { //
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 //
	};

	public static final byte[] SYNC = new byte[] { //
			0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00 //
	};

	private static final byte[] BUFFER_SYNC = new byte[12];

	private final byte[] mode2Sh = new byte[8];

	private final byte[] mode0Data = new byte[2352];
	private final byte[] mode1Data = new byte[2048];
	private final byte[] mode2Form0Data = new byte[2336];
	private final byte[] mode2Form1Data = new byte[2048];
	private final byte[] mode2Form2Data = new byte[2324];

	private final byte[] edcData = new byte[4];
	private final byte[] eccData = new byte[276];

	private final RandomAccessFile reader;

	private int currentSectorNumber;

	private int sectorMode;
	private int sectorModeForm;
	private int sectorMinute;
	private int sectorSecond;
	private int sectorFrame;

	public CD(RandomAccessFile reader) {
		this.reader = reader;
	}

	public void seek(int sectorNumber) throws IOException {
		this.reader.seek(sectorNumber * SECTOR_SIZE);
		this.currentSectorNumber = sectorNumber;
	}

	public void readSector() throws IOException {
		if (this.reader.read(BUFFER_SYNC) != BUFFER_SYNC.length) {
			throw new IllegalStateException("Failed to fully read sync");
		}

		if (Arrays.equals(BUFFER_SYNC, EMPTY_SYNC)) {
			this.sectorMode = 0;
			this.sectorModeForm = 0;
			this.sectorMinute = 0;
			this.sectorSecond = 0;
			this.sectorFrame = 0;

			// skip rest
			this.reader.skipBytes(SECTOR_SIZE - SYNC.length);
		} else if (Arrays.equals(BUFFER_SYNC, SYNC)) {
			this.sectorMinute = this.reader.read();
			this.sectorSecond = this.reader.read();
			this.sectorFrame = this.reader.read();
			this.sectorMode = this.reader.read();

			switch (this.sectorMode) {
			case 1:
				this.sectorModeForm = 0;
				this.reader.read(this.mode1Data);
				this.reader.read(this.edcData);
				this.reader.skipBytes(8);
				this.reader.read(this.eccData);
				break;
			case 2:
				this.sectorModeForm = identifyMode2Form();

				switch (this.sectorModeForm) {
				case 0:
					System.arraycopy(this.mode2Sh, 0, this.mode2Form0Data, 0, this.mode2Form0Data.length);
					this.reader.read(this.mode2Form0Data, this.mode2Sh.length,
							this.mode2Form0Data.length - this.mode2Sh.length);
					break;
				case 1:
					this.reader.read(this.mode2Form1Data);
					this.reader.read(this.edcData);
					this.reader.read(this.eccData);
					break;
				case 2:
					this.reader.read(this.mode2Form2Data);
					this.reader.read(this.edcData);
					break;
				}
				break;
			default:
				throw new IllegalStateException("Unknown sector mode: " + this.sectorMode);
			}
		} else {
			throw new IllegalStateException("Sector has no sync");
		}

		this.currentSectorNumber++;
	}

	public void writeSector(OutputStream writer) throws IOException {
		if (this.sectorMode == 0) {
			writer.write(getCurrentData());
		} else {
			writer.write(CD.SYNC);
			writer.write(getCurrentMinute());
			writer.write(getCurrentSecond());
			writer.write(getCurrentFrame());
			writer.write(getCurrentMode());
			writer.write(getCurrentSh());
			writer.write(getCurrentData());

			if (this.sectorMode == 1) {
				writer.write(getCurrentEdc());
				writer.write(0);
				writer.write(0);
				writer.write(0);
				writer.write(0);
				writer.write(0);
				writer.write(0);
				writer.write(0);
				writer.write(0);
				writer.write(getCurrentEcc());
			} else if (this.sectorModeForm == 1) {
				writer.write(getCurrentEdc());
				writer.write(getCurrentEcc());
			} else if (this.sectorModeForm == 2) {
				writer.write(getCurrentEdc());
			}
		}
	}

	private int identifyMode2Form() throws IOException {
		this.reader.read(this.mode2Sh);

		final int result;

		if ((this.mode2Sh[0] == this.mode2Sh[4]) && ((this.mode2Sh[1] == this.mode2Sh[5]))
				&& (this.mode2Sh[2] == this.mode2Sh[6]) && (this.mode2Sh[3] == this.mode2Sh[7])) {
			if (hasBitMask(this.mode2Sh[2], 0x20)) {
				result = 2;
			} else {
				result = 1;
			}
		} else {
			result = 0;
		}

		return result;
	}

	private static boolean hasBitMask(int bits, int mask) {
		return (bits & mask) == mask;
	}

	public int getCurrentSectorNumber() {
		return this.currentSectorNumber;
	}

	public int getCurrentMode() {
		return this.sectorMode;
	}

	public int getCurrentModeForm() {
		return this.sectorModeForm;
	}

	public int getCurrentMinute() {
		return this.sectorMinute;
	}

	public int getCurrentSecond() {
		return this.sectorSecond;
	}

	public int getCurrentFrame() {
		return this.sectorFrame;
	}

	public byte[] getCurrentSh() {
		return this.mode2Sh;
	}

	public byte[] getCurrentData() {
		switch (this.sectorMode) {
		case 0:
			return this.mode0Data.clone();
		case 1:
			return this.mode1Data.clone();
		case 2:
			switch (this.sectorModeForm) {
			case 0:
				return this.mode2Form0Data.clone();
			case 1:
				return this.mode2Form1Data.clone();
			case 2:
				return this.mode2Form2Data.clone();
			}
		}

		return null;
	}

	public byte[] getCurrentEdc() {
		return this.edcData;
	}

	public byte[] getCurrentEcc() {
		return this.eccData;
	}

}
