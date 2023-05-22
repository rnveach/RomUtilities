package com.github.rveach.disassembly;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public final class Holder {
	private List<AssemblyRepresentation> assemblyRepresentations;

	public Holder() {
	}

	public List<AssemblyRepresentation> getAssemblyRepresentations() {
		return this.assemblyRepresentations;
	}

	public void setAssemblyRepresentations(List<AssemblyRepresentation> assemblyRepresentations) {
		this.assemblyRepresentations = assemblyRepresentations;
	}

	public void output(FileWriter writer, AssemblyType assemblyType) throws IOException {
		for (final AssemblyRepresentation item : this.assemblyRepresentations) {
			item.output(writer, assemblyType);
		}
	}
}
