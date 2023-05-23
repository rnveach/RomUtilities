package com.github.rveach.disassembly.routines;

import java.util.List;

import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.IfCommand;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.routines.subroutines.SimplifyAddingNegativesVisitor;
import com.github.rveach.disassembly.routines.subroutines.SimplifyDoubleEqualityVisitor;
import com.github.rveach.disassembly.routines.subroutines.SimplifyNotVisitor;

public final class CSimplifyMore {

	/**
	 * This class is all about simplifying commands so that they are easier to read.
	 *
	 * Different ways to simplify:
	 *
	 * 1)
	 *
	 * if (!!A)
	 *
	 * ...turns into...
	 *
	 * if (A)
	 *
	 * 2)
	 *
	 * if (!(A {equality} B)
	 *
	 * ...turns into...
	 *
	 * if (A {not equality} B)
	 *
	 * 3)
	 *
	 * if ((A {equality} B) {equality} C)
	 *
	 * ...turns into...
	 *
	 * a)<br />
	 * if (A {equality} B)
	 *
	 * b)<br />
	 * if (A {not equality} B)
	 *
	 * Note: C must be a hardcoded value and must be either 0 ({@code false}) or 1
	 * ({@code true}).
	 *
	 * 4)
	 *
	 * A = B + -C<br />
	 * D = E - -F<br />
	 *
	 * ...turns into...
	 *
	 * A = B - C<br />
	 * D = E + F
	 */

	// TODO:
	// A = (B + 1) + 1

	private CSimplifyMore() {
	}

	public static boolean execute(Holder holder) {
		boolean result = false;

		final List<AssemblyRepresentation> representations = holder.getAssemblyRepresentations();

		for (final AssemblyRepresentation representation : representations) {
			final AbstractCommand command = representation.getRepresentation();

			if (command instanceof IfCommand) {
				result |= SimplifyNotVisitor.get().begin(command).getResult();
			}
			if ((command instanceof IfCommand) || (command instanceof OperationCommand)) {
				result |= SimplifyDoubleEqualityVisitor.get().begin(command).getResult();

				result |= SimplifyAddingNegativesVisitor.get().begin(command).getResult();
			}
		}

		return result;
	}

}
