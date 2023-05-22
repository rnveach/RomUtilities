package com.github.rveach.disassembly.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiRegisterCommand extends AbstractCommand {

	private final AbstractCommand[] registers;

	public MultiRegisterCommand(AbstractCommand... registers) {
		this.registers = registers;
	}

	@Override
	public AbstractCommand deepClone() {
		final AbstractCommand[] deepRegisters = this.registers.clone();

		for (int i = 0; i < deepRegisters.length; i++) {
			deepRegisters[i] = deepRegisters[i].deepClone();
		}

		return new MultiRegisterCommand(deepRegisters);
	}

	@Override
	protected boolean equalsSpecific(AbstractCommand other) {
		final MultiRegisterCommand o = (MultiRegisterCommand) other;

		return Arrays.deepEquals(this.registers, o.registers);
	}

	@Override
	public boolean containsSpecific(AbstractCommand other) {
		boolean result = false;

		for (final AbstractCommand register : this.registers) {
			result = register.contains(other);

			if (result) {
				break;
			}
		}

		return result;
	}

	@Override
	public boolean isAssignedTo(AbstractCommand o) {
		return false;
	}

	@Override
	protected boolean isReadFromSpecific(AbstractCommand o) {
		boolean result = false;

		for (final AbstractCommand register : this.registers) {
			result = register.isReadFrom(o);

			if (result) {
				break;
			}
		}

		return result;
	}

	@Override
	public void swap(AbstractCommand from, AbstractCommand to) {
		for (int i = 0; i < this.registers.length; i++) {
			final AbstractCommand register = this.registers[i];

			if (register.equals(from)) {
				this.registers[i] = to;
			} else {
				register.swap(from, to);
			}
		}
	}

	@Override
	public List<Integer> getHardcodedLabels() {
		final List<Integer> results = new ArrayList<>();

		for (final AbstractCommand register : this.registers) {
			results.addAll(register.getHardcodedLabels());
		}

		return results;
	}

	@Override
	public List<AbstractCommand> getRegistersInvolved() {
		final List<AbstractCommand> results = new ArrayList<>();

		for (final AbstractCommand register : this.registers) {
			results.addAll(register.getRegistersInvolved());
		}

		return results;
	}

	@Override
	public String getDisplay() {
		String result = "";
		boolean first = true;

		for (final AbstractCommand register : this.registers) {
			if (first) {
				first = false;
			} else {
				result += ':';
			}

			result += register.getDisplay();
		}

		return result;
	}

	public AbstractCommand[] getRegisters() {
		return this.registers;
	}

}
