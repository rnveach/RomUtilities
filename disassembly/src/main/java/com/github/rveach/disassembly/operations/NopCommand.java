package com.github.rveach.disassembly.operations;

import java.util.Arrays;
import java.util.List;

public final class NopCommand extends AbstractCommand {

	public static final NopCommand INSTANCE = new NopCommand();

	private NopCommand() {
	}

	public static NopCommand get() {
		return INSTANCE;
	}

	@Override
	public AbstractCommand deepClone() {
		return INSTANCE;
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		return true;
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
	public String getDisplay() {
		return "";
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		return Arrays.asList();
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		return Arrays.asList();
	}

}
