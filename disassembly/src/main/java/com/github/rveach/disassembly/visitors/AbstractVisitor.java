package com.github.rveach.disassembly.visitors;

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

public abstract class AbstractVisitor<T extends AbstractVisitor<?>> {

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

	protected void begin() {
		// to be overridden
	}

	protected void end() {
		// to be overridden
	}

	@SuppressWarnings("unchecked")
	public T begin(AbstractCommand command) {
		begin();

		recurse(command);

		end();

		return (T) this;
	}

	protected void recurse(AbstractCommand command) {
		if (command instanceof ByteTruncationCommand) {
			final ByteTruncationCommand converted = (ByteTruncationCommand) command;

			if (visit(converted)) {
				recurse(converted.getCommand());
			}
		} else if (command instanceof CustomCallCommand) {
			final CustomCallCommand converted = (CustomCallCommand) command;

			visit(converted);
		} else if (command instanceof GotoCommand) {
			final GotoCommand converted = (GotoCommand) command;

			if (visit(converted)) {
				recurse(converted.getLocation());
			}
		} else if (command instanceof HardcodeValueCommand) {
			final HardcodeValueCommand converted = (HardcodeValueCommand) command;

			visit(converted);
		} else if (command instanceof IfCommand) {
			final IfCommand converted = (IfCommand) command;

			if (visit(converted)) {
				recurse(converted.getCondition());
				recurse(converted.getOperation());
			}
		} else if (command instanceof JumpSubroutineCommand) {
			final JumpSubroutineCommand converted = (JumpSubroutineCommand) command;

			if (visit(converted)) {
				recurse(converted.getTarget());
				recurse(converted.getReturnLocation());
			}
		} else if (command instanceof LabelCommand) {
			final LabelCommand converted = (LabelCommand) command;

			visit(converted);
		} else if (command instanceof MultipleCommands) {
			final MultipleCommands converted = (MultipleCommands) command;

			if (visit(converted)) {
				recurse(converted.getCommands());
			}
		} else if (command instanceof MultiRegisterCommand) {
			final MultiRegisterCommand converted = (MultiRegisterCommand) command;

			if (visit(converted)) {
				recurse(converted.getRegisters());
			}
		} else if (command instanceof NopCommand) {
			final NopCommand converted = (NopCommand) command;

			visit(converted);
		} else if (command instanceof NotCommand) {
			final NotCommand converted = (NotCommand) command;

			if (visit(converted)) {
				recurse(converted.getTarget());
			}
		} else if (command instanceof OperationCommand) {
			final OperationCommand converted = (OperationCommand) command;

			if (visit(converted)) {
				recurse(converted.getLeftCommand());
				recurse(converted.getRightCommand());
			}
		} else if (command instanceof RegisterCommand) {
			final RegisterCommand converted = (RegisterCommand) command;

			visit(converted);
		} else {
			throw new IllegalStateException("Unknown class: " + command.getClass());
		}
	}

	private void recurse(AbstractCommand... commands) {
		for (final AbstractCommand command : commands) {
			recurse(command);
		}
	}

}
