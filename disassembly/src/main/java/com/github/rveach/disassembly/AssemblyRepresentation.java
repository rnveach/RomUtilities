package com.github.rveach.disassembly;

import java.io.IOException;
import java.io.Writer;

import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.LabelCommand;
import com.github.rveach.disassembly.utils.Util;

public final class AssemblyRepresentation {

	private final int address;
	private final int assembly;
	private final int assemblySize;
	private final String assemblyDisplay;
	private AbstractCommand representation;
	private AbstractCommand saveRepresentation;

	public AssemblyRepresentation(AbstractCommand representation) {
		this.address = 0;
		this.assembly = 0;
		this.assemblySize = 0;
		this.assemblyDisplay = "";
		this.representation = representation;
		this.saveRepresentation = null;
	}

	public AssemblyRepresentation(int address, int assembly, int assemblySize, String display,
			AbstractCommand representation) {
		this.address = address;
		this.assembly = assembly;
		this.assemblySize = assemblySize;
		this.assemblyDisplay = display;
		this.representation = representation;
		this.saveRepresentation = null;
	}

	public void output(Writer writer, AssemblyType assemblyType) throws IOException {
		final boolean isLabel = this.representation instanceof LabelCommand;
		String finalDisplay = this.representation.getDisplay();
		final String display;

		if (isLabel) {
			display = this.representation.getDisplay();
			finalDisplay = "";
		} else {
			display = (this.saveRepresentation == null ? "" : this.saveRepresentation.getDisplay());
		}

		if (this.assemblySize == 0) {
			// completley wiped out, pretend it didn't happen
			if ((display.isEmpty()) && (finalDisplay.isEmpty())) {
				return;
			}

			writer.write("                     ");
		} else {
			writer.write(Util.hexRaw(this.address, assemblyType.getAddressSize() * 2));
			writer.write(' ');

			writeRawHex(writer);
		}

		if (!isLabel) {
			writer.write("     ");

			writer.write(this.assemblyDisplay);
		}

		// only assembly display, everything else was wiped
		if ((display.isEmpty()) && (finalDisplay.isEmpty())) {
			writer.write("\r\n");
			return;
		}

		// align to next column except labels which are placed into the assembly display
		// area
		if (!isLabel) {
			writeVariableSpaces(writer, 40 - this.assemblyDisplay.length());
		}

		if (!display.isEmpty()) {
			writer.write(display);
		}

		if (!finalDisplay.isEmpty()) {
			writeVariableSpaces(writer, 50 - display.length());

			writer.write(finalDisplay);
		}

		writer.write("\r\n");
	}

	private void writeRawHex(Writer writer) throws IOException {
		int temp = this.assembly;

		for (int i = 0; i < this.assemblySize; i++) {
			writer.write(' ');
			writer.write(Util.hexRaw(temp & 0xFF, 2));

			temp >>>= 8;
		}

		for (int i = this.assemblySize; i < 4; i++) {
			writer.write("   ");
		}
	}

	private static void writeVariableSpaces(Writer writer, int size) throws IOException {
		final int spaces = Math.max(size, 1);

		for (int i = 0; i < spaces; i++) {
			writer.write(' ');
		}
	}

	@Override
	public String toString() {
		return "address=" + Util.hex(this.address) + "\n" //
				+ "assembly=" + Util.hex(this.assembly) + "\n" //
				+ "assemblySize=" + this.assemblySize + "\n" //
				+ "assemblyDisplay=" + this.assemblyDisplay + "\n" //
				+ "representation=" + this.representation + "\n" //
				+ "saveRepresentation=" + this.saveRepresentation;
	}

	public AbstractCommand getRepresentation() {
		return this.representation;
	}

	public void setRepresentation(AbstractCommand representation) {
		this.representation = representation;
	}

	public AbstractCommand getSaveRepresentation() {
		return this.saveRepresentation;
	}

	public void setSaveRepresentation(AbstractCommand saveRepresentation) {
		this.saveRepresentation = saveRepresentation;
	}

	public int getAddress() {
		return this.address;
	}

	public int getAssembly() {
		return this.assembly;
	}

	public int getAssemblySize() {
		return this.assemblySize;
	}

	public String getAssemblyDisplay() {
		return this.assemblyDisplay;
	}

}
