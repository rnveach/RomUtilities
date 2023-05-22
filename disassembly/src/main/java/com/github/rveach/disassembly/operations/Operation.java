package com.github.rveach.disassembly.operations;

public enum Operation {
	ASSIGNMENT, //
	ADD_SIGNED, ADD_UNSIGNED, SUBTRACT_SIGNED, SUBTRACT_UNSIGNED, MULTIPLY_SIGNED, MULTIPLY_UNSIGNED, DIVIDE_SIGNED,
	DIVIDE_UNSIGNED, MOD, SHIFT_LEFT, SHIFT_RIGHT_ARITHMETIC /* >> */, SHIFT_RIGHT_LOGICAL /* >>> */, //
	AND, OR, XOR, //
	EQUAL, NOT_EQUAL, LESS_THAN_SIGNED, LESS_THAN_UNSIGNED, LESS_THAN_OR_EQUAL_SIGNED, LESS_THAN_OR_EQUAL_UNSIGNED,
	GREATER_THAN_SIGNED, GREATER_THAN_UNSIGNED, GREATER_THAN_OR_EQUAL_SIGNED, GREATER_THAN_OR_EQUAL_UNSIGNED, //
	INDEX_SIGNED, INDEX_UNSIGNED;

	String getDisplay() {
		switch (this) {
		case ADD_SIGNED:
		case ADD_UNSIGNED:
			return " + ";
		case AND:
			return " & ";
		case ASSIGNMENT:
			return " = ";
		case DIVIDE_SIGNED:
		case DIVIDE_UNSIGNED:
			return " / ";
		case EQUAL:
			return " == ";
		case GREATER_THAN_OR_EQUAL_SIGNED:
		case GREATER_THAN_OR_EQUAL_UNSIGNED:
			return " >= ";
		case GREATER_THAN_SIGNED:
		case GREATER_THAN_UNSIGNED:
			return " > ";
		case LESS_THAN_OR_EQUAL_SIGNED:
		case LESS_THAN_OR_EQUAL_UNSIGNED:
			return " <= ";
		case LESS_THAN_SIGNED:
		case LESS_THAN_UNSIGNED:
			return " < ";
		case MOD:
			return " % ";
		case MULTIPLY_SIGNED:
		case MULTIPLY_UNSIGNED:
			return " * ";
		case NOT_EQUAL:
			return " != ";
		case OR:
			return " | ";
		case SHIFT_LEFT:
			return " << ";
		case SHIFT_RIGHT_ARITHMETIC:
			return " >> ";
		case SHIFT_RIGHT_LOGICAL:
			return " >>> ";
		case SUBTRACT_SIGNED:
		case SUBTRACT_UNSIGNED:
			return " - ";
		case XOR:
			return " ^ ";
		case INDEX_SIGNED:
		case INDEX_UNSIGNED:
			return null;
		}

		throw new IllegalStateException("Unknown Operation");
	}

	public boolean isIndex() {
		switch (this) {
		case INDEX_SIGNED:
		case INDEX_UNSIGNED:
			return false;
		case ADD_SIGNED:
		case ADD_UNSIGNED:
		case AND:
		case ASSIGNMENT:
		case DIVIDE_SIGNED:
		case DIVIDE_UNSIGNED:
		case EQUAL:
		case GREATER_THAN_OR_EQUAL_SIGNED:
		case GREATER_THAN_OR_EQUAL_UNSIGNED:
		case GREATER_THAN_SIGNED:
		case GREATER_THAN_UNSIGNED:
		case LESS_THAN_OR_EQUAL_SIGNED:
		case LESS_THAN_OR_EQUAL_UNSIGNED:
		case LESS_THAN_SIGNED:
		case LESS_THAN_UNSIGNED:
		case MOD:
		case MULTIPLY_SIGNED:
		case MULTIPLY_UNSIGNED:
		case NOT_EQUAL:
		case OR:
		case SHIFT_LEFT:
		case SHIFT_RIGHT_ARITHMETIC:
		case SHIFT_RIGHT_LOGICAL:
		case SUBTRACT_SIGNED:
		case SUBTRACT_UNSIGNED:
		case XOR:
			return true;
		}

		throw new IllegalStateException("Unknown Operation");
	}

	public boolean isSelfSignable() {
		switch (this) {
		case ASSIGNMENT:
		case INDEX_SIGNED:
		case INDEX_UNSIGNED:
		case EQUAL:
		case GREATER_THAN_OR_EQUAL_SIGNED:
		case GREATER_THAN_OR_EQUAL_UNSIGNED:
		case GREATER_THAN_SIGNED:
		case GREATER_THAN_UNSIGNED:
		case LESS_THAN_OR_EQUAL_SIGNED:
		case LESS_THAN_OR_EQUAL_UNSIGNED:
		case LESS_THAN_SIGNED:
		case LESS_THAN_UNSIGNED:
		case NOT_EQUAL:
			return false;
		case ADD_SIGNED:
		case ADD_UNSIGNED:
		case AND:
		case DIVIDE_SIGNED:
		case DIVIDE_UNSIGNED:
		case MOD:
		case MULTIPLY_SIGNED:
		case MULTIPLY_UNSIGNED:
		case OR:
		case SHIFT_LEFT:
		case SHIFT_RIGHT_ARITHMETIC:
		case SHIFT_RIGHT_LOGICAL:
		case SUBTRACT_SIGNED:
		case SUBTRACT_UNSIGNED:
		case XOR:
			return true;
		}

		throw new IllegalStateException("Unknown Operation");
	}
}
