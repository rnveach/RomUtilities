package com.github.rveach.disassembly;

import static com.google.common.truth.Truth.assertWithMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.github.rveach.disassembly.routines.subroutines.SimplifyHardcodedRegisterAssignmentVisitor;
import com.github.rveach.disassembly.routines.subroutines.SimplifyRegisterAssignmentVisitor;

public abstract class AbstractInstructionTest {

	protected static Method MAIN_PROCESS_METHOD;

	private static final int PSX_BYTES_PER_INSTRUCTION = 4;

	protected void assertPsxProcess(List<String> input, List<String> output) {
		final Holder holder = processPsx(input, true);

		assertWithMessage("Expected output is different").that(convert(holder)).isEqualTo(String.join("\n", output));
	}

	protected void assertPsxCompleteProcess(List<String> input, List<String> assemblyOutput,
			List<String> nonSimplifiedOutput, List<String> simplifiedOutput) {
		reset();

		final Holder nonSimplifiedHolder = processPsx(input, true);

		assertWithMessage("Expected assembly output is different").that(convertAssembly(nonSimplifiedHolder))
				.isEqualTo(String.join("\n", assemblyOutput));

		assertWithMessage("Expected non-simplified output is different").that(convert(nonSimplifiedHolder))
				.isEqualTo(String.join("\n", nonSimplifiedOutput));

		assertWithMessage("Expected simplified output is different").that(convert(processPsx(input, false)))
				.isEqualTo(String.join("\n", simplifiedOutput));
	}

	protected void reset() {
		SimplifyRegisterAssignmentVisitor.get().reset();
		SimplifyHardcodedRegisterAssignmentVisitor.get().reset();
	}

	protected static Holder processPsx(List<String> input, boolean skipAllSimplifies) {
		if (MAIN_PROCESS_METHOD == null) {
			try {
				MAIN_PROCESS_METHOD = Main.class.getDeclaredMethod("process", AssemblyType.class, boolean.class,
						byte[].class);

				MAIN_PROCESS_METHOD.setAccessible(true);
			} catch (NoSuchMethodException | SecurityException ex) {
				throw new IllegalStateException("Unable to get main process", ex);
			}
		}

		try {
			return (Holder) MAIN_PROCESS_METHOD.invoke(null, AssemblyType.PSX, skipAllSimplifies,
					convert(input, PSX_BYTES_PER_INSTRUCTION));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IllegalStateException("Exception when processing", ex);
		}
	}

	private static byte[] convert(List<String> input, int bytesPerInstruction) {
		final byte[] results = new byte[input.size() * bytesPerInstruction];
		int position = 0;

		for (final String line : input) {
			// ignore empty lines or comments
			if (line.isEmpty() || line.trim().startsWith("#")) {
				continue;
			}

			final String[] parts = line.split("\\s+");

			convert(parts, results, position, bytesPerInstruction);

			position += bytesPerInstruction;
		}

		return results;
	}

	private static void convert(String[] s, byte[] results, int position, int size) {
		for (int i = 0; i < size; i++) {
			results[position + i] = (byte) Integer.parseInt(s[i], 0x10);
		}

	}

	private static String convert(Holder holder) {
		final StringBuilder sb = new StringBuilder();
		String previousAppend = null;
		boolean first = true;

		for (final AssemblyRepresentation representation : holder.getAssemblyRepresentations()) {
			final String append = convert(representation);

			if (!append.isEmpty()) {
				if (first) {
					first = false;
				} else if (!previousAppend.isEmpty()) {
					sb.append("\n");
				}

				previousAppend = append;
			}

			sb.append(append);
		}

		return sb.toString();
	}

	private static String convertAssembly(Holder holder) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;

		for (final AssemblyRepresentation representation : holder.getAssemblyRepresentations()) {
			if (first) {
				first = false;
			} else {
				sb.append("\n");
			}

			final String append = representation.getAssemblyDisplay();

			sb.append(append);
		}

		return sb.toString();
	}

	private static String convert(AssemblyRepresentation representation) {
		return representation.getRepresentation().getDisplay();
	}

}
