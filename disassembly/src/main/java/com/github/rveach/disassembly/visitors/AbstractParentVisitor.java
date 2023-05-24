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

public abstract class AbstractParentVisitor<T extends AbstractParentVisitor<?>> {

	protected abstract boolean visit(ByteTruncationCommand byteTruncationCommand, AbstractCommand parent);

	protected abstract boolean visit(CustomCallCommand customCallCommand, AbstractCommand parent);

	protected abstract boolean visit(GotoCommand gotoCommand, AbstractCommand parent);

	protected abstract boolean visit(HardcodeValueCommand hardcodeValueCommand, AbstractCommand parent);

	protected abstract boolean visit(IfCommand ifCommand, AbstractCommand parent);

	protected abstract boolean visit(JumpSubroutineCommand jumpSubroutineCommand, AbstractCommand parent);

	protected abstract boolean visit(LabelCommand labelCommand, AbstractCommand parent);

	protected abstract boolean visit(MultipleCommands multipleCommands, AbstractCommand parent);

	protected abstract boolean visit(MultiRegisterCommand multiRegisterCommand, AbstractCommand parent);

	protected abstract boolean visit(NopCommand nopCommand, AbstractCommand parent);

	protected abstract boolean visit(NotCommand notCommand, AbstractCommand parent);

	protected abstract boolean visit(OperationCommand operationCommand, AbstractCommand parent);

	protected abstract boolean visit(RegisterCommand registerCommand, AbstractCommand parent);

	protected void beginInit() {
		// to be overridden
	}

	protected void end() {
		// to be overridden
	}

	@SuppressWarnings("unchecked")
	public T begin(AbstractCommand command) {
		beginInit();

		recurse(null, command);

		end();

		return (T) this;
	}

	protected void recurse(AbstractCommand parent, AbstractCommand command) {
		if (command instanceof ByteTruncationCommand) {
			final ByteTruncationCommand converted = (ByteTruncationCommand) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getCommand());
			}
		} else if (command instanceof CustomCallCommand) {
			final CustomCallCommand converted = (CustomCallCommand) command;

			visit(converted, parent);
		} else if (command instanceof GotoCommand) {
			final GotoCommand converted = (GotoCommand) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getLocation());
			}
		} else if (command instanceof HardcodeValueCommand) {
			final HardcodeValueCommand converted = (HardcodeValueCommand) command;

			visit(converted, parent);
		} else if (command instanceof IfCommand) {
			final IfCommand converted = (IfCommand) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getCondition());
				recurse(converted, converted.getOperation());
			}
		} else if (command instanceof JumpSubroutineCommand) {
			final JumpSubroutineCommand converted = (JumpSubroutineCommand) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getTarget());
				recurse(converted, converted.getReturnLocation());
			}
		} else if (command instanceof LabelCommand) {
			final LabelCommand converted = (LabelCommand) command;

			visit(converted, parent);
		} else if (command instanceof MultipleCommands) {
			final MultipleCommands converted = (MultipleCommands) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getCommands());
			}
		} else if (command instanceof MultiRegisterCommand) {
			final MultiRegisterCommand converted = (MultiRegisterCommand) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getRegisters());
			}
		} else if (command instanceof NopCommand) {
			final NopCommand converted = (NopCommand) command;

			visit(converted, parent);
		} else if (command instanceof NotCommand) {
			final NotCommand converted = (NotCommand) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getTarget());
			}
		} else if (command instanceof OperationCommand) {
			final OperationCommand converted = (OperationCommand) command;

			if (visit(converted, parent)) {
				recurse(converted, converted.getLeftOperand());
				recurse(converted, converted.getRightOperand());
			}
		} else if (command instanceof RegisterCommand) {
			final RegisterCommand converted = (RegisterCommand) command;

			visit(converted, parent);
		} else {
			throw new IllegalStateException("Unknown class: " + command.getClass());
		}
	}

	protected void recurse(AbstractCommand parent, AbstractCommand[] commands) {
		for (final AbstractCommand command : commands) {
			recurse(parent, command);
		}
	}

}
