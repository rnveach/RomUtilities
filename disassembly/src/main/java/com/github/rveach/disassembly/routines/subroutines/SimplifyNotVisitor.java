package com.github.rveach.disassembly.routines.subroutines;

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
import com.github.rveach.disassembly.operations.Operation;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;
import com.github.rveach.disassembly.visitors.AbstractParentVisitor;

/**
 * This removes double {@link NotCommand} and applies single {@link NotCommand}
 * to operators, if available. Result is {@code true} if a change was made.
 */
public final class SimplifyNotVisitor extends AbstractParentVisitor<SimplifyNotVisitor> {

	private boolean result;

	private static final SimplifyNotVisitor INSTANCE = new SimplifyNotVisitor();

	public static SimplifyNotVisitor get() {
		return INSTANCE;
	}

	@Override
	protected void beginInit() {
		this.result = false;
	}

	@Override
	protected boolean visit(ByteTruncationCommand byteTruncationCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(CustomCallCommand customCallCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(GotoCommand gotoCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(HardcodeValueCommand hardcodeValueCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(IfCommand ifCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(JumpSubroutineCommand jumpSubroutineCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(LabelCommand labelCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(MultipleCommands multipleCommands, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(MultiRegisterCommand multiRegisterCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(NopCommand nopCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(NotCommand notCommand, AbstractCommand parent) {
		final AbstractCommand target = notCommand.getTarget();
		boolean changed = false;

		if (target instanceof NotCommand) {
			// remove not of not

			final AbstractCommand newTarget = ((NotCommand) target).getTarget();

			if (parent instanceof OperationCommand) {
				final OperationCommand operationCommand = (OperationCommand) parent;

				if (operationCommand.getLeftOperand() == notCommand) {
					operationCommand.setLeftOperand(newTarget);
				} else {
					operationCommand.setRightOperand(newTarget);
				}
			} else {
				((IfCommand) parent).setCondition(newTarget);
			}

			changed = true;
		} else if (target instanceof OperationCommand) {
			final OperationCommand operationCommand = (OperationCommand) target;
			final Operation operation = operationCommand.getOperation();

			// TODO: logical operators
			if (operation.isEqualityOperation()) {
				// move not command and apply to the equality

				operationCommand.setOperation(operation.getNot());

				((IfCommand) parent).setCondition(operationCommand);

				changed = true;
			}
		}

		// changing instances means its not safe to continue
		return !changed;
	}

	@Override
	protected boolean visit(OperationCommand operationCommand, AbstractCommand parent) {
		return true;
	}

	@Override
	protected boolean visit(RegisterCommand registerCommand, AbstractCommand parent) {
		return true;
	}

	public boolean getResult() {
		return this.result;
	}

}
