package com.github.rveach.disassembly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.github.rveach.disassembly.routines.AssemblySimplify;
import com.github.rveach.disassembly.routines.CSimplify;
import com.github.rveach.disassembly.routines.CSimplifyMore;
import com.github.rveach.disassembly.routines.CStructurize;
import com.github.rveach.disassembly.routines.PsxAssembly;
import com.github.rveach.disassembly.routines.PsxAssemblyFix;
import com.github.rveach.disassembly.routines.PsxAssemblySimplify;
import com.github.rveach.disassembly.routines.SaveOriginalC;
import com.github.rveach.disassembly.utils.Util;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.PicocliException;

public final class Main {

	private Main() {
	}

	public static void main(String... arguments) throws IOException {
		final CliOptions cliOptions = new CliOptions();
		final CommandLine commandLine = new CommandLine(cliOptions);

		commandLine.setUsageHelpWidth(CliOptions.HELP_WIDTH);
		commandLine.setCaseInsensitiveEnumValuesAllowed(true);
		commandLine.registerConverter(Integer.class, Integer::decode);
		commandLine.registerConverter(Integer.TYPE, Integer::decode);
		commandLine.registerConverter(Long.class, Long::decode);
		commandLine.registerConverter(Long.TYPE, Long::decode);

		try {
			final ParseResult parseResult = commandLine.parseArgs(arguments);

			if (parseResult.isUsageHelpRequested()) {
				commandLine.usage(System.out);
			} else {
				cliOptions.process();
			}
		} catch (final PicocliException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// for testing
	protected static Holder process(AssemblyType assemblyType, boolean skipAllSimplifies, byte[] buffer) {
		final CliOptions options = new CliOptions();

		options.assemblyType = assemblyType;
		options.skipAllSimplifies = skipAllSimplifies;

		return options.process(buffer);
	}

	@Command(name = "assembly", description = "Programs that extracts assembly to help debugging.", mixinStandardHelpOptions = true)
	private static final class CliOptions {

		private static final int HELP_WIDTH = 100;

		@Option(required = true, names = "-i", description = "Specifies the file to use as input.")
		private File inputFile;

		@Option(required = true, names = "-t", description = "Specifies the type of assembly. Available options are: ${COMPLETION-CANDIDATES}")
		private AssemblyType assemblyType;

		@Option(required = true, names = "-start", description = "Start file location of the assembly.")
		private int startPosition;

		@Option(required = true, names = "-end", description = "End file location of the assembly.")
		private int endPosition;

		// must be long to store 4 byte values by picocli
		@Option(names = "-offset", description = "Offset to convert file location to memory address.")
		private long offset;

		@Option(required = true, names = "-o", description = "Specifies the file to output to.")
		private File outputFile;

		@Option(names = "-skipAllSimplifies", description = "Specifies the file to output to.")
		private boolean skipAllSimplifies;

		public void process() throws IOException {
			final List<String> messages = validate();

			if (messages.isEmpty()) {
				createOutput();
			} else {
				for (final String message : messages) {
					System.err.println(message);
				}
			}
		}

		private List<String> validate() {
			final List<String> results = new ArrayList<>();

			if (!this.inputFile.exists()) {
				results.add("Input file must exist");
			}
			if (!this.inputFile.isFile()) {
				results.add("Input file must be a file");
			}
			if (this.endPosition < this.startPosition) {
				results.add("Start position must come after the end position");
			}
			if (this.outputFile.isDirectory()) {
				results.add("Output file must be a file");
			}

			return results;
		}

		private void createOutput() throws FileNotFoundException, IOException {
			try (final RandomAccessFile reader = new RandomAccessFile(this.inputFile, "r")) {
				try (FileWriter writer = new FileWriter(this.outputFile);
						BufferedWriter bw = new BufferedWriter(writer)) {

					startOutput(writer);

					final byte[] buffer = initializeInput(reader);

					final Holder holder = process(buffer);

					holder.output(writer, this.assemblyType);
				}
			}
		}

		private Holder process(byte[] buffer) {
			final Holder holder = new Holder();

			// assemble

			switch (this.assemblyType) {
			case PSX:
				PsxAssembly.execute(holder, this.startPosition, (int) this.offset, buffer);
				break;
			}

			if (!this.skipAllSimplifies) {
				// simplify on basic assembly

				AssemblySimplify.execute(holder);

				// simplify on basic, system specific assembly

				switch (this.assemblyType) {
				case PSX:
					PsxAssemblySimplify.execute(holder);
					break;
				}

				// save original C for display purposes before we begin any modifications

				SaveOriginalC.execute(holder);

				// fix system specific assembly

				switch (this.assemblyType) {
				case PSX:
					PsxAssemblyFix.execute(holder);
					break;
				}

				boolean changed;

				do {
					changed = false;

					while (CSimplify.execute(holder)) {
						changed = true;
					}

					while (CStructurize.execute(holder)) {
						changed = true;
					}

					while (CSimplifyMore.execute(holder)) {
						// nothing to do
					}
				} while (changed);
			}

			return holder;
		}

		private void startOutput(FileWriter writer) throws IOException {
			writer.write("File:   " + this.inputFile + "\r\n");
			writer.write("Start:  " + Util.hex(this.startPosition) + "\r\n");
			writer.write("End:    " + Util.hex(this.endPosition) + "\r\n");
			writer.write("Offset: " + Util.hex(this.offset) + "\r\n");
			writer.write("-----------------\r\n");
		}

		public byte[] initializeInput(RandomAccessFile reader) throws IOException {
			reader.seek(this.startPosition);

			final byte[] buffer = new byte[(this.endPosition - this.startPosition) + 1];

			reader.read(buffer);

			return buffer;
		}
	}

}
