package com.github.rveach.disassembly.routines;

import java.util.List;

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
import com.github.rveach.disassembly.routines.subroutines.SimplifyDistanceFromFirstRegisterUsageVisitor;
import com.github.rveach.disassembly.routines.subroutines.SimplifyRegisterAssignmentVisitor;
import com.github.rveach.disassembly.utils.AssemblyUtil;

public final class CSimplify {

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
	 *
	 * 6)
	 *
	 * A = B...<br />
	 * B++<br />
	 * C = A...
	 *
	 * ...turns into...
	 *
	 * A = B...<br />
	 * C = A...<br />
	 * B++
	 *
	 * Note: This brings variables closer to their usage to assist with the hopes
	 * the lines can be simplified in the end. No similar registers can be involved
	 * in the lines being swapped.
	 */

	private CSimplify() {
	}

	public static boolean execute(Holder holder) {
		boolean result = false;

		final AssemblyIterator iterator = holder.getAssemblyRepresentationsIterator();

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

				if (simplifyIfGotoGotoLabelPattern(iterator, ifCommand)) {
					result = true;
					continue;
				}
			}

			if (AssemblyUtil.isRegisterAssignment(command)) {
				if (SimplifyRegisterAssignmentVisitor.get().setOriginalRepresentation(representation)
						.setOriginalCommand(command).begin(iterator.clone()).getResult()) {
					iterator.previous();

					result = true;
					continue;
				} else if (SimplifyDistanceFromFirstRegisterUsageVisitor.get().setOriginalCommand(command)
						.begin(iterator.clone()).getResult()) {
					iterator.previous();

					result = true;
					continue;
				}
			}
		}

		return result;
	}

	private static boolean simplifyIfGotoGotoLabelPattern(final AssemblyIterator iterator, final IfCommand ifCommand) {
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

			return true;
		}

		return false;
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

	private static boolean isLabelUsed(AssemblyIterator iterator, int location) {
		final List<Integer> calls = iterator.findBranchesTo(location);

		return !calls.isEmpty();
	}

}
