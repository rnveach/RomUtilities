package com.github.rveach.disassembly.routines.subroutines;

import java.util.Arrays;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.ByteTruncationCommand;
import com.github.rveach.disassembly.operations.CustomCallCommand;
import com.github.rveach.disassembly.operations.GotoCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.IfCommand;
import com.github.rveach.disassembly.operations.JumpSubroutineCommand;
import com.github.rveach.disassembly.operations.LabelCommand;
import com.github.rveach.disassembly.operations.MultiRegisterCommand;
import com.github.rveach.disassembly.operations.MultipleCommands;
import com.github.rveach.disassembly.operations.NopCommand;
import com.github.rveach.disassembly.operations.NotCommand;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;
import com.github.rveach.disassembly.utils.AssemblyUtil;
import com.github.rveach.disassembly.visitors.AbstractLineIteratorVisitor;

/**
 * This merges an assignment that has a hardcoded value with all usages, if
 * possible. Result is {@code true} if a change was made.
 */
public final class SimplifyHardcodedRegisterAssignmentVisitor
		extends AbstractLineIteratorVisitor<SimplifyHardcodedRegisterAssignmentVisitor> {

	private boolean[] tracker;

	private AbstractCommand originalAssignment;
	private AbstractCommand originalAssignnee;

	private boolean result;

	private static final SimplifyHardcodedRegisterAssignmentVisitor INSTANCE = new SimplifyHardcodedRegisterAssignmentVisitor();

	private SimplifyHardcodedRegisterAssignmentVisitor() {
	}

	private SimplifyHardcodedRegisterAssignmentVisitor(SimplifyHardcodedRegisterAssignmentVisitor o) {
		this.tracker = o.tracker;
		this.originalAssignment = o.originalAssignment;
		this.originalAssignnee = o.originalAssignnee;
	}

	public static SimplifyHardcodedRegisterAssignmentVisitor get() {
		return INSTANCE;
	}

	// to reset testing
	public void reset() {
		this.tracker = null;
	}

	@Override
	public SimplifyHardcodedRegisterAssignmentVisitor clone() {
		return new SimplifyHardcodedRegisterAssignmentVisitor(this);
	}

	@Override
	protected void beginInit(AssemblyIterator iterator) {
		this.result = false;

		if (this.tracker == null) {
			this.tracker = new boolean[iterator.getList().size()];
		}

		Arrays.fill(this.tracker, false);

		this.tracker[iterator.getPosition()] = true;
	}

	@Override
	protected boolean visit(ByteTruncationCommand byteTruncationCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(CustomCallCommand customCallCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(GotoCommand gotoCommand, AssemblyIterator iterator) {
		// follow gotos, as long as they are hardcoded values and jump to a location
		// which can't fall through from above

		final AbstractCommand location = gotoCommand.getLocation();

		if (location instanceof HardcodeValueCommand) {
			final int gotoLocation = iterator.findLabelPosition(((HardcodeValueCommand) location).getValue());
			int beforeLabelPosition = gotoLocation - 1;

			while (iterator.getAt(beforeLabelPosition).getRepresentation() instanceof NopCommand) {
				beforeLabelPosition--;
			}

			if (iterator.getAt(beforeLabelPosition).getRepresentation() instanceof GotoCommand) {
				iterator.gotoPosition(gotoLocation + 1);

				return true;
			}
		}

		// otherwise, it is not safe to pass the goto

		return false;
	}

	@Override
	protected boolean visit(HardcodeValueCommand hardcodeValueCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(IfCommand ifCommand, AssemblyIterator iterator) {
		return true;
	}

	private boolean visitAfter(IfCommand command, AssemblyIterator iterator) {
		final GotoCommand gotoCommand = (GotoCommand) command.getOperation();

		// if the goto takes on the position to go to, then a new clone is used to
		// continue on here

		if (visit(gotoCommand, iterator)) {
			if (clone().begin(iterator.clone()).getResult()) {
				this.result = true;
			}
		}

		return true;
	}

	@Override
	protected boolean visit(JumpSubroutineCommand jumpSubroutineCommand, AssemblyIterator iterator) {
		// we can't go pass a call, unless we can examine it too, so we treat it as
		// unknown
		return false;
	}

	@Override
	protected boolean visit(LabelCommand labelCommand, AssemblyIterator iterator) {
		// TODO: implementation
		return false;
	}

	@Override
	protected boolean visit(MultipleCommands multipleCommands, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(MultiRegisterCommand multiRegisterCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(NopCommand nopCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(NotCommand notCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(OperationCommand operationCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(RegisterCommand registerCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visitAllBefore(AbstractCommand command, AssemblyIterator iterator) {
		if (this.tracker[iterator.getPosition()]) {
			return false;
		}

		this.tracker[iterator.getPosition()] = true;
		return true;
	}

	@Override
	protected boolean visitAllAfter(AbstractCommand command, AssemblyIterator iterator) {
		if (AssemblyUtil.isRegisterAssignment(command)) {
			// check if next register assignment is same as original

			if (command.isAssignedTo(this.originalAssignment)) {
				// original is getting wiped by next command
				return false;
			}
		}

		// apply swap to all instances of the assignment
		command.swap(this.originalAssignment, this.originalAssignnee);

		if (command instanceof IfCommand) {
			if (!visitAfter((IfCommand) command, iterator)) {
				return false;
			}
		}

		return true;
	}

	public SimplifyHardcodedRegisterAssignmentVisitor setOriginalCommand(AbstractCommand originalCommand) {
		this.originalAssignment = ((OperationCommand) originalCommand).getLeftOperand();
		this.originalAssignnee = ((OperationCommand) originalCommand).getRightOperand();

		return this;
	}

	public boolean getResult() {
		return this.result;
	}

}
