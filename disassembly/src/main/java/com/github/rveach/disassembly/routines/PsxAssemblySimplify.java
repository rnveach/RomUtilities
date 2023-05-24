package com.github.rveach.disassembly.routines;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.NopCommand;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;
import com.github.rveach.disassembly.utils.AssemblyUtil;

public final class PsxAssemblySimplify {

	/**
	 * This class is all about simplifying very general PSX-specific assembly
	 * display.
	 *
	 * Different ways to simplify:
	 *
	 * 1)
	 *
	 * $r0 = ...
	 *
	 * ..turns into...
	 *
	 * NOP
	 *
	 * 2)
	 *
	 * Any usage of $r0 is turned into 0x0.
	 *
	 * 3)
	 *
	 * lui and ori/addiu consecutive commands are turned into a single load.
	 */

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

			// assignment to $r0 is considered a NOP
			if (AssemblyUtil.isRegisterAssignment(command)) {
				if (((OperationCommand) command).getLeftOperand().equals(r0SwapFrom)) {
					representation.setRepresentation(NopCommand.get());

					continue;
				}
			}

			// swap register $r0 with hard coded 0x0
			command.swap(r0SwapFrom, r0SwapTo);

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
		if (assignment1.getLeftOperand().equals(assignment2.getLeftOperand())) {
			final OperationCommand operand2 = (OperationCommand) assignment2.getLeftOperand();

			if (assignment1.getLeftOperand().equals(operand2.getLeftOperand())) {
				final HardcodeValueCommand hardcoded1 = (HardcodeValueCommand) assignment1.getRightOperand();
				final HardcodeValueCommand hardcoded2 = (HardcodeValueCommand) operand2.getRightOperand();

				hardcoded1.setValue(hardcoded1.getValue() + hardcoded2.getValue());

				nextRepresentation.setRepresentation(NopCommand.get());
			}
		}
	}

}
