package com.github.rveach.disassembly.routines.subroutines;

import java.util.Arrays;
import java.util.List;

import com.github.rveach.disassembly.AssemblyIterator;
import com.github.rveach.disassembly.AssemblyRepresentation;
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
 * This merges an assignment with it's first usage, if possible. Result is
 * {@code true} if a change was made.
 */
public final class SimplifyRegisterAssignmentVisitor
		extends AbstractLineIteratorVisitor<SimplifyRegisterAssignmentVisitor> {

	private boolean[] tracker;

	private AssemblyRepresentation originalRepresentation;
	private AbstractCommand originalCommand;

	private AbstractCommand originalAssignment;
	private AbstractCommand originalAssignnee;
	private List<AbstractCommand> originalAssignneeRegisters;

	private boolean result;

	private static final SimplifyRegisterAssignmentVisitor INSTANCE = new SimplifyRegisterAssignmentVisitor();

	private SimplifyRegisterAssignmentVisitor() {
	}

	public static SimplifyRegisterAssignmentVisitor get() {
		return INSTANCE;
	}

	@Override
	protected void beginInit(AssemblyIterator iterator) {
		this.result = false;

		if (this.tracker == null) {
			this.tracker = new boolean[iterator.getList().size()];
		}

		Arrays.fill(this.tracker, false);

		this.tracker[iterator.getPosition()] = true;

		this.originalAssignment = ((OperationCommand) this.originalCommand).getLeftOperand();
		this.originalAssignnee = ((OperationCommand) this.originalCommand).getRightOperand();
		this.originalAssignneeRegisters = this.originalAssignnee.getRegistersInvolved();
	}

	@Override
	protected void end(AssemblyIterator iterator, boolean reachedEndOfIterator) {
		// if we hit the end, assume everything is used as we don't know how the
		// variables will be used in other functions
		if (reachedEndOfIterator) {
			this.result = true;
		}
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
		// follow gotos, as long as they are hardcoded values, otherwise we treat it as
		// unknown

		final AbstractCommand location = gotoCommand.getLocation();

		if (location instanceof HardcodeValueCommand) {
			final int gotoLocation = iterator.findLabelPosition(((HardcodeValueCommand) location).getValue());

			// TODO: we could allow move forward only if the label had 1 goto

			if (!isUsedAgainBeforeAssignment(iterator.clone(gotoLocation + 1), this.originalAssignment, false)) {
				this.originalRepresentation.setRepresentation(NopCommand.get());
			}
		}

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
		// follow gotos, as long as they are hardcoded values, otherwise we treat it as
		// unknown

		final GotoCommand gotoCommand = (GotoCommand) command.getOperation();
		final AbstractCommand location = gotoCommand.getLocation();

		if (location instanceof HardcodeValueCommand) {
			final int gotoLocation = iterator.findLabelPosition(((HardcodeValueCommand) location).getValue());

			// if command isn't used before assignment on the goto, assume it is safe to
			// continue through the bottom

			if (!isUsedAgainBeforeAssignment(iterator.clone(gotoLocation + 1), this.originalAssignment, false)) {
				return true;
			}
		}

		// otherwise, it is not safe to pass the if command

		return false;
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

				// TODO: don't do a swap of 2+ instances
				// combine the contents of the next command with the result of the previous
				// assignment if possible
				((OperationCommand) command).swapRightOnly(this.originalAssignment, this.originalAssignnee);

				// and wipe the original
				this.originalRepresentation.setRepresentation(NopCommand.get());
				this.result = true;

				return false;
			}
		}

		// command uses the previous assignment but it is not re-assigning to it
		if (command.contains(this.originalAssignment)) {
			final boolean isIfCommandNext = command instanceof IfCommand;

			if (!isUsedAgainBeforeAssignment(iterator.clone(iterator.getPosition() + (isIfCommandNext ? 0 : 1)),
					this.originalAssignment, isIfCommandNext)) {
				// original is getting wiped by next command

				// TODO: don't do a swap of 2+ instances
				// combine the contents of the next command with the result of the previous
				// assignment if possible
				if (isIfCommandNext) {
					command.swap(this.originalAssignment, this.originalAssignnee);
				} else {
					// TODO: is this safe?
					((OperationCommand) command).swapRightOnly(this.originalAssignment, this.originalAssignnee);
				}

				// and wipe the original
				this.originalRepresentation.setRepresentation(NopCommand.get());
				this.result = true;
			}

			return false;
		}

		// one of the variable in the assignee is changing after the assignment, we
		// can't do any swaps as this would break the original assignment
		if (command.isAssignedToOneOf(this.originalAssignneeRegisters)) {
			return false;
		}

		if (command instanceof IfCommand) {
			if (!visitAfter((IfCommand) command, iterator)) {
				return false;
			}
		}

		return true;
	}

	private boolean isUsedAgainBeforeAssignment(AssemblyIterator iterator, AbstractCommand register,
			boolean skipFirstCommand) {
		return IsRegisterUsedeBeforeAssignmentVisitor.get()//
				.setTracker(this.tracker).setRegister(register).setSkipFirstCommand(skipFirstCommand)//
				.begin(iterator).getResult();
	}

	public SimplifyRegisterAssignmentVisitor setOriginalRepresentation(AssemblyRepresentation originalRepresentation) {
		this.originalRepresentation = originalRepresentation;

		return this;
	}

	public SimplifyRegisterAssignmentVisitor setOriginalCommand(AbstractCommand originalCommand) {
		this.originalCommand = originalCommand;

		return this;
	}

	public boolean getResult() {
		return this.result;
	}

}
