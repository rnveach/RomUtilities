package com.github.rveach.disassembly.operations;

import java.util.ArrayList;
import java.util.List;

public final class IfCommand extends AbstractCommand {

	private AbstractCommand condition;
	private AbstractCommand operation;

	public IfCommand(AbstractCommand condition, AbstractCommand operation) {
		this.condition = condition;
		this.operation = operation;
	}

	@Override
	public AbstractCommand deepClone() {
		return new IfCommand(this.condition.deepClone(), this.operation.deepClone());
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final IfCommand o = (IfCommand) other;

		return (this.condition.equals(o.condition)) && (this.operation.equals(o.operation));
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		return this.condition.contains(other) || this.operation.contains(other);
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return this.operation.isAssignedTo(o);
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		return this.condition.isReadFrom(o) || this.operation.isReadFrom(o);
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		if (this.condition.equals(from)) {
			this.condition = to;
		} else {
			this.condition.swap(from, to);
		}

		if (this.operation.equals(from)) {
			this.operation = to;
		} else {
			this.operation.swap(from, to);
		}
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		final List<Integer> results = new ArrayList<>(this.condition.getHardcodedLabels());

		results.addAll(this.operation.getHardcodedLabels());

		return results;
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		final List<AbstractCommand> results = new ArrayList<>();

		results.addAll(this.condition.getRegistersInvolved());
		results.addAll(this.operation.getRegistersInvolved());

		return results;
	}

	@Override
	public String getDisplay() {
		if ((this.operation instanceof MultipleCommands)
				&& (((MultipleCommands) this.operation).getCommands().length > 1)) {
			return "if " + this.condition.getDisplay() + " { " + this.operation.getDisplay() + " }";
		}

		return "if " + this.condition.getDisplay() + " " + this.operation.getDisplay();
	}

	public AbstractCommand getCondition() {
		return this.condition;
	}

	public void setCondition(AbstractCommand condition) {
		this.condition = condition;
	}

	public AbstractCommand getOperation() {
		return this.operation;
	}

	public void setOperation(AbstractCommand operation) {
		this.operation = operation;
	}

}
