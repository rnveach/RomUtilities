package com.github.rnveach.zones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.github.rnveach.utils.UriUtil;

public final class TimZone implements Comparable<TimZone> {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

	private final String name;
	private final int x1;
	private final int y1;
	private final int x2;
	private final int y2;
	private final Integer clutNumber;
	private final URI clutUri;

	private TimZone(String name, int x1, int y1, int x2, int y2, Integer clutNumber, URI clutUri) {
		this.name = name;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.clutNumber = clutNumber;
		this.clutUri = clutUri;
	}

	@Override
	public int compareTo(TimZone o) {
		int d;

		d = Integer.compare(this.y1, this.y2);
		if (d != 0) {
			return d;
		}

		d = Integer.compare(this.x1, this.x2);
		if (d != 0) {
			return d;
		}

		return 0;
	}

	public static List<TimZone> loadAndValidate(File file) throws FileNotFoundException, IOException {
		final List<TimZone> results = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			final List<String> zoneLines = new ArrayList<>();
			String line;

			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.isEmpty()) {
					continue;
				}
				if (line.startsWith(";") || line.startsWith("#")) {
					continue;
				}

				if (line.startsWith("Zone")) {
					if (!zoneLines.isEmpty()) {
						final TimZone zone = validateAndCreate(zoneLines);

						if (zone == null) {
							// error creating zone
							return null;
						}

						results.add(zone);
					}

					zoneLines.clear();
				}

				zoneLines.add(line);
			}
		}

		if (results.isEmpty()) {
			System.err.println("Zone File has no parsable entries");
			return null;
		}

		Collections.sort(results);

		return results;
	}

	private static TimZone validateAndCreate(List<String> zoneLines) {
		final String firstLine = zoneLines.get(0);

		if (!firstLine.startsWith("Zone ")) {
			System.err.println("Missing clear start of Zone: " + firstLine);
			return null;
		}

		final String name = firstLine.substring(5);

		if (zoneLines.size() == 1) {
			System.err.println("Missing zone dimensions for Zone " + name);
			return null;
		}

		final String secondLine = zoneLines.get(1);
		final String[] dimensionStrings = secondLine.split(",\\s*");

		if (dimensionStrings.length < 4) {
			System.err.println("Zone " + name + " dimensions are incomplete: " + secondLine);
			return null;
		}

		final Integer d0 = parse(dimensionStrings[0]);
		final Integer d1 = parse(dimensionStrings[1]);
		final Integer d2 = parse(dimensionStrings[2]);
		final Integer d3 = parse(dimensionStrings[3]);

		if (d0 == null) {
			System.err.println("Zone " + name + " x1 dimension isn't a number: " + secondLine);
			return null;
		} else if (d0 < 0) {
			System.err.println("Zone " + name + " x1 dimension is negative: " + secondLine);
			return null;
		} else if (d1 == null) {
			System.err.println("Zone " + name + " y1 dimension isn't a number: " + secondLine);
			return null;
		} else if (d1 < 0) {
			System.err.println("Zone " + name + " y1 dimension is negative: " + secondLine);
			return null;
		} else if (d2 == null) {
			System.err.println("Zone " + name + " x2 dimension isn't a number: " + secondLine);
			return null;
		} else if (d2 < 0) {
			System.err.println("Zone " + name + " x2 dimension is negative: " + secondLine);
			return null;
		} else if (d3 == null) {
			System.err.println("Zone " + name + " y2 dimension isn't a number: " + secondLine);
			return null;
		} else if (d3 < 0) {
			System.err.println("Zone " + name + " y2 dimension is negative: " + secondLine);
			return null;
		}

		if (d2 < d0) {
			System.err.println("Zone " + name + " x1 dimension is out of order to x2: " + d0 + " vs " + d2);
			return null;
		}
		if (d3 < d1) {
			System.err.println("Zone " + name + " y1 dimension is out of order to y2: " + d1 + " vs " + d3);
			return null;
		}

		Integer clutNumber = null;
		URI clutUri = null;

		if (zoneLines.size() > 2) {
			// optional clut

			final String thirdLine = zoneLines.get(2);

			if (NUMBER_PATTERN.matcher(thirdLine).matches()) {
				clutNumber = parse(thirdLine);
			} else {
				clutUri = UriUtil.getUri(thirdLine);

				if (clutUri == null) {
					System.err.println("Zone " + name + " clut isn't a valid URI or number: " + thirdLine);
					return null;
				}
			}

			for (int i = 3; i < zoneLines.size(); i++) {
				System.out
						.println("Zone " + name + " has extra information which will be ignored: " + zoneLines.get(i));
			}
		}

		return new TimZone(name, d0, d1, d2, d3, clutNumber, clutUri);
	}

	private static Integer parse(String s) {
		try {
			return Integer.parseInt(s);
		} catch (final Throwable t) {
			return null;
		}
	}

	public String getName() {
		return this.name;
	}

	public int getX1() {
		return this.x1;
	}

	public int getY1() {
		return this.y1;
	}

	public int getX2() {
		return this.x2;
	}

	public int getY2() {
		return this.y2;
	}

	public Integer getClutNumber() {
		return this.clutNumber;
	}

	public URI getClutUri() {
		return this.clutUri;
	}

}
