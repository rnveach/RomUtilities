package com.github.rnveach.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class UriUtil {
	private UriUtil() {
	}

	public static URI getUri(String path) {
		URI uri = getWebOrFileProtocolUri(path);

		if (uri == null) {
			uri = getFilepathUri(path);
		}

		return uri;
	}

	private static URI getWebOrFileProtocolUri(String filename) {
		URI uri;

		try {
			final URL url = new URL(filename);
			uri = url.toURI();
		} catch (URISyntaxException | MalformedURLException ignored) {
			uri = null;
		}
		return uri;
	}

	private static URI getFilepathUri(String filename) {
		final URI uri;
		final File file = new File(filename);

		if (file.exists()) {
			uri = file.toURI();
		} else {
			uri = null;
		}
		return uri;
	}

}
