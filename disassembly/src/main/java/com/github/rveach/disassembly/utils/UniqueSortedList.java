package com.github.rveach.disassembly.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class UniqueSortedList<E extends Comparable<? super E>> extends ArrayList<E> {
	private static final long serialVersionUID = -4134410888037473596L;

	@Override
	@SuppressWarnings("unchecked")
	public int indexOf(Object o) {
		if ((o == null) || (o instanceof Comparable)) {
			return indexOf((E) o);
		}

		throw new UnsupportedOperationException();
	}

	public int indexOf(E e) {
		final int pos = Collections.binarySearch(this, e);

		if (pos >= 0) {
			return pos;
		}
		return -1;
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(E e) {
		final int pos = Collections.binarySearch(this, e);

		if (pos < 0) {
			super.add(-pos - 1, e);
			return true;
		}

		return false;
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		if ((o == null) || (o instanceof Comparable)) {
			return remove((E) o);
		}

		throw new UnsupportedOperationException();
	}

	public boolean remove(E o) {
		final int pos = Collections.binarySearch(this, o);

		if (pos >= 0) {
			super.remove(pos);
			return true;
		}

		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;

		for (final E e : c) {
			if (add(e)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
}
