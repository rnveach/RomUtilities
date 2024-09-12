package com.github.rveach.disassembly;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public final class Holder {
	private List<AssemblyRepresentation> assemblyRepresentations;

	public Holder() {
	}

	public AssemblyIterator getAssemblyRepresentationsIterator() {
		return new AssemblyIterator(this.assemblyRepresentations);
	}

	public List<AssemblyRepresentation> getAssemblyRepresentations() {
		return this.assemblyRepresentations;
	}

	public void setAssemblyRepresentations(List<AssemblyRepresentation> assemblyRepresentations) {
		this.assemblyRepresentations = assemblyRepresentations;
	}

	public void output(Writer writer, AssemblyType assemblyType) throws IOException {
		for (final AssemblyRepresentation item : this.assemblyRepresentations) {
			item.output(writer, assemblyType);
		}
	}
}
