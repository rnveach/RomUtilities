package com.github.rveach.disassembly.routines;

import java.util.List;

import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;

public class SaveOriginalC {

	public static void execute(Holder holder) {
		final List<AssemblyRepresentation> representations = holder.getAssemblyRepresentations();

		for (final AssemblyRepresentation representation : representations) {
			final AbstractCommand command = representation.getRepresentation();

			// don't copy new entries we create as they are only for the main representation
			if (representation.getAssemblySize() != 0) {
				representation.setSaveRepresentation(command.deepClone());
			}
		}
	}

}
