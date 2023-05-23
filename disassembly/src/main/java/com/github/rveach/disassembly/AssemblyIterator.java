package com.github.rveach.disassembly;

import java.util.Iterator;
import java.util.List;

import com.github.rveach.disassembly.operations.NopCommand;

public class AssemblyIterator implements Iterator<AssemblyRepresentation> {

	private final List<AssemblyRepresentation> list;
	private int position;

	public AssemblyIterator(List<AssemblyRepresentation> list) {
		this.list = list;

		resetPosition();
	}

	public AssemblyIterator(AssemblyIterator o) {
		this.list = o.list;
		this.position = o.position;
	}

	@Override
	public AssemblyIterator clone() {
		return new AssemblyIterator(this);
	}

	public AssemblyIterator clone(int position) {
		final AssemblyIterator result = new AssemblyIterator(this);

		result.position = position;

		return result;
	}

	public void resetPosition() {
		this.position = 0;
	}

	@Override
	public boolean hasNext() {
		return this.position < this.list.size();
	}

	public boolean hasNext(int addition) {
		return (this.position + addition) < this.list.size();
	}

	@Override
	public AssemblyRepresentation next() {
		final AssemblyRepresentation result = this.list.get(this.position);

		this.position++;

		return result;
	}

	public void previous() {
		this.position--;
	}

	public void gotoPosition(int position) {
		this.position = position;
	}

	@Override
	public void remove() {
		previous();

		this.list.remove(this.position);
	}

	public void add(AssemblyRepresentation item) {
		this.list.add(this.position, item);

		this.position++;
	}

	public void add(int addition, AssemblyRepresentation item) {
		this.list.add((this.position + addition) - 1, item);

		if (addition <= 1) {
			this.position++;
		}
	}

	public AssemblyRepresentation get(int addition) {
		return this.list.get((this.position + addition) - 1);
	}

	public AssemblyRepresentation nextRepresentation() {
		while (true) {
			final AssemblyRepresentation item = next();

			if (!(item.getRepresentation() instanceof NopCommand)) {
				return item;
			}
		}
	}

	public void clear() {
		final AssemblyRepresentation item = this.list.get(this.position - 1);

		if (item.getAssemblySize() == 0) {
			remove();
		} else {
			item.setRepresentation(NopCommand.get());
		}
	}

	@Override
	public String toString() {
		return "position=" + this.position + "\ncurrent=" + (this.position == 0 ? "null" : get(0));
	}

	public List<AssemblyRepresentation> getList() {
		return this.list;
	}

	public int getPosition() {
		return this.position - 1;
	}

}
