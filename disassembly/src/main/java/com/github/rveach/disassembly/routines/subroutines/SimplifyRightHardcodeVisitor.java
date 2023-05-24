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
import com.github.rveach.disassembly.visitors.AbstractVisitor;

/**
 * This swaps operands so the hardcoded value is on the right, if the operator
 * allows it. Result is {@code true} if a change was made.
 */
public final class SimplifyRightHardcodeVisitor extends AbstractVisitor<SimplifyRightHardcodeVisitor> {

	private boolean result;

	private static final SimplifyRightHardcodeVisitor INSTANCE = new SimplifyRightHardcodeVisitor();

	public static SimplifyRightHardcodeVisitor get() {
		return INSTANCE;
	}

	@Override
	protected void beginInit() {
		this.result = false;
	}

	@Override
	protected boolean visit(ByteTruncationCommand byteTruncationCommand) {
		return true;
	}

	@Override
	protected boolean visit(CustomCallCommand customCallCommand) {
		return true;
	}

	@Override
	protected boolean visit(GotoCommand gotoCommand) {
		return true;
	}

	@Override
	protected boolean visit(HardcodeValueCommand hardcodeValueCommand) {
		return true;
	}

	@Override
	protected boolean visit(IfCommand ifCommand) {
		return true;
	}

	@Override
	protected boolean visit(JumpSubroutineCommand jumpSubroutineCommand) {
		return true;
	}

	@Override
	protected boolean visit(LabelCommand labelCommand) {
		return true;
	}

	@Override
	protected boolean visit(MultipleCommands multipleCommands) {
		return true;
	}

	@Override
	protected boolean visit(MultiRegisterCommand multiRegisterCommand) {
		return true;
	}

	@Override
	protected boolean visit(NopCommand nopCommand) {
		return true;
	}

	@Override
	protected boolean visit(NotCommand notCommand) {
		return true;
	}

	@Override
	protected boolean visit(OperationCommand operationCommand) {
		final AbstractCommand leftOperand = operationCommand.getLeftOperand();
		final AbstractCommand rightOperand = operationCommand.getRightOperand();

		if ((leftOperand instanceof HardcodeValueCommand) && (!(rightOperand instanceof HardcodeValueCommand))) {
			final Operation operation = operationCommand.getOperation();

			if (operation.isLeftRightOperandsInterchangeable()) {
				operationCommand.setLeftOperand(rightOperand);
				operationCommand.setRightOperand(leftOperand);

				this.result = true;
			}
		}

		return true;
	}

	@Override
	protected boolean visit(RegisterCommand registerCommand) {
		return true;
	}

	public boolean getResult() {
		return this.result;
	}

}
