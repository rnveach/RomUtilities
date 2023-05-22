package com.github.rveach.disassembly;

public enum AssemblyType {
	PSX;

	public int getAddressSize() {
		switch (this) {
		case PSX:
			return 4;
		}

		throw new IllegalStateException("Unknown Type");
	}

	public int getRegisterSize() {
		switch (this) {
		case PSX:
			return 4;
		}

		throw new IllegalStateException("Unknown Type");
	}
}
