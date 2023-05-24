package com.github.rveach.disassembly.routines.subroutines;

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
import com.github.rveach.disassembly.visitors.AbstractLineIteratorVisitor;

/**
 * This identifies if a register is used before the next assignment it finds.
 * Result is {@code true} if the register is used.
 */
public class IsRegisterUsedeBeforeAssignmentVisitor
		extends AbstractLineIteratorVisitor<IsRegisterUsedeBeforeAssignmentVisitor> {

	private boolean[] tracker;
	private boolean skipFirstCommand;
	private AbstractCommand register;

	private boolean result;

	private static final IsRegisterUsedeBeforeAssignmentVisitor INSTANCE = new IsRegisterUsedeBeforeAssignmentVisitor();

	private IsRegisterUsedeBeforeAssignmentVisitor() {
	}

	private IsRegisterUsedeBeforeAssignmentVisitor(IsRegisterUsedeBeforeAssignmentVisitor o) {
		this.tracker = o.tracker;
		this.skipFirstCommand = o.skipFirstCommand;
		this.register = o.register;
	}

	public static IsRegisterUsedeBeforeAssignmentVisitor get() {
		return INSTANCE;
	}

	@Override
	public IsRegisterUsedeBeforeAssignmentVisitor clone() {
		return new IsRegisterUsedeBeforeAssignmentVisitor(this);
	}

	@Override
	protected void beginInit(AssemblyIterator iterator) {
		this.result = false;
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
			this.skipFirstCommand = false;

			iterator.gotoPosition(iterator.findLabelPosition(((HardcodeValueCommand) location).getValue()));

			return true;
		} else {
			this.result = true;

			return false;
		}
	}

	@Override
	protected boolean visit(HardcodeValueCommand hardcodeValueCommand, AssemblyIterator iterator) {
		return true;
	}

	@Override
	protected boolean visit(IfCommand ifCommand, AssemblyIterator iterator) {
		return true;
	}

	private boolean visitAfter(IfCommand ifCommand, AssemblyIterator iterator) {
		// after checking register usage, follow 2 paths on ifs (goto and command after
		// if), and exit; if either path reports it is used, then report it back

		final AbstractCommand operation = ifCommand.getOperation();

		if (operation instanceof GotoCommand) {
			final AbstractCommand location = ((GotoCommand) operation).getLocation();

			if (location instanceof HardcodeValueCommand) {
				final int gotoLocation = iterator.findLabelPosition(((HardcodeValueCommand) location).getValue());

				if (clone().begin(iterator.clone(gotoLocation + 1)).getResult()) {
					this.result = true;

					return false;
				}

				// continue on following non-goto path
			} else {
				this.result = true;

				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean visit(JumpSubroutineCommand jumpSubroutineCommand, AssemblyIterator iterator) {
		// we can't go pass a call, unless we can examine it too, so we treat it as
		// unknown

		this.result = true;

		return false;
	}

	@Override
	protected boolean visit(LabelCommand labelCommand, AssemblyIterator iterator) {
		return true;
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
		if ((!this.skipFirstCommand) && (this.tracker[iterator.getPosition()])) {
			return false;
		}

		this.tracker[iterator.getPosition()] = true;
		return true;
	}

	@Override
	protected boolean visitAllAfter(AbstractCommand command, AssemblyIterator iterator) {
		if (this.skipFirstCommand) {
			this.skipFirstCommand = false;
		} else if (command.contains(this.register)) {
			// we exit if the register is used in some way; however, we say it isn't used if
			// it is only just re-assigned later and not read from

			this.result = true;

			if (command.isAssignedTo(this.register)) {
				if (!command.isReadFrom(this.register)) {
					this.result = false;
				}
			}

			return false;
		}

		if (command instanceof IfCommand) {
			if (!visitAfter((IfCommand) command, iterator)) {
				return false;
			}
		}

		return true;
	}

	public IsRegisterUsedeBeforeAssignmentVisitor setTracker(boolean[] tracker) {
		this.tracker = tracker;

		return this;
	}

	public IsRegisterUsedeBeforeAssignmentVisitor setSkipFirstCommand(boolean skipFirstCommand) {
		this.skipFirstCommand = skipFirstCommand;

		return this;
	}

	public IsRegisterUsedeBeforeAssignmentVisitor setRegister(AbstractCommand register) {
		this.register = register;

		return this;
	}

	public boolean getResult() {
		return this.result;
	}

}
