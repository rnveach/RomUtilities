package com.github.rveach.disassembly.routines;

import java.util.Arrays;
import java.util.List;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.GotoCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.IfCommand;
import com.github.rveach.disassembly.operations.JumpSubroutineCommand;
import com.github.rveach.disassembly.operations.LabelCommand;
import com.github.rveach.disassembly.operations.MultiRegisterCommand;
import com.github.rveach.disassembly.operations.NopCommand;
import com.github.rveach.disassembly.operations.NotCommand;
import com.github.rveach.disassembly.operations.Operation;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;

public final class CSimplify {
	private static boolean[] tracker;

	/**
	 * This class is all about simplifying multiple commands into a smaller amount.
	 *
	 * Different ways to simplify:
	 *
	 * 1)
	 *
	 * NOPs not connected to any code.
	 *
	 * 2)
	 *
	 * Unused labels should be removed.
	 *
	 * Note: Ignore label at position 0 since it is used for display purposes.
	 *
	 * 3)
	 *
	 * A = B ...<br />
	 * A = C ...
	 *
	 * ...turns into...
	 *
	 * A = C ...
	 *
	 * Note: The first line should be erased.
	 *
	 * 4)
	 *
	 * A = B<br />
	 * A = A + C
	 *
	 * ...turns into...
	 *
	 * A = (B) + C
	 *
	 * Note: The first line should be erased. Watch out for "B = G" between first 2
	 * lines because you can't combine this type since the value of B changed.
	 * Crossing labels is fine unless there is a go back and the register is
	 * changed. Functions cannot be crossed unless we identify what the function
	 * does and if it modifies any of the pertinent variables.
	 *
	 * 5)
	 *
	 * if (A) goto B;<br />
	 * goto C;<br />
	 * B:
	 *
	 * ...turns into...
	 *
	 * if (!A) goto C;<br />
	 * B:
	 */

	private CSimplify() {
	}

	public static boolean execute(Holder holder) {
		boolean result = false;

		final AssemblyIterator iterator = holder.getAssemblyRepresentationsIterator();

		tracker = new boolean[iterator.getList().size()];

		// skip over label 0, as it shouldn't be changed
		iterator.next();

		while (iterator.hasNext()) {
			final AssemblyRepresentation representation = iterator.next();
			final AbstractCommand command = representation.getRepresentation();

			// remove nops not connected to any code
			if ((command instanceof NopCommand) && (representation.getAssemblySize() == 0)) {
				iterator.remove();

				result = true;
				continue;
			}

			// remove unused labels
			if (command instanceof LabelCommand) {
				if (!isLabelUsed(iterator, ((LabelCommand) command).getLocation())) {
					iterator.remove();

					result = true;
					continue;
				}
			}

			if (command instanceof IfCommand) {
				final IfCommand ifCommand = (IfCommand) command;

				if (isIfGotoGotoLabelPattern(iterator, ifCommand)) {
					final AssemblyRepresentation gotoRepresentation = iterator.get(1);
					final GotoCommand gotoCommand = (GotoCommand) gotoRepresentation.getRepresentation();

					// update if's condition
					ifCommand.setCondition(new NotCommand(ifCommand.getCondition()));

					// update if's goto
					ifCommand.setOperation(gotoCommand);

					// remove goto as it was inlined
					gotoRepresentation.setRepresentation(NopCommand.get());

					iterator.previous();

					result = true;
					continue;
				}
			}

			if (isRegisterAssignment(command)) {
				if (simplifyInitialRegisterAssignmentCommand(iterator.clone(), representation, command)) {
					iterator.previous();

					result = true;
					continue;
				}
			}
		}

		return result;
	}

	private static boolean isIfGotoGotoLabelPattern(AssemblyIterator iterator, IfCommand command) {
		if (!iterator.hasNext(2)) {
			return false;
		}

		final AssemblyRepresentation gotoRepresentation = iterator.get(1);

		final AbstractCommand gotoCommand = gotoRepresentation.getRepresentation();
		if (!(gotoCommand instanceof GotoCommand)) {
			return false;
		}

		final AssemblyRepresentation labelRepresentation = iterator.get(2);

		final AbstractCommand labelCommand = labelRepresentation.getRepresentation();
		if (!(labelCommand instanceof LabelCommand)) {
			return false;
		}

		final AbstractCommand ifOperation = command.getOperation();
		if (!(ifOperation instanceof GotoCommand)) {
			return false;
		}

		final AbstractCommand ifGotoLocation = ((GotoCommand) ifOperation).getLocation();
		if (!(ifGotoLocation instanceof HardcodeValueCommand)) {
			return false;
		}

		// if's goto has to be the same place as the label
		if (((HardcodeValueCommand) ifGotoLocation).getValue() != ((LabelCommand) labelCommand).getLocation()) {
			return false;
		}

		return true;
	}

	private static boolean simplifyInitialRegisterAssignmentCommand(AssemblyIterator iterator,
			AssemblyRepresentation originalRepresentation, AbstractCommand originalCommand) {
		boolean result = false;

		final AbstractCommand originalAssignment = ((OperationCommand) originalCommand).getLeftOperand();
		final AbstractCommand originalAssignnee = ((OperationCommand) originalCommand).getRightOperand();
		final List<AbstractCommand> originalAssignneeRegisters = originalAssignnee.getRegistersInvolved();

		// initialize tracker

		Arrays.fill(tracker, false);

		tracker[iterator.getPosition()] = true;

		while (iterator.hasNext()) {
			final AssemblyRepresentation nextRepresentation = iterator.next();
			final AbstractCommand nextCommand = nextRepresentation.getRepresentation();

			tracker[iterator.getPosition()] = true;

			// we can't go pass a call, unless we can examine it too, so we treat it as
			// unknown
			if (nextCommand instanceof JumpSubroutineCommand) {
				break;
			}
			if (nextCommand instanceof LabelCommand) {
				// TODO: implementation
				break;
			}
			// follow gotos, as long as they are hardcoded values, otherwise we treat it as
			// unknown
			if (nextCommand instanceof GotoCommand) {
				final AbstractCommand location = ((GotoCommand) nextCommand).getLocation();

				if (location instanceof HardcodeValueCommand) {
					final int gotoLocation = iterator.findLabelPosition(((HardcodeValueCommand) location).getValue());

					// TODO: we could allow move forward only if the label had 1 goto

					if (!isUsedAgainBeforeAssignment(iterator.clone(gotoLocation + 1), originalAssignment, false)) {
						originalRepresentation.setRepresentation(NopCommand.get());
					}
				}

				break;
			}

			if (isRegisterAssignment(nextCommand)) {
				// check if next register assignment is same as original

				if (nextCommand.isAssignedTo(originalAssignment)) {
					// original is getting wiped by next command

					// TODO: don't do a swap of 2+ instances
					// combine the contents of the next command with the result of the previous
					// assignment if possible
					((OperationCommand) nextCommand).swapRightOnly(originalAssignment, originalAssignnee);

					// and wipe the original
					originalRepresentation.setRepresentation(NopCommand.get());
					result = true;
					break;
				}
			}

			// command uses the previous assignment but it is not re-assigning to it
			if (nextCommand.contains(originalAssignment)) {
				final boolean isIfCommandNext = nextCommand instanceof IfCommand;

				if (!isUsedAgainBeforeAssignment(iterator.clone(iterator.getPosition() + (isIfCommandNext ? 0 : 1)),
						originalAssignment, isIfCommandNext)) {
					// original is getting wiped by next command

					// TODO: don't do a swap of 2+ instances
					// combine the contents of the next command with the result of the previous
					// assignment if possible
					if (isIfCommandNext) {
						nextCommand.swap(originalAssignment, originalAssignnee);
					} else {
						// TODO: is this safe?
						((OperationCommand) nextCommand).swapRightOnly(originalAssignment, originalAssignnee);
					}

					// and wipe the original
					originalRepresentation.setRepresentation(NopCommand.get());
					result = true;
				}
				break;
			}

			// one of the variable in the assignee is changing after the assignment, we
			// can't do any swaps as this would break the original assignment
			if (nextCommand.isAssignedToOneOf(originalAssignneeRegisters)) {
				break;
			}

			// TODO: implementation
			if ((nextCommand instanceof IfCommand)) {
				break;
			}
		}

		// if we hit the end, assume everything is used as we don't know how the
		// variables will be used in other functions
		if (!iterator.hasNext()) {
			result = true;
		}

		return result;
	}

	// checks for uses only, not assignments
	private static boolean isUsedAgainBeforeAssignment(AssemblyIterator iterator, AbstractCommand commandCheck,
			boolean skipFirstCommand) {
		boolean result = false;

		while (iterator.hasNext()) {
			final AssemblyRepresentation nextRepresentation = iterator.next();

			// examined before
			if ((!skipFirstCommand) && (tracker[iterator.getPosition()])) {
				break;
			}

			final AbstractCommand nextCommand = nextRepresentation.getRepresentation();

			tracker[iterator.getPosition()] = true;

			// we can't go pass a call, unless we can examine it too, so we treat it as
			// unknown
			if (nextCommand instanceof JumpSubroutineCommand) {
				result = true;
				break;
			}
			// follow gotos, as long as they are hardcoded values, otherwise we treat it as
			// unknown
			if (nextCommand instanceof GotoCommand) {
				final AbstractCommand location = ((GotoCommand) nextCommand).getLocation();

				if (location instanceof HardcodeValueCommand) {
					iterator.gotoPosition(iterator.findLabelPosition(((HardcodeValueCommand) location).getValue()));
					continue;
				} else {
					result = true;
					break;
				}
			}

			if (skipFirstCommand) {
				skipFirstCommand = false;
			} else if (nextCommand.contains(commandCheck)) {
				// we exit if the command is used in some way; however, we say it isn't used if
				// it is only just re-assigned later and read from

				result = true;

				if (nextCommand.isAssignedTo(commandCheck)) {
					if (!nextCommand.isReadFrom(commandCheck)) {
						result = false;
					}
				}

				break;
			}

			// follow 2 paths on gotos, and exit; if either path reports it is used, then
			// report it back
			if (nextCommand instanceof IfCommand) {
				final AbstractCommand operation = ((IfCommand) nextCommand).getOperation();

				if (operation instanceof GotoCommand) {
					final AbstractCommand location = ((GotoCommand) operation).getLocation();

					if (location instanceof HardcodeValueCommand) {
						final int gotoLocation = iterator
								.findLabelPosition(((HardcodeValueCommand) location).getValue());

						if (isUsedAgainBeforeAssignment(iterator.clone(gotoLocation + 1), commandCheck, false)
								|| isUsedAgainBeforeAssignment(iterator.clone(), commandCheck, false)) {
							result = true;
						}
					} else {
						result = true;
					}

					break;
				}
			}
		}

		// if we hit the end, assume everything is used as we don't know how the
		// variables will be used in other functions
		if (!iterator.hasNext()) {
			result = true;
		}

		return result;
	}

	private static boolean isLabelUsed(AssemblyIterator iterator, int location) {
		final List<Integer> calls = iterator.findBranchesTo(location);

		return !calls.isEmpty();
	}

	// TODO: limiting register reduces some simplifications
	private static boolean isRegisterAssignment(AbstractCommand command) {
		if (command instanceof OperationCommand) {
			final OperationCommand operation = ((OperationCommand) command);

			if ((operation.getOperation() == Operation.ASSIGNMENT)
					&& ((operation.getLeftOperand() instanceof RegisterCommand)
							|| (operation.getLeftOperand() instanceof MultiRegisterCommand))) {
				return true;
			}
		}

		return false;
	}
}
