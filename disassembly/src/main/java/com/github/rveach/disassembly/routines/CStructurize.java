package com.github.rveach.disassembly.routines;

import java.util.ArrayList;
import java.util.List;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.GotoCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.IfCommand;
import com.github.rveach.disassembly.operations.LabelCommand;

/**
 * This class is all about making the flow of the code follow normal
 * if/else/while/do conventions.
 *
 * Different ways to do this:
 *
 * 1)
 *
 * if (A) goto B<br />
 * ...<br />
 * goto C<br />
 * ...<br />
 * B:
 *
 * ...turns into...
 *
 * if (A) goto B<br />
 * goto C<br />
 * ...<br />
 * goto D<br />
 * B:<br />
 * ...
 *
 * Note: It may not be clear, but the commands between {@code goto B} and
 * {@code goto C} were moved to after the B label.
 *
 * 2)
 *
 * if (A) goto B<br />
 * C...<br />
 * goto D<br />
 * ...<br />
 * C...<br />
 * B:
 *
 * ...turns into...
 *
 * if (A) goto B<br />
 * goto C<br />
 * ...<br />
 * C...<br />
 * B:<br />
 * ...
 *
 * Note: It may not be clear, but the commands between {@code goto B} and
 * {@code goto C} were removed as they were duplicates of what was before B's
 * label.
 */
public final class CStructurize {

	private CStructurize() {
	}

	public static boolean execute(Holder holder) {
		boolean result = false;

		final AssemblyIterator iterator = holder.getAssemblyRepresentationsIterator();

		// skip over label 0, as it shouldn't be changed
		iterator.next();

		while (iterator.hasNext()) {
			final AssemblyRepresentation representation = iterator.next();
			final AbstractCommand command = representation.getRepresentation();

			if (command instanceof IfCommand) {
				if (simplifyIfGotoCommandsGotoSingleLabelPattern(iterator.clone())) {
					iterator.previous();

					result = true;
					continue;
				}

				if (simplifyIfGotoCommandsGotoSingleLabelDuplicateCommandPattern(iterator.clone())) {
					iterator.previous();

					result = true;
					continue;
				}
			}
		}

		return result;
	}

	private static boolean simplifyIfGotoCommandsGotoSingleLabelPattern(AssemblyIterator iterator) {
		final int count = countIfGotoCommandsGotoSingleLabelPattern(iterator);

		// -1 = not found, 0 = same as 'simplifyIfGotoGotoLabelPattern'
		if (count > 0) {
			final List<AbstractCommand> copies = new ArrayList<>();

			// copy and clear 'count' instructions
			for (int i = 0; i < count; i++) {
				copies.add(iterator.next().getRepresentation().deepClone());

				iterator.clear();
			}

			// goto 'goto' label's position + 1
			iterator.gotoPosition(iterator.findLabelPosition(
					((HardcodeValueCommand) ((GotoCommand) iterator.next().getRepresentation()).getLocation())
							.getValue())
					+ 1);

			// add copied instructions
			for (final AbstractCommand copy : copies) {
				iterator.add(new AssemblyRepresentation(copy));
			}

			return true;
		}

		return false;
	}

	private static boolean simplifyIfGotoCommandsGotoSingleLabelDuplicateCommandPattern(AssemblyIterator iterator) {
		final int count = countIfGotoCommandsGotoSingleLabelDuplicateCommandPattern(iterator);

		// -1 = not found, 0 = same as 'simplifyIfGotoGotoLabelPattern'
		if (count > 0) {
			// clear 'count' instructions
			for (int i = 0; i < count; i++) {
				iterator.next();
				iterator.clear();
			}

			return true;
		}

		return false;
	}

	private static int countIfGotoCommandsGotoSingleLabelPattern(AssemblyIterator iterator) {
		int result = 1;

		while (true) {
			if (!iterator.hasNext(result)) {
				return -1;
			}

			final AssemblyRepresentation representation = iterator.get(result);
			final AbstractCommand command = representation.getRepresentation();

			// TODO: can this be left unchecked?
			// TODO: if we are branching into one of the instructions
			if ((command instanceof LabelCommand) || (command instanceof IfCommand)) {
				return -1;
			}

			if (command instanceof GotoCommand) {
				final AbstractCommand location = ((GotoCommand) command).getLocation();

				if (!(location instanceof HardcodeValueCommand)) {
					return -1;
				}

				final int locationValue = ((HardcodeValueCommand) location).getValue();

				if (iterator.findBranchesTo(locationValue).size() != 1) {
					return -1;
				}

				final int beforeLabelPosition = iterator.findLabelPosition(locationValue) - 1;

				if (iterator.getAt(beforeLabelPosition).getRepresentation() instanceof GotoCommand) {
					break;
				}

				return -1;
			}

			result++;
		}

		return result - 1;
	}

	private static int countIfGotoCommandsGotoSingleLabelDuplicateCommandPattern(AssemblyIterator iterator) {
		final List<AbstractCommand> copies = new ArrayList<>();
		int result = 1;

		while (true) {
			if (!iterator.hasNext(result)) {
				return -1;
			}

			final AssemblyRepresentation representation = iterator.get(result);
			final AbstractCommand command = representation.getRepresentation();

			// TODO: can this be left unchecked?
			// TODO: if we are branching into one of the instructions
			if ((command instanceof LabelCommand) || (command instanceof IfCommand)) {
				return -1;
			}

			if (command instanceof GotoCommand) {
				if (copies.size() == 0) {
					return -1;
				}

				final AbstractCommand location = ((GotoCommand) command).getLocation();

				if (!(location instanceof HardcodeValueCommand)) {
					return -1;
				}

				final int locationValue = ((HardcodeValueCommand) location).getValue();

				if (iterator.findBranchesTo(locationValue).size() != 1) {
					return -1;
				}

				int beforeLabelPosition = iterator.findLabelPosition(locationValue) - copies.size();

				for (final AbstractCommand copy : copies) {
					if (!iterator.getAt(beforeLabelPosition).getRepresentation().equals(copy)) {
						return -1;
					}

					beforeLabelPosition++;
				}

				break;
			}

			copies.add(command);
			result++;
		}

		return result - 1;
	}

}
