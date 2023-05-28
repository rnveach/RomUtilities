package com.github.rveach.disassembly;

import static com.google.common.truth.Truth.assertWithMessage;

import java.io.IOException;
import java.util.Arrays;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard.SysErr;
import org.itsallcode.junit.sysextensions.SystemOutGuard;
import org.itsallcode.junit.sysextensions.SystemOutGuard.SysOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({ SystemErrGuard.class, SystemOutGuard.class })
public final class MainTest extends AbstractInstructionTest {

	@BeforeEach
	public void setUp(@SysErr Capturable systemErr, @SysOut Capturable systemOut) {
		systemErr.captureMuted();
		systemOut.captureMuted();
	}

	@Test
	public void testRequiredOptions(@SysErr Capturable systemErr, @SysOut Capturable systemOut) throws IOException {
		Main.main();

		assertWithMessage("Unexpected output log").that(systemOut.getCapturedData()).isEqualTo("");
		assertWithMessage("Unexpected system error log").that(systemErr.getCapturedData())
				.isEqualTo("Missing required options: '-i=<inputFile>', '-t=<assemblyType>',"
						+ " '-start=<startPosition>', '-end=<endPosition>', '-o=<outputFile>'"
						+ System.lineSeparator());

	}

	@Test
	public void testHelp(@SysErr Capturable systemErr, @SysOut Capturable systemOut) throws IOException {
		Main.main("-h");

		assertWithMessage("Unexpected output log").that(systemOut.getCapturedData()).isEqualTo(
				"Usage: assembly [-hV] [-skipAllSimplifies] -end=<endPosition> -i=<inputFile> -o=<outputFile>"
						+ System.lineSeparator()
						+ "                [-offset=<offset>] -start=<startPosition> -t=<assemblyType>"
						+ System.lineSeparator() + "Programs that extracts assembly to help debugging."
						+ System.lineSeparator() + "      -end=<endPosition>   End file location of the assembly."
						+ System.lineSeparator() + "  -h, --help               Show this help message and exit."
						+ System.lineSeparator() + "  -i=<inputFile>           Specifies the file to use as input."
						+ System.lineSeparator() + "  -o=<outputFile>          Specifies the file to output to."
						+ System.lineSeparator()
						+ "      -offset=<offset>     Offset to convert file location to memory address."
						+ System.lineSeparator() + "      -skipAllSimplifies   Specifies the file to output to."
						+ System.lineSeparator() + "      -start=<startPosition>" + System.lineSeparator()
						+ "                           Start file location of the assembly." + System.lineSeparator()
						+ "  -t=<assemblyType>        Specifies the type of assembly. Available options are: PSX"
						+ System.lineSeparator() + "  -V, --version            Print version information and exit."
						+ System.lineSeparator());
		assertWithMessage("Unexpected system error log").that(systemErr.getCapturedData()).isEqualTo("");

	}

	@Test
	public void testNoData() {
		assertPsxProcess(Arrays.asList(""), //
				Arrays.asList(""));
	}

}
