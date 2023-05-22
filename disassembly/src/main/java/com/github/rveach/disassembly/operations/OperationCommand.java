package com.github.rveach.disassembly.operations;

import java.util.ArrayList;
import java.util.List;

public class OperationCommand extends AbstractCommand {

	private AbstractCommand leftCommand;
	private final Operation operation;
	private AbstractCommand rightCommand;

	public OperationCommand(AbstractCommand leftCommand, Operation operation, AbstractCommand rightCommand) {
		this.leftCommand = leftCommand;
		this.operation = operation;
		this.rightCommand = rightCommand;
	}

	@Override
	public AbstractCommand deepClone() {
		return new OperationCommand(this.leftCommand.deepClone(), this.operation, this.rightCommand.deepClone());
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final OperationCommand o = (OperationCommand) other;

		if (!this.leftCommand.equals(o.leftCommand)) {
			return false;
		}

		if (this.operation != o.operation) {
			return false;
		}

		return this.rightCommand.equals(o.rightCommand);
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		return this.leftCommand.contains(other) || this.rightCommand.contains(other);
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return (this.operation == Operation.ASSIGNMENT) && this.leftCommand.equals(o);
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		if (this.operation == Operation.ASSIGNMENT) {
			if ((!(this.leftCommand instanceof RegisterCommand)) && (this.leftCommand.isReadFrom(o))) {
				return true;
			}
		} else if (this.leftCommand.isReadFrom(o)) {
			return true;
		}

		return this.rightCommand.isReadFrom(o);
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		if (this.leftCommand.equals(from)) {
			this.leftCommand = to;
		} else {
			this.leftCommand.swap(from, to);
		}

		swapRightOnly(from, to);
	}

	public void swapRightOnly(AbstractCommand from, AbstractCommand to) {
		if (this.rightCommand.equals(from)) {
			this.rightCommand = to;
		} else {
			this.rightCommand.swap(from, to);
		}
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		final List<Integer> results = new ArrayList<>(this.leftCommand.getHardcodedLabels());

		results.addAll(this.rightCommand.getHardcodedLabels());

		return results;
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		final List<AbstractCommand> results = new ArrayList<>();

		results.addAll(this.leftCommand.getRegistersInvolved());
		results.addAll(this.rightCommand.getRegistersInvolved());

		return results;
	}

	@Override
	public String getDisplay() {
		if (!this.operation.isIndex()) {
			return this.leftCommand.getDisplay() + "[" + this.rightCommand.getDisplay() + "]";
		}

		// for assignments, left command equals left command of nested operation
		if (this.operation == Operation.ASSIGNMENT) {
			if (this.rightCommand instanceof OperationCommand) {
				final OperationCommand nestedOperation = (OperationCommand) this.rightCommand;

				if ((this.leftCommand.equals(nestedOperation.getLeftCommand()))
						&& (nestedOperation.getOperation().isSelfSignable())) {
					final String nestedOp = nestedOperation.getOperation().getDisplay();

					return this.leftCommand.getDisplay() + nestedOp.substring(0, nestedOp.length() - 1) + "= "
							+ nestedOperation.getRightCommand().getDisplay();
				}
			}
		}

		String display = this.leftCommand.getDisplay() + this.operation.getDisplay();
		final String rightDisplay = this.rightCommand.getDisplay();

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

	public AbstractCommand getLeftCommand() {
		return this.leftCommand;
	}

	public void setLeftCommand(AbstractCommand leftCommand) {
		this.leftCommand = leftCommand;
	}

	public AbstractCommand getRightCommand() {
		return this.rightCommand;
	}

	public void setRightCommand(AbstractCommand rightCommand) {
		this.rightCommand = rightCommand;
	}

	public Operation getOperation() {
		return this.operation;
	}

}
