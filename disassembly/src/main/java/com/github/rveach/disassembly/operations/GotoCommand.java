package com.github.rveach.disassembly.operations;

import java.util.Arrays;
import java.util.List;

import com.github.rveach.disassembly.utils.Util;

public final class GotoCommand extends AbstractCommand {

	private AbstractCommand location;

	public GotoCommand(AbstractCommand location) {
		this.location = location;
	}

	@Override
	public AbstractCommand deepClone() {
		return new GotoCommand(this.location.deepClone());
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final GotoCommand o = (GotoCommand) other;

		return this.location.equals(o.location);
	}

	@Override
	public boolean containsSpecific(AbstractCommand o) {
		return this.location.contains(o);
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return false;
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		return this.location.isReadFrom(o);
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		if (this.location.equals(from)) {
			this.location = to;
		} else {
			this.location.swap(from, to);
		}
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		if (this.location instanceof HardcodeValueCommand) {
			return Arrays.asList(((HardcodeValueCommand) this.location).getValue());
		}

		return Arrays.asList();
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		return this.location.getRegistersInvolved();
	}

	@Override
	public String getDisplay() {
		if (this.location instanceof HardcodeValueCommand) {
			return "goto LAB_" + Util.hexRaw(((HardcodeValueCommand) this.location).getValue());
		}

		return "goto " + this.location.getDisplay();
	}

	public AbstractCommand getLocation() {
		return this.location;
	}

	public void setLocation(AbstractCommand location) {
		this.location = location;
	}

}
