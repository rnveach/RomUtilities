package com.github.rveach.disassembly.operations;

import java.util.List;

public final class MultipleCommands extends AbstractCommand {

	private final AbstractCommand[] commands;

	public MultipleCommands(AbstractCommand... commands) {
		this.commands = commands;
	}

	@Override
	public AbstractCommand deepClone() {
		final AbstractCommand[] deepCommands = this.commands.clone();

		for (int i = 0; i < deepCommands.length; i++) {
			deepCommands[i] = deepCommands[i].deepClone();
		}

		return new MultipleCommands(deepCommands);
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		throw new IllegalArgumentException("Not implemented");
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		throw new IllegalArgumentException("Not implemented");
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		throw new IllegalArgumentException("Not implemented");
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		throw new IllegalArgumentException("Not implemented");
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		throw new IllegalArgumentException("Not implemented");
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		throw new IllegalArgumentException("Not implemented");
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		throw new IllegalArgumentException("Not implemented");
	}

	@Override
	public String getDisplay() {
		String result = "";
		boolean first = true;

		for (final AbstractCommand command : this.commands) {
			if (first) {
				first = false;
			} else {
				result += "; ";
			}

			result += command.getDisplay();
		}

		return result;
	}

	public AbstractCommand[] getCommands() {
		return this.commands;
	}

}
