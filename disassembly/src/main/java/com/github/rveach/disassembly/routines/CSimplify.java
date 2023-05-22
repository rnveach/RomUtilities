package com.github.rveach.disassembly.routines;

import java.util.Arrays;
import java.util.List;

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
import com.github.rveach.disassembly.operations.Operation;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;

public final class CSimplify {
	private static boolean[] tracker;

	/**
	 * Different ways to simplify:
	 *
	 * 1)
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
	 * 2)
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
	 */

	// TODO
	// if (!!A) goto B

	// TODO
	// if (!A) goto B

	// TODO
	// if (A) goto B
	// goto C
	// B:

	// TODO:
	// A = (B + 1) + 1

	// TODO:
	// A[0xB][0xC]

	// TODO:
	// (A {equality} B) {equality} C

	// TODO
	// A = (size) B[C]
	// B++;
	// ((size) $B[C]) = A

	private CSimplify() {
	}

	public static boolean execute(Holder holder) {
		boolean result = false;

		final List<AssemblyRepresentation> representations = holder.getAssemblyRepresentations();
		final int size = representations.size();

		tracker = new boolean[size];

		for (int i = 0; i < size; i++) {
			final AssemblyRepresentation representation = representations.get(i);
			final AbstractCommand command = representation.getRepresentation();

			if (isRegisterAssignment(command)) {
				result |= simplifyInitialRegisterAssignmentCommand(representations, i + 1, size, representation,
						command);
			}

			// TODO
		}

		return result;
	}

	private static boolean simplifyInitialRegisterAssignmentCommand(List<AssemblyRepresentation> representations, int i,
			int size, AssemblyRepresentation originalRepresentation, AbstractCommand originalCommand) {
		boolean result = false;

		final AbstractCommand originalAssignment = ((OperationCommand) originalCommand).getLeftCommand();
		final AbstractCommand originalAssignnee = ((OperationCommand) originalCommand).getRightCommand();
		final List<AbstractCommand> originalAssignneeRegisters = originalAssignnee.getRegistersInvolved();

		// initialize tracker

		Arrays.fill(tracker, false);

		tracker[i - 1] = true;

		for (; i < size; i++) {
			final AssemblyRepresentation nextRepresentation = representations.get(i);
			final AbstractCommand nextCommand = nextRepresentation.getRepresentation();

			tracker[i] = true;

			if (nextCommand instanceof LabelCommand) {
				// TODO: implementation
				break;
			}
			// follow gotos, as long as they are hardcoded values, otherwise we treat it as
			// unknown
			if (nextCommand instanceof GotoCommand) {
				final AbstractCommand location = ((GotoCommand) nextCommand).getLocation();

				if (location instanceof HardcodeValueCommand) {
					final int gotoLocation = findLabelPosition(representations, size,
							((HardcodeValueCommand) location).getValue());

					// TODO: we could allow move forward only if the label had 1 goto

					if (!isUsedAgainBeforeAssignment(representations, gotoLocation + 1, size, originalAssignment,
							false)) {
						originalRepresentation.setRepresentation(NopCommand.INSTANCE);
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
					originalRepresentation.setRepresentation(NopCommand.INSTANCE);
					result = true;
					break;
				}
			}

			// command uses the previous assignment but it is not re-assigning to it
			if (nextCommand.contains(originalAssignment)) {
				final boolean isIfCommandNext = nextCommand instanceof IfCommand;

				if (!isUsedAgainBeforeAssignment(representations, i + (isIfCommandNext ? 0 : 1), size,
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
					originalRepresentation.setRepresentation(NopCommand.INSTANCE);
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
			if ((nextCommand instanceof IfCommand) || (nextCommand instanceof JumpSubroutineCommand)) {
				break;
			}
		}

		// if we hit the end, assume everything is used as we don't know how the
		// variables will be used in other functions
		if (i == size) {
			result = true;
		}

		return result;
	}

	// checks for uses only, not assignments
	private static boolean isUsedAgainBeforeAssignment(List<AssemblyRepresentation> representations, int i, int size,
			AbstractCommand commandCheck, boolean skipFirstCommand) {
		boolean result = false;

		for (; i < size; i++) {
			// examined before
			if ((!skipFirstCommand) && (tracker[i])) {
				break;
			}

			final AssemblyRepresentation nextRepresentation = representations.get(i);
			final AbstractCommand nextCommand = nextRepresentation.getRepresentation();

			tracker[i] = true;

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
					i = findLabelPosition(representations, size, ((HardcodeValueCommand) location).getValue());
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
						final int gotoLocation = findLabelPosition(representations, size,
								((HardcodeValueCommand) location).getValue());

						if (isUsedAgainBeforeAssignment(representations, gotoLocation + 1, size, commandCheck, false)
								|| isUsedAgainBeforeAssignment(representations, i + 1, size, commandCheck, false)) {
							result = true;
						}
					} else {
						result = true;
					}

					break;
				}
			}
		}

		return result;
	}

	private static int findLabelPosition(List<AssemblyRepresentation> representations, int size, int location) {
		for (int i = 0; i < size; i++) {
			final AssemblyRepresentation representation = representations.get(i);
			final AbstractCommand command = representation.getRepresentation();

			if ((command instanceof LabelCommand) && (((LabelCommand) command).getLocation() == location)) {
				return i;
			}
		}

		throw new IllegalArgumentException("Could not find label: ");
	}

	// TODO: limiting register reduces some simplifications
	private static boolean isRegisterAssignment(AbstractCommand command) {
		if (command instanceof OperationCommand) {
			final OperationCommand operation = ((OperationCommand) command);

			if ((operation.getOperation() == Operation.ASSIGNMENT)
					&& ((operation.getLeftCommand() instanceof RegisterCommand)
							|| (operation.getLeftCommand() instanceof MultiRegisterCommand))) {
				return true;
			}
		}

		return false;
	}

}
