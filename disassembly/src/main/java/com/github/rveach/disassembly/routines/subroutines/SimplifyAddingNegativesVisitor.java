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
 * This simplifies subtracting or adding a negative number to make it positive
 * and flip the operator. Result is {@code true} if a change was made.
 */
public class SimplifyAddingNegativesVisitor extends AbstractVisitor<SimplifyAddingNegativesVisitor> {

	private boolean result;

	private static final SimplifyAddingNegativesVisitor INSTANCE = new SimplifyAddingNegativesVisitor();

	public static SimplifyAddingNegativesVisitor get() {
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
		final AbstractCommand rightOperand = operationCommand.getRightOperand();

		if (rightOperand instanceof HardcodeValueCommand) {
			final HardcodeValueCommand hardcoded = (HardcodeValueCommand) rightOperand;
			final int value = ((HardcodeValueCommand) rightOperand).getValue();

			// don't convert possibly memory addresses
			if ((value < 0) && ((value >>> 24) != 0x80)) {
				final Operation operation = operationCommand.getOperation();

				if ((operation == Operation.ADD_SIGNED) || (operation == Operation.ADD_UNSIGNED)) {
					operationCommand.setOperation(operation == Operation.ADD_SIGNED ? Operation.SUBTRACT_SIGNED
							: Operation.SUBTRACT_UNSIGNED);

					hardcoded.setValue(-value);
				} else if ((operation == Operation.SUBTRACT_SIGNED) || (operation == Operation.SUBTRACT_UNSIGNED)) {
					operationCommand.setOperation(
							operation == Operation.SUBTRACT_SIGNED ? Operation.ADD_SIGNED : Operation.ADD_UNSIGNED);

					hardcoded.setValue(-value);
				}
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
