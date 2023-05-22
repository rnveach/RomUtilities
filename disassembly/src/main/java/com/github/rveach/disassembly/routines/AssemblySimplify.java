package com.github.rveach.disassembly.routines;

import java.util.List;

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
		final List<AssemblyRepresentation> representations = holder.getAssemblyRepresentations();
		final List<Integer> labels = new UniqueSortedList<>();
		int size = representations.size();

		for (int i = 0; i < size; i++) {
			final AssemblyRepresentation assembly = representations.get(i);
			AbstractCommand representation = assembly.getRepresentation();

			// split multiples
			if (representation instanceof MultipleCommands) {
				final AbstractCommand[] commands = ((MultipleCommands) representation).getCommands();

				representations.remove(i);

				boolean first = true;

				for (final AbstractCommand command : commands) {
					if (first) {
						representation = command;

						assembly.setRepresentation(command);

						first = false;
					} else {
						final AssemblyRepresentation item = new AssemblyRepresentation(command);

						representations.add(i, item);

						i++;
						size++;
					}
				}
			}

			// track labels
			labels.addAll(representation.getHardcodedLabels());
		}

		addLabels(representations, labels, size);
	}

	private static void addLabels(List<AssemblyRepresentation> representations, List<Integer> labels, int size) {
		if (!labels.isEmpty()) {
			boolean first = true;

			for (int i = 0; i < size; i++) {
				final AssemblyRepresentation assembly = representations.get(i);
				final int address = assembly.getAddress();

				if ((labels.contains(address)) || (first)) {
					representations.add(i, getLabel(address));

					i++;
					size++;
				}

				first = false;
			}
		}
	}

	private static AssemblyRepresentation getLabel(int address) {
		return new AssemblyRepresentation(0, 0, 0, "", new LabelCommand(address));
	}

}
