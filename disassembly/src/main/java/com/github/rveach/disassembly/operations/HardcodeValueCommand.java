package com.github.rveach.disassembly.operations;

import java.util.Arrays;
import java.util.List;

import com.github.rveach.disassembly.utils.Util;

public class HardcodeValueCommand extends AbstractCommand {

	private int value;

	public HardcodeValueCommand(int value) {
		this.value = value;
	}

	@Override
	public AbstractCommand deepClone() {
		return new HardcodeValueCommand(this.value);
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final HardcodeValueCommand o = (HardcodeValueCommand) other;

		return this.value == o.value;
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
		if ((this.value <= 9) && (this.value >= -9)) {
			return Integer.toString(this.value);
		}

		return Util.hex(this.value);
	}

	public int getValue() {
		return this.value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
