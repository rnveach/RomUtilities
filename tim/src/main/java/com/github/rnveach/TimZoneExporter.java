package com.github.rnveach;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.github.rnveach.zones.Exporter;
import com.github.rnveach.zones.TimZone;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

public final class TimZoneExporter {

	private TimZoneExporter() {
	}

	public static void main(String[] args) throws IOException {
		final CliOptions cliOptions = new CliOptions();
		final CommandLine commandLine = new CommandLine(cliOptions);
		commandLine.setUsageHelpWidth(100);
		commandLine.setCaseInsensitiveEnumValuesAllowed(true);

		final ParseResult parseResult = commandLine.parseArgs(args);
		if (parseResult.isVersionHelpRequested()) {
			System.out.println("Version: " + TimZoneExporter.class.getPackage().getImplementationVersion());
		} else if (parseResult.isUsageHelpRequested()) {
			commandLine.usage(System.out);
		} else {
			execute(cliOptions);
		}
	}

	private static void execute(CliOptions options) throws IOException {
		if (options.validateCli()) {
			run(options);
		}
	}

	private static void run(CliOptions options) throws IOException {
		final List<TimZone> zones = TimZone.loadAndValidate(options.zoneFile);

		if (zones == null) {
			return;
		}

		Exporter.process(options.file, zones);
	}

	@Command(name = "checkstyle", description = "Checkstyle verifies that the specified "
			+ "source code files adhere to the specified rules. By default, violations are "
			+ "reported to standard out in plain format. Checkstyle requires a configuration "
			+ "XML file that configures the checks to apply.", mixinStandardHelpOptions = true)
	private static final class CliOptions {

		@Parameters(description = "TIM to process")
		private File file;

		@Option(names = "-z", description = "Specifies the location of the file that defines"
				+ " the zone information.")
		private File zoneFile;

		public boolean validateCli() {
			if (this.file == null) {
				System.err.println("TIM is missing");
				return false;
			} else if (this.file.exists() && this.file.isFile()) {
				System.err.println("TIM must exist");
				return false;
			} else if (this.file.canRead()) {
				System.err.println("TIM must be readable");
				return false;
			}

			if (this.zoneFile == null) {
				System.err.println("Zone File is missing");
				return false;
			} else if (this.zoneFile.exists() && this.zoneFile.isFile()) {
				System.err.println("Zone File must exist");
				return false;
			} else if (this.zoneFile.canRead()) {
				System.err.println("Zone File must be readable");
				return false;
			}

			return true;
		}

	}

}
