package com.github.rveach.disassembly.operations;

import java.util.Arrays;
import java.util.List;

import com.github.rveach.disassembly.utils.Util;

public final class LabelCommand extends AbstractCommand {

	private final int location;

	public LabelCommand(int location) {
		this.location = location;
	}

	@Override
	public AbstractCommand deepClone() {
		return new LabelCommand(this.location);
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final LabelCommand o = (LabelCommand) other;

		return this.location == o.location;
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		return false;
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return false;
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		return false;
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		return Arrays.asList();
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		return Arrays.asList();
	}

	@Override
	public String getDisplay() {
		return "LAB_" + Util.hexRaw(this.location);
	}

	public int getLocation() {
		return this.location;
	}

}
