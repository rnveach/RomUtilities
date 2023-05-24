package com.github.rveach.disassembly.utils;

import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.MultiRegisterCommand;
import com.github.rveach.disassembly.operations.Operation;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;

public final class AssemblyUtil {

	private AssemblyUtil() {
	}

	public static boolean isRegisterAssignment(AbstractCommand command) {
		if (command instanceof OperationCommand) {
			final OperationCommand operation = ((OperationCommand) command);

			if ((operation.getOperation() == Operation.ASSIGNMENT)
					&& ((operation.getLeftOperand() instanceof RegisterCommand)
							|| (operation.getLeftOperand() instanceof MultiRegisterCommand))) {
				return true;
			}
		}

		return false;
	}

}
