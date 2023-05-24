package com.github.rveach.disassembly.routines;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.GotoCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.IfCommand;
import com.github.rveach.disassembly.operations.LabelCommand;
import com.github.rveach.disassembly.operations.NopCommand;
import com.github.rveach.disassembly.operations.NotCommand;
import com.github.rveach.disassembly.operations.Operation;
import com.github.rveach.disassembly.operations.OperationCommand;

/**
 * Different branch types to re-arrange:
 *
 * 1)
 *
 * goto B;<br />
 * C;
 *
 * ... turns into ...
 *
 * C;<br />
 * goto B;
 *
 * 2)
 *
 * if (A) goto B;<br />
 * C;
 *
 * ... turns into either ...
 *
 * a)
 *
 * C;<br />
 * if (A) goto B;
 *
 * b)
 *
 * if (!A) goto B_1;<br />
 * C;<br />
 * goto B;<br />
 * B_1:<br />
 * C;
 *
 * Note: (a) depends on if re-arranging C would change the type of logic check
 * done in A or not. If it would change it, then (b) must be done instead as a
 * "special swap".
 */
public final class PsxAssemblyFix {

	private PsxAssemblyFix() {
	}

	public static void execute(Holder holder) {
		AssemblyIterator iterator = holder.getAssemblyRepresentationsIterator();

		while (iterator.hasNext(1)) {
			final AssemblyRepresentation representation = iterator.next();
			final AbstractCommand command = representation.getRepresentation();

			// re-order branches as they are actually delayed
			iterator = reorderBranches(iterator, representation, command);
		}
	}

	private static AssemblyIterator reorderBranches(AssemblyIterator iterator, AssemblyRepresentation representation,
			AbstractCommand command) {
		if ((command instanceof IfCommand) || (command instanceof GotoCommand)) {
			AssemblyRepresentation nextRepresentation = iterator.get(1);
			AbstractCommand nextCommand = nextRepresentation.getRepresentation();
			LabelCommand labelCommand = null;

			if (nextCommand instanceof LabelCommand) {
				// load next next line as we can't move the label

				labelCommand = (LabelCommand) nextCommand;

				nextRepresentation = iterator.get(2);
				nextCommand = nextRepresentation.getRepresentation();
			}

			// ignore if next command is nop as they don't do anything
			if (!(nextCommand instanceof NopCommand)) {
				// more code is needed if next command has a delay too
				if ((nextCommand instanceof IfCommand) || (nextCommand instanceof GotoCommand)) {
					throw new IllegalStateException("Mulitple delay commands not implemented");
				}

				final boolean specialSwapNeeded;

				// determine if a special swap is necessary where commands have to be
				// re-ordered, but re-ordering them changes the logic of the code so we have to
				// find a work around

				if (labelCommand != null) {
					specialSwapNeeded = true;
				} else {
					specialSwapNeeded = isSpecialSwapNeeded(command, nextCommand);
				}

				if (specialSwapNeeded) {
					applySpecialSwap(iterator, (IfCommand) command, nextRepresentation, nextCommand, labelCommand);
				} else {
					// normal swap of order

					nextRepresentation.setRepresentation(command);
					representation.setRepresentation(nextCommand);

					iterator.next();
				}
			}
		}

		return iterator;
	}

	private static boolean isSpecialSwapNeeded(final AbstractCommand command, AbstractCommand nextCommand) {
		final boolean result;

		if (command instanceof GotoCommand) {
			result = false;
		} else if (nextCommand instanceof OperationCommand) {
			// is registers in next left assignment used in original condition

			final OperationCommand operation = ((OperationCommand) nextCommand);
			final AbstractCommand condition = ((IfCommand) command).getCondition();

			if (operation.getOperation() == Operation.ASSIGNMENT) {
				final AbstractCommand left = operation.getLeftOperand();

				result = condition.contains(left);
			} else {
				result = false;
			}
		} else {
			result = false;
		}

		return result;
	}

	private static void applySpecialSwap(AssemblyIterator iterator, IfCommand ifCommand,
			AssemblyRepresentation nextRepresentation, AbstractCommand nextCommand, LabelCommand labelCommand) {
		// if statement must be transformed (see 2b in javadoc)

		// apply 'not' to original condition
		ifCommand.setCondition(new NotCommand(ifCommand.getCondition()));
		// duplicate next command
		iterator.add(new AssemblyRepresentation(nextCommand.deepClone()));
		// add original goto by itself
		iterator.add(new AssemblyRepresentation(ifCommand.getOperation().deepClone()));

		if (labelCommand == null) {
			// create new label and add it

			labelCommand = new LabelCommand(nextRepresentation.getAddress());

			iterator.add(new AssemblyRepresentation(labelCommand));
		} else {
			iterator.next();
		}

		// update if statement for new label
		((GotoCommand) ifCommand.getOperation()).setLocation(new HardcodeValueCommand(labelCommand.getLocation()));
	}

}
