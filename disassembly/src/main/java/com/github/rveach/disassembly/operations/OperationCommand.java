package com.github.rveach.disassembly.operations;

import java.util.ArrayList;
import java.util.List;

public class OperationCommand extends AbstractCommand {

	private AbstractCommand leftOperand;
	private Operation operation;
	private AbstractCommand rightOperand;

	public OperationCommand(AbstractCommand leftOperand, Operation operation, AbstractCommand rightCommand) {
		this.leftOperand = leftOperand;
		this.operation = operation;
		this.rightOperand = rightCommand;
	}

	public void copyFrom(OperationCommand o) {
		this.leftOperand = o.leftOperand;
		this.operation = o.operation;
		this.rightOperand = o.rightOperand;
	}

	@Override
	public AbstractCommand deepClone() {
		return new OperationCommand(this.leftOperand.deepClone(), this.operation, this.rightOperand.deepClone());
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final OperationCommand o = (OperationCommand) other;

		return this.leftOperand.equals(o.leftOperand) && (this.operation == o.operation)
				&& this.rightOperand.equals(o.rightOperand);
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		return this.leftOperand.contains(other) || this.rightOperand.contains(other);
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return (this.operation == Operation.ASSIGNMENT) && this.leftOperand.equals(o);
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		if (this.operation == Operation.ASSIGNMENT) {
			if ((!(this.leftOperand instanceof RegisterCommand)) && (this.leftOperand.isReadFrom(o))) {
				return true;
			}
		} else if (this.leftOperand.isReadFrom(o)) {
			return true;
		}

		return this.rightOperand.isReadFrom(o);
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		if (this.leftOperand.equals(from)) {
			this.leftOperand = to;
		} else {
			this.leftOperand.swap(from, to);
		}

		swapRightOnly(from, to);
	}

	public void swapRightOnly(AbstractCommand from, AbstractCommand to) {
		if (this.rightOperand.equals(from)) {
			this.rightOperand = to;
		} else {
			this.rightOperand.swap(from, to);
		}
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		final List<Integer> results = new ArrayList<>(this.leftOperand.getHardcodedLabels());

		results.addAll(this.rightOperand.getHardcodedLabels());

		return results;
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		final List<AbstractCommand> results = new ArrayList<>();

		results.addAll(this.leftOperand.getRegistersInvolved());
		results.addAll(this.rightOperand.getRegistersInvolved());

		return results;
	}

	@Override
	public String getDisplay() {
		if (this.operation.isIndex()) {
			return this.leftOperand.getDisplay() + "[" + this.rightOperand.getDisplay() + "]";
		}

		// for assignments, left command equals left command of nested operation
		if (this.operation == Operation.ASSIGNMENT) {
			if (this.rightOperand instanceof OperationCommand) {
				final OperationCommand nestedOperation = (OperationCommand) this.rightOperand;

				if ((this.leftOperand.equals(nestedOperation.leftOperand))
						&& (nestedOperation.getOperation().isSelfSignable())) {
					final String nestedOp = nestedOperation.getOperation().getDisplay();

					return this.leftOperand.getDisplay() + nestedOp.substring(0, nestedOp.length() - 1) + "= "
							+ nestedOperation.rightOperand.getDisplay();
				}
			}
		}

		String display = this.leftOperand.getDisplay() + this.operation.getDisplay();
		final String rightDisplay = this.rightOperand.getDisplay();

		if ((this.operation == Operation.ASSIGNMENT) && (rightDisplay.startsWith("("))) {
			display += rightDisplay.substring(1, rightDisplay.length() - 1);
		} else {
			display += rightDisplay;
		}

		if (this.operation == Operation.ASSIGNMENT) {
			return display;
		}

		return "(" + display + ")";
	}

	public AbstractCommand getLeftOperand() {
		return this.leftOperand;
	}

	public void setLeftOperand(AbstractCommand leftOperand) {
		this.leftOperand = leftOperand;
	}

	public Operation getOperation() {
		return this.operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public AbstractCommand getRightOperand() {
		return this.rightOperand;
	}

	public void setRightOperand(AbstractCommand rightOperand) {
		this.rightOperand = rightOperand;
	}

}
