package com.github.rveach.disassembly.routines.subroutines;

import java.util.List;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.ByteTruncationCommand;
import com.github.rveach.disassembly.operations.CustomCallCommand;
import com.github.rveach.disassembly.operations.GotoCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.IfCommand;
import com.github.rveach.disassembly.operations.JumpSubroutineCommand;
import com.github.rveach.disassembly.operations.LabelCommand;
import com.github.rveach.disassembly.operations.MultiRegisterCommand;
import com.github.rveach.disassembly.operations.MultipleCommands;
import com.github.rveach.disassembly.operations.NopCommand;
import com.github.rveach.disassembly.operations.NotCommand;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;
import com.github.rveach.disassembly.visitors.AbstractLineIteratorVisitor;

/**
 * This re-arranges code by moving first usages backwards, closer to the
 * assignment. Result is {@code true} if a change was made.
 *
 * I consider this a very questionable implementation as it could drastically
 * re-arrange code from it's original purpose making it harder to connect the C
 * with the original code. It may also be possible that this could get stuck in
 * a loop if 2 sets of instructions can be swapped indefinitely.
 *
 * This was created for code that couldn't be naturally merged, like:
 *
 * <pre>
 * $v0 = (byte) $a1[0]
 * $a1 += 1
 * ((byte) $a2[0]) = $v0
 * </pre>
 *
 * $v0 couldn't be brought down since $a1 was modified and it is used in the
 * assignment of $v0. However, it is possible to bring the assignment of $a2[0]
 * up so the 2 are closer and can be merged.
 */
public final class SimplifyDistanceFromFirstRegisterUsageVisitor
		extends AbstractLineIteratorVisitor<SimplifyDistanceFromFirstRegisterUsageVisitor> {

	private AbstractCommand register;

	private boolean result;

	private static final SimplifyDistanceFromFirstRegisterUsageVisitor INSTANCE = new SimplifyDistanceFromFirstRegisterUsageVisitor();

	public static SimplifyDistanceFromFirstRegisterUsageVisitor get() {
		return INSTANCE;
	}

	@Override
	protected void beginInit(AssemblyIterator iterator) {
		this.result = false;
	}

	@Override
	protected boolean visit(ByteTruncationCommand byteTruncationCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(CustomCallCommand customCallCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(GotoCommand gotoCommand, AssemblyIterator iterator) {
		// TODO: implementation
		return false;
	}

	@Override
	protected boolean visit(HardcodeValueCommand hardcodeValueCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(IfCommand ifCommand, AssemblyIterator iterator) {
		// TODO: implementation
		return false;
	}

	@Override
	protected boolean visit(JumpSubroutineCommand jumpSubroutineCommand, AssemblyIterator iterator) {
		// we can't go pass a call, unless we can examine it too, so we treat it as
		// unknown
		return false;
	}

	@Override
	protected boolean visit(LabelCommand labelCommand, AssemblyIterator iterator) {
		// TODO: implementation
		return false;
	}

	@Override
	protected boolean visit(MultipleCommands multipleCommands, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(MultiRegisterCommand multiRegisterCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(NopCommand nopCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(NotCommand notCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(OperationCommand operationCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(RegisterCommand registerCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visitAllBefore(AbstractCommand command, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visitAllAfter(AbstractCommand command, AssemblyIterator iterator) {
		if (command.contains(this.register)) {
			// see if the current command can be moved up until it touches the first

			int count = -1;
			final List<AbstractCommand> commandRegistersInvolved = command.getRegistersInvolved();

			while (true) {
				final AssemblyRepresentation previous = iterator.get(count);
				final AbstractCommand previousCommand = previous.getRepresentation();
				final List<AbstractCommand> previousRegistersInvolved = previousCommand.getRegistersInvolved();

				if (matchesOneOf(commandRegistersInvolved, previousRegistersInvolved)) {
					break;
				}

				final AssemblyRepresentation current = iterator.get(count + 1);

				current.setRepresentation(previousCommand);
				previous.setRepresentation(command);

				count--;

				this.result = true;
			}

			return false;
		}

		return true;
	}

	private static boolean matchesOneOf(List<AbstractCommand> commandRegistersInvolved,
			List<AbstractCommand> previousRegistersInvolved) {
		for (final AbstractCommand r1 : commandRegistersInvolved) {
			for (final AbstractCommand r2 : previousRegistersInvolved) {
				if (r1.equals(r2)) {
					return true;
				}
			}
		}

		return false;
	}

	public AbstractLineIteratorVisitor<SimplifyDistanceFromFirstRegisterUsageVisitor> setOriginalCommand(
			AbstractCommand command) {
		this.register = ((OperationCommand) command).getLeftOperand();

		return this;
	}

	public boolean getResult() {
		return this.result;
	}

}
