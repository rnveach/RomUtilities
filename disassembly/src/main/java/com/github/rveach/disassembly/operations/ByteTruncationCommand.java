package com.github.rveach.disassembly.operations;

import java.util.List;

public final class ByteTruncationCommand extends AbstractCommand {

	private int byteSize;
	private AbstractCommand command;

	public ByteTruncationCommand(int byteSize, AbstractCommand command) {
		this.byteSize = byteSize;
		this.command = command;
	}

	@Override
	public AbstractCommand deepClone() {
		return new ByteTruncationCommand(this.byteSize, this.command.deepClone());
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final ByteTruncationCommand o = (ByteTruncationCommand) other;

		return (this.byteSize == o.byteSize) && (this.command.equals(o.command));
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		return this.command.contains(other);
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return false;
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		return this.command.isReadFrom(o);
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		if (this.command.equals(from)) {
			this.command = to;
		} else {
			this.command.swap(from, to);
		}
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		return this.command.getHardcodedLabels();
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		return this.command.getRegistersInvolved();
	}

	@Override
	public String getDisplay() {
		final String sizeDisplay;

		switch (this.byteSize) {
		case 1:
			sizeDisplay = "(byte) ";
			break;
		case 2:
			sizeDisplay = "(short) ";
			break;
		case 4:
			sizeDisplay = "(long) ";
			break;
		default:
			throw new IllegalStateException("Not Implemented Byte Size");
		}

		return "(" + sizeDisplay + this.command.getDisplay() + ")";
	}

	public int getByteSize() {
		return this.byteSize;
	}

	public void setByteSize(int byteSize) {
		this.byteSize = byteSize;
	}

	public AbstractCommand getCommand() {
		return this.command;
	}

	public void setCommand(AbstractCommand command) {
		this.command = command;
	}

}
