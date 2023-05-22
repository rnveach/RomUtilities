package com.github.rveach.disassembly.operations;

import java.util.Arrays;
import java.util.List;

public final class CustomCallCommand extends AbstractCommand {

	private final String name;
	private final int parameter;

	public CustomCallCommand(String name, int parameter) {
		this.name = name;
		this.parameter = parameter;
	}

	@Override
	public AbstractCommand deepClone() {
		return new CustomCallCommand(this.name, this.parameter);
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final CustomCallCommand o = (CustomCallCommand) other;

		return (this.name.equals(o.name)) && (this.parameter == o.parameter);
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
		return this.name + "(" + this.parameter + ")";
	}

	public String getName() {
		return this.name;
	}

	public int getParameter() {
		return this.parameter;
	}

}
