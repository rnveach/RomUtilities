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

public abstract class AbstractLineVisitor<T extends AbstractLineVisitor<?>> {

	protected abstract boolean visit(ByteTruncationCommand byteTruncationCommand);

	protected abstract boolean visit(CustomCallCommand customCallCommand);

	protected abstract boolean visit(GotoCommand gotoCommand);

	protected abstract boolean visit(HardcodeValueCommand hardcodeValueCommand);

	protected abstract boolean visit(IfCommand ifCommand);

	protected abstract boolean visit(JumpSubroutineCommand jumpSubroutineCommand);

	protected abstract boolean visit(LabelCommand labelCommand);

	protected abstract boolean visit(MultipleCommands multipleCommands);

	protected abstract boolean visit(MultiRegisterCommand multiRegisterCommand);

	protected abstract boolean visit(NopCommand nopCommand);

	protected abstract boolean visit(NotCommand notCommand);

	protected abstract boolean visit(OperationCommand operationCommand);

	protected abstract boolean visit(RegisterCommand registerCommand);

	protected void beginInit() {
		// to be overridden
	}

	protected void end() {
		// to be overridden
	}

	@SuppressWarnings("unchecked")
	public T begin(AssemblyIterator iterator) {
		beginInit();

		next(iterator);

		end();

		return (T) this;
	}

	protected void next(AssemblyIterator iterator) {
		while (iterator.hasNext()) {
			final AbstractCommand command = iterator.next().getRepresentation();

			if (command instanceof ByteTruncationCommand) {
				final ByteTruncationCommand converted = (ByteTruncationCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof CustomCallCommand) {
				final CustomCallCommand converted = (CustomCallCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof GotoCommand) {
				final GotoCommand converted = (GotoCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof HardcodeValueCommand) {
				final HardcodeValueCommand converted = (HardcodeValueCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof IfCommand) {
				final IfCommand converted = (IfCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof JumpSubroutineCommand) {
				final JumpSubroutineCommand converted = (JumpSubroutineCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof LabelCommand) {
				final LabelCommand converted = (LabelCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof MultipleCommands) {
				final MultipleCommands converted = (MultipleCommands) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof MultiRegisterCommand) {
				final MultiRegisterCommand converted = (MultiRegisterCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof NopCommand) {
				final NopCommand converted = (NopCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof NotCommand) {
				final NotCommand converted = (NotCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof OperationCommand) {
				final OperationCommand converted = (OperationCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else if (command instanceof RegisterCommand) {
				final RegisterCommand converted = (RegisterCommand) command;

				if (!visit(converted)) {
					break;
				}
			} else {
				throw new IllegalStateException("Unknown class: " + command.getClass());
			}
		}
	}

}
