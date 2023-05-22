package com.github.rveach.disassembly.operations;

import java.util.Arrays;
import java.util.List;

public class RegisterCommand extends AbstractCommand {

	private final String register;

	public RegisterCommand(String register) {
		this.register = register;
	}

	@Override
	public AbstractCommand deepClone() {
		return new RegisterCommand(this.register);
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final RegisterCommand o = (RegisterCommand) other;

		return this.register.equals(o.register);
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
		return Arrays.asList(this);
	}

	@Override
	public String getDisplay() {
		return this.register;
	}

	public String getRegister() {
		return this.register;
	}

}
