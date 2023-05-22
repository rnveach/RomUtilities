package com.github.rveach.disassembly.operations;

import java.util.List;

public final class NotCommand extends AbstractCommand {

	private AbstractCommand target;

	public NotCommand(AbstractCommand target) {
		this.target = target;
	}

	@Override
	public AbstractCommand deepClone() {
		return new NotCommand(this.target.deepClone());
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final NotCommand o = (NotCommand) other;

		return this.target.equals(o.target);
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		return this.target.contains(other);
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return false;
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		return this.target.isReadFrom(o);
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		if (this.target.equals(from)) {
			this.target = to;
		} else {
			this.target.swap(from, to);
		}
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		return this.target.getHardcodedLabels();
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		return this.target.getRegistersInvolved();
	}

	@Override
	public String getDisplay() {
		return "!(" + this.target.getDisplay() + ")";
	}

	public AbstractCommand getTarget() {
		return this.target;
	}

	public void setTarget(AbstractCommand target) {
		this.target = target;
	}
}
