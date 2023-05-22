package com.github.rveach.disassembly.operations;

import java.util.ArrayList;
import java.util.List;

public final class JumpSubroutineCommand extends AbstractCommand {

	private AbstractCommand target;
	private AbstractCommand returnLocation;

	public JumpSubroutineCommand(AbstractCommand target, AbstractCommand returnLocation) {
		this.target = target;
		this.returnLocation = returnLocation;
	}

	@Override
	public AbstractCommand deepClone() {
		return new JumpSubroutineCommand(this.target.deepClone(), this.returnLocation.deepClone());
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final JumpSubroutineCommand o = (JumpSubroutineCommand) other;

		return this.target.equals(o.target) && this.returnLocation.equals(o.returnLocation);
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		return this.target.contains(other) || this.returnLocation.contains(other);
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		if (this.target.equals(from)) {
			this.target = to;
		} else {
			this.target.swap(from, to);
		}

		if (this.returnLocation.equals(from)) {
			this.returnLocation = to;
		} else {
			this.returnLocation.swap(from, to);
		}
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return this.returnLocation.equals(o);
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		return this.target.isReadFrom(o);
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		final List<Integer> results = new ArrayList<>(this.target.getHardcodedLabels());

		results.addAll(this.target.getHardcodedLabels());
		results.addAll(this.returnLocation.getHardcodedLabels());

		return results;
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		final List<AbstractCommand> results = new ArrayList<>();

		results.addAll(this.target.getRegistersInvolved());
		results.addAll(this.returnLocation.getRegistersInvolved());

		return results;
	}

	@Override
	public String getDisplay() {
		return this.returnLocation.getDisplay() + " <- " + this.target.getDisplay();
	}

	public AbstractCommand getTarget() {
		return this.target;
	}

	public void setTarget(AbstractCommand target) {
		this.target = target;
	}

	public AbstractCommand getReturnLocation() {
		return this.returnLocation;
	}

	public void setReturnLocation(AbstractCommand returnLocation) {
		this.returnLocation = returnLocation;
	}

}
