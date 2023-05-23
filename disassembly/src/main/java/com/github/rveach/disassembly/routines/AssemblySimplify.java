package com.github.rveach.disassembly.routines;

import java.util.List;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.LabelCommand;
import com.github.rveach.disassembly.operations.MultipleCommands;
import com.github.rveach.disassembly.utils.UniqueSortedList;

public final class AssemblySimplify {

	private AssemblySimplify() {
	}

	public static void execute(Holder holder) {
		final List<Integer> labels = new UniqueSortedList<>();
		final AssemblyIterator iterator = holder.getAssemblyRepresentationsIterator();

		while (iterator.hasNext()) {
			final AssemblyRepresentation assembly = iterator.next();
			AbstractCommand representation = assembly.getRepresentation();

			// split multiples
			if (representation instanceof MultipleCommands) {
				final AbstractCommand[] commands = ((MultipleCommands) representation).getCommands();
				boolean first = true;

				for (final AbstractCommand command : commands) {
					if (first) {
						representation = command;

						assembly.setRepresentation(command);

						first = false;
					} else {
						final AssemblyRepresentation item = new AssemblyRepresentation(command);

						iterator.add(item);
					}
				}
			}

			// track labels
			labels.addAll(representation.getHardcodedLabels());
		}

		addLabels(iterator, labels);
	}

	private static void addLabels(AssemblyIterator iterator, List<Integer> labels) {
		if (!labels.isEmpty()) {
			boolean first = true;

			iterator.resetPosition();

			while (iterator.hasNext()) {
				final AssemblyRepresentation assembly = iterator.next();
				final int address = assembly.getAddress();

				if ((labels.contains(address)) || (first)) {
					iterator.add(0, getLabel(address));
				}

				first = false;
			}
		}
	}

	private static AssemblyRepresentation getLabel(int address) {
		return new AssemblyRepresentation(0, 0, 0, "", new LabelCommand(address));
	}

}
