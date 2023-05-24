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
 * This removes some hardcoded value operations used in ways not easily
 * understood. Result is {@code true} if a change was made.
 *
 * Waring: Some of this can produce adding a negative. Keeping hardcoded values
 * on the right is being maintained.
 *
 * Examples:
 *
 * 1)
 *
 * (A << #) +/- A
 *
 * ...turns into...
 *
 * A * ((1 << #) +/- 1)
 *
 * 2)
 *
 * (A + 1) + 1
 *
 * ...turns into...
 *
 * A + 2
 */
public class SimplifySomeHardcodeValuesVisitor extends AbstractParentVisitor<SimplifySomeHardcodeValuesVisitor> {

	private boolean result;

	private static final SimplifySomeHardcodeValuesVisitor INSTANCE = new SimplifySomeHardcodeValuesVisitor();

	public static SimplifySomeHardcodeValuesVisitor get() {
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
		return true;
	}

	@Override
	protected boolean visit(OperationCommand operationCommand, AbstractCommand parent) {
		final AbstractCommand rightOperand = operationCommand.getRightOperand();

		if ((rightOperand instanceof HardcodeValueCommand) && (parent instanceof OperationCommand)) {
			final Operation operation = operationCommand.getOperation();

			if (operation == Operation.SHIFT_LEFT) {
				final AbstractCommand leftOperand = operationCommand.getLeftOperand();
				final OperationCommand parentOperation = (OperationCommand) parent;
				final AbstractCommand parentRightOperand = parentOperation.getRightOperand();

				if (leftOperand.equals(parentRightOperand)) {
					// instances of (A << #) +/- A

					final Operation parentOp = parentOperation.getOperation();
					final int value = ((HardcodeValueCommand) rightOperand).getValue();

					if (parentOp == Operation.ADD_SIGNED) {
						parentOperation.setLeftOperand(parentOperation.getRightOperand());
						parentOperation.setOperation(Operation.MULTIPLY_SIGNED);
						parentOperation.setRightOperand(new HardcodeValueCommand((1 << value) + 1));

						return false;
					} else if (parentOp == Operation.ADD_UNSIGNED) {
						parentOperation.setLeftOperand(parentOperation.getRightOperand());
						parentOperation.setOperation(Operation.MULTIPLY_UNSIGNED);
						parentOperation.setRightOperand(new HardcodeValueCommand((1 << value) + 1));

						return false;
					} else if (parentOp == Operation.SUBTRACT_SIGNED) {
						parentOperation.setLeftOperand(parentOperation.getRightOperand());
						parentOperation.setOperation(Operation.MULTIPLY_SIGNED);
						parentOperation.setRightOperand(new HardcodeValueCommand((1 << value) - 1));

						return false;
					} else if (parentOp == Operation.SUBTRACT_UNSIGNED) {
						parentOperation.setLeftOperand(parentOperation.getRightOperand());
						parentOperation.setOperation(Operation.MULTIPLY_UNSIGNED);
						parentOperation.setRightOperand(new HardcodeValueCommand((1 << value) - 1));

						return false;
					}
				}
			} else if ((operation == Operation.ADD_SIGNED) || (operation == Operation.ADD_UNSIGNED)
					|| (operation == Operation.SUBTRACT_SIGNED) || (operation == Operation.SUBTRACT_UNSIGNED)) {
				final OperationCommand parentOperation = (OperationCommand) parent;
				final AbstractCommand parentRightOperand = parentOperation.getRightOperand();

				if (parentRightOperand instanceof HardcodeValueCommand) {
					final AbstractCommand leftOperand = operationCommand.getLeftOperand();
					int value1 = ((HardcodeValueCommand) rightOperand).getValue();
					final int value2 = ((HardcodeValueCommand) parentRightOperand).getValue();
					final Operation parentOp = parentOperation.getOperation();

					if ((operation == Operation.SUBTRACT_SIGNED) || (operation == Operation.SUBTRACT_UNSIGNED)) {
						value1 = -value1;
					}

					final Operation newOp;

					if ((operation == Operation.ADD_SIGNED) || (operation == Operation.ADD_UNSIGNED)) {
						newOp = operation;
					} else if (operation == Operation.SUBTRACT_SIGNED) {
						newOp = Operation.ADD_SIGNED;
					} else {
						newOp = Operation.ADD_UNSIGNED;
					}

					if ((parentOp == Operation.ADD_SIGNED) || (parentOp == Operation.ADD_UNSIGNED)) {
						parentOperation.setLeftOperand(leftOperand);
						parentOperation.setOperation(newOp);
						parentOperation.setRightOperand(new HardcodeValueCommand(value1 + value2));

						return false;
					} else if ((parentOp == Operation.SUBTRACT_SIGNED) || ((parentOp == Operation.SUBTRACT_UNSIGNED))) {
						parentOperation.setLeftOperand(leftOperand);
						parentOperation.setOperation(newOp);
						parentOperation.setRightOperand(new HardcodeValueCommand(value1 - value2));

						return false;
					}
				}
			}
		}

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
