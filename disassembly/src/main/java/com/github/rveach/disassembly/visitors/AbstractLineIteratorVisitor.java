package com.github.rveach.disassembly.visitors;

import com.github.rveach.disassembly.AssemblyIterator;
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

public abstract class AbstractLineIteratorVisitor<T extends AbstractLineIteratorVisitor<?>> {

	protected abstract boolean visit(ByteTruncationCommand byteTruncationCommand, AssemblyIterator iterator);

	protected abstract boolean visit(CustomCallCommand customCallCommand, AssemblyIterator iterator);

	protected abstract boolean visit(GotoCommand gotoCommand, AssemblyIterator iterator);

	protected abstract boolean visit(HardcodeValueCommand hardcodeValueCommand, AssemblyIterator iterator);

	protected abstract boolean visit(IfCommand ifCommand, AssemblyIterator iterator);

	protected abstract boolean visit(JumpSubroutineCommand jumpSubroutineCommand, AssemblyIterator iterator);

	protected abstract boolean visit(LabelCommand labelCommand, AssemblyIterator iterator);

	protected abstract boolean visit(MultipleCommands multipleCommands, AssemblyIterator iterator);

	protected abstract boolean visit(MultiRegisterCommand multiRegisterCommand, AssemblyIterator iterator);

	protected abstract boolean visit(NopCommand nopCommand, AssemblyIterator iterator);

	protected abstract boolean visit(NotCommand notCommand, AssemblyIterator iterator);

	protected abstract boolean visit(OperationCommand operationCommand, AssemblyIterator iterator);

	protected abstract boolean visit(RegisterCommand registerCommand, AssemblyIterator iterator);

	/**
	 * Method to begin initialization.
	 *
	 * @param iterator Iterator being used.
	 */
	protected void beginInit(AssemblyIterator iterator) {
		// to be overridden
	}

	/**
	 * Method for visiting all lines before the specific line.
	 *
	 * @param command  Command being visited on.
	 * @param iterator Iterator being used.
	 */
	protected boolean visitAllAfter(AbstractCommand command, AssemblyIterator iterator) {
		// to be overridden
		return true;
	}

	/**
	 * Method for visiting all lines after the specific line.
	 *
	 * @param command  Command being visited on.
	 * @param iterator Iterator being used.
	 */
	protected boolean visitAllBefore(AbstractCommand command, AssemblyIterator iterator) {
		// to be overridden
		return true;
	}

	/**
	 * Method to end.
	 *
	 * @param iterator             Iterator being used.
	 * @param reachedEndOfIterator {@code true} if the end of the iterator was
	 *                             reached.
	 */
	protected void end(AssemblyIterator iterator, boolean reachedEndOfIterator) {
		// to be overridden
	}

	@SuppressWarnings("unchecked")
	public T begin(AssemblyIterator iterator) {
		beginInit(iterator);

		end(iterator, next(iterator));

		return (T) this;
	}

	protected boolean next(AssemblyIterator iterator) {
		while (iterator.hasNext()) {
			final AbstractCommand command = iterator.next().getRepresentation();

			if (!visitAllBefore(command, iterator)) {
				return false;
			}

			if (command instanceof ByteTruncationCommand) {
				final ByteTruncationCommand converted = (ByteTruncationCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof CustomCallCommand) {
				final CustomCallCommand converted = (CustomCallCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof GotoCommand) {
				final GotoCommand converted = (GotoCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof HardcodeValueCommand) {
				final HardcodeValueCommand converted = (HardcodeValueCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof IfCommand) {
				final IfCommand converted = (IfCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof JumpSubroutineCommand) {
				final JumpSubroutineCommand converted = (JumpSubroutineCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof LabelCommand) {
				final LabelCommand converted = (LabelCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof MultipleCommands) {
				final MultipleCommands converted = (MultipleCommands) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof MultiRegisterCommand) {
				final MultiRegisterCommand converted = (MultiRegisterCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof NopCommand) {
				final NopCommand converted = (NopCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof NotCommand) {
				final NotCommand converted = (NotCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof OperationCommand) {
				final OperationCommand converted = (OperationCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else if (command instanceof RegisterCommand) {
				final RegisterCommand converted = (RegisterCommand) command;

				if (!visit(converted, iterator)) {
					return false;
				}
			} else {
				throw new IllegalStateException("Unknown class: " + command.getClass());
			}

			if (!visitAllAfter(command, iterator)) {
				return false;
			}
		}

		return true;
	}

}
