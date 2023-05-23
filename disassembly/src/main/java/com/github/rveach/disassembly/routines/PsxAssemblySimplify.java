package com.github.rveach.disassembly.routines;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.NopCommand;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;

public final class PsxAssemblySimplify {

	private PsxAssemblySimplify() {
	}

	public static void execute(Holder holder) {
		final AbstractCommand r0SwapFrom = new RegisterCommand(PsxAssembly.REGISTERS[0]);
		final AbstractCommand r0SwapTo = new HardcodeValueCommand(0);

		final AssemblyIterator iterator = holder.getAssemblyRepresentationsIterator();

		while (iterator.hasNext()) {
			final AssemblyRepresentation representation = iterator.next();
			final AbstractCommand command = representation.getRepresentation();
			final int assembly = representation.getAssembly();

			// swap register $r0 with hard coded 0x0
			command.swap(r0SwapFrom, r0SwapTo);

			// TODO: '0x0 = '

			// combine lui and ori/addiu
			combineLuiAddiuOri(iterator, command, assembly);
		}
	}

	private static void combineLuiAddiuOri(AssemblyIterator iterator, AbstractCommand command, int assembly) {
		if ((assembly >>> 26) == 15) {
			final AssemblyRepresentation nextRepresentation = iterator.get(1);
			final AbstractCommand nextCommand = nextRepresentation.getRepresentation();

			if ((nextRepresentation.getAssemblyDisplay().startsWith("addiu "))
					|| (nextRepresentation.getAssemblyDisplay().startsWith("ori "))) {
				combineWithLui(command, nextRepresentation, nextCommand);

				iterator.next();
			}
		}
	}

	private static void combineWithLui(AbstractCommand command, AssemblyRepresentation nextRepresentation,
			AbstractCommand nextCommand) {
		final OperationCommand assignment1 = ((OperationCommand) command);
		final OperationCommand assignment2 = ((OperationCommand) nextCommand);

		// assignment to same variable
		if (assignment1.getLeftCommand().equals(assignment2.getLeftCommand())) {
			final OperationCommand operation2 = (OperationCommand) assignment2.getLeftCommand();

			if (assignment1.getLeftCommand().equals(operation2.getLeftCommand())) {
				final HardcodeValueCommand hardcoded1 = (HardcodeValueCommand) assignment1.getRightCommand();
				final HardcodeValueCommand hardcoded2 = (HardcodeValueCommand) operation2.getRightCommand();

				hardcoded1.setValue(hardcoded1.getValue() + hardcoded2.getValue());

				nextRepresentation.setRepresentation(NopCommand.get());
			}
		}
	}

}
