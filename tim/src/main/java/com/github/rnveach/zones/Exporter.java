package com.github.rnveach.zones;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import com.github.rnveach.structs.TimImage;
import com.github.rnveach.utils.Util;

public final class Exporter {

	private static final int BUFFER_LENGTH = 1024 * 10;
	private static final byte[] BUFFER = new byte[BUFFER_LENGTH];

	private Exporter() {
	}

	public static void process(File file, List<TimZone> zones) throws FileNotFoundException, IOException {
		final TimImage tim = TimImage.load(file);

		if (validateWithTim(tim, zones)) {
			final File parent = file.getParentFile();
			final String exportFilePrefix = Util.getFileNameWithoutExtension(file);

			for (final TimZone zone : zones) {
				export(tim, parent, exportFilePrefix, zone);
			}
		}
	}

	private static boolean validateWithTim(TimImage tim, List<TimZone> zones) {
		for (final TimZone zone : zones) {
			if (tim.hasClut()) {
				// check zone has required clut number
				if ((zone.getClutNumber() == null) && (zone.getClutUri() == null)) {
					if ((tim.getNumberOfCluts() > 1) || (tim.getNumberOfCluts() == 0)) {
						System.err.println(
								"Zone " + zone.getName() + " specifies no CLUTs but the TIM has multiple CLUTs");
						return false;
					}
				} else if (zone.getClutNumber() != null) {
					if (zone.getClutNumber() >= tim.getNumberOfCluts()) {
						System.err.println("Zone " + zone.getName() + " specifies the CLUT #" + zone.getClutNumber()
								+ " but the TIM only has " + tim.getNumberOfCluts() + " CLUTs");
						return false;
					}
				}
			}
		}

		return true;
	}

	private static void export(TimImage tim, File parent, String exportFilePrefix, TimZone zone)
			throws FileNotFoundException, IOException {
		final File saveFile = new File(parent, exportFilePrefix + "_" + zone.getName() + ".tim");

		final TimImage saveTim = new TimImage(getClutToWrite(tim, zone), getImageToWrite(tim, zone), tim.getBpp(),
				tim.getColorsPerClut(), 1, tim.getWidth(), tim.getHeight());

		saveTim.save(saveFile);
	}

	private static byte[] getClutToWrite(TimImage tim, TimZone zone) throws MalformedURLException, IOException {
		if (!tim.hasClut()) {
			return null;
		}

		// assume there is just the one since we pre-checked
		if ((zone.getClutNumber() == null) && (zone.getClutUri() == null)) {
			return tim.getClutData();
		}

		if (zone.getClutNumber() != null) {
			// get internal clut by number

			if ((zone.getClutNumber() == 1) && (tim.getNumberOfCluts() == 1)) {
				return tim.getClutData();
			}

			final byte[] results = new byte[2 * tim.getColorsPerClut()];

			System.arraycopy(tim.getClutData(), results.length * zone.getClutNumber(), results, 0, results.length);

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

	private static byte[] getImageToWrite(TimImage tim, TimZone zone) {
		final int timBytesPerRow = (int) (tim.getBytesPerColor() * tim.getWidth());
		final int zoneBytesPerRow = (int) (tim.getBytesPerColor() * zone.getWidth());
		final byte[] results = new byte[zone.getEncodedWidth(tim.getBpp()) * 2 * zone.getHeight()];

		for (int y = 0; y < zone.getHeight(); y++) {
			for (int x = 0; x < zone.getWidth(); x++) {

			}
		}

		return results;
	}

}
