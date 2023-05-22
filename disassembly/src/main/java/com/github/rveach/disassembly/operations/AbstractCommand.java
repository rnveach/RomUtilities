package com.github.rveach.disassembly.operations;

import java.util.List;

public abstract class AbstractCommand {

	public abstract AbstractCommand deepClone();

	public final boolean equals(AbstractCommand o) {
		if ((o == null) || !getClass().equals(o.getClass())) {
			return false;
		}

		return equalsSpecific(o);
	}

	protected abstract boolean equalsSpecific(AbstractCommand other);

	public final boolean contains(AbstractCommand o) {
		if (equals(o)) {
			return true;
		}

		return containsSpecific(o);
	}

	protected abstract boolean containsSpecific(AbstractCommand other);

	public boolean containsOneOf(List<AbstractCommand> o) {
		for (final AbstractCommand other : o) {
			if (contains(other)) {
				return true;
			}
		}

		return false;
	}

	public abstract boolean isAssignedTo(AbstractCommand o);

	public boolean isAssignedToOneOf(List<AbstractCommand> o) {
		for (final AbstractCommand other : o) {
			if (isAssignedTo(other)) {
				return true;
			}
		}

		return false;
	}

	public final boolean isReadFrom(AbstractCommand o) {
		if (equals(o)) {
			return true;
		}

		return isReadFromSpecific(o);
	}

	protected abstract boolean isReadFromSpecific(AbstractCommand o);

	public abstract void swap(AbstractCommand from, AbstractCommand to);

	public abstract List<Integer> getHardcodedLabels();

	public abstract List<AbstractCommand> getRegistersInvolved();

	public abstract String getDisplay();

	@Override
	public String toString() {
		return getDisplay();
	}

}
