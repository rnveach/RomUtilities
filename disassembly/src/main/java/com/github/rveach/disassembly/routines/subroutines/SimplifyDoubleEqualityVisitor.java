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
 * This removes double {@link OperationCommand} where both operations use an
 * equality operator and one side of the operator is a hardcode value. Result is
 * {@code true} if a change was made.
 */
public class SimplifyDoubleEqualityVisitor extends AbstractParentVisitor<SimplifyDoubleEqualityVisitor> {

	private boolean result;

	private static final SimplifyDoubleEqualityVisitor INSTANCE = new SimplifyDoubleEqualityVisitor();

	public static SimplifyDoubleEqualityVisitor get() {
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
		if (parent instanceof OperationCommand) {
			final OperationCommand parentOperationCommand = (OperationCommand) parent;
			final Operation parentOp = parentOperationCommand.getOperation();

			if ((operationCommand.getOperation().isEqualityOperation()) && (parentOp.isEqualityOperation())) {
				final boolean originalFromLeft = (parentOperationCommand.getLeftOperand() == operationCommand);
				final AbstractCommand comparingTo;

				if (originalFromLeft) {
					comparingTo = parentOperationCommand.getRightOperand();
				} else {
					comparingTo = parentOperationCommand.getLeftOperand();
				}

				if (comparingTo instanceof HardcodeValueCommand) {
					final int value = ((HardcodeValueCommand) comparingTo).getValue();

					if ((value == 0) || (value == 1)) {
						final boolean set = (value == 1);
						boolean applyNot = false;

						switch (parentOp) {
						case EQUAL:
							applyNot = !set;
							break;
						case NOT_EQUAL:
							applyNot = set;
							break;
						case GREATER_THAN_OR_EQUAL_SIGNED:
						case GREATER_THAN_OR_EQUAL_UNSIGNED:
						case GREATER_THAN_SIGNED:
						case GREATER_THAN_UNSIGNED:
						case LESS_THAN_OR_EQUAL_SIGNED:
						case LESS_THAN_OR_EQUAL_UNSIGNED:
						case LESS_THAN_SIGNED:
						case LESS_THAN_UNSIGNED:
						case ADD_SIGNED:
						case ADD_UNSIGNED:
						case AND:
						case ASSIGNMENT:
						case DIVIDE_SIGNED:
						case DIVIDE_UNSIGNED:
						case INDEX:
						case MOD:
						case MULTIPLY_SIGNED:
						case MULTIPLY_UNSIGNED:
						case OR:
						case SHIFT_LEFT:
						case SHIFT_RIGHT_ARITHMETIC:
						case SHIFT_RIGHT_LOGICAL:
						case SUBTRACT_SIGNED:
						case SUBTRACT_UNSIGNED:
						case XOR:
							throw new IllegalStateException("Unexpected Operator: " + parentOp);
						}

						parentOperationCommand.copyFrom(operationCommand);

						if (applyNot) {
							parentOperationCommand.setOperation(parentOperationCommand.getOperation().getNot());
						}

						this.result = true;
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
