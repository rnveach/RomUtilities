package com.github.rveach.disassembly.routines;

import java.util.ArrayList;
import java.util.List;

import com.github.rveach.disassembly.AssemblyRepresentation;
import com.github.rveach.disassembly.Holder;
import com.github.rveach.disassembly.operations.AbstractCommand;
import com.github.rveach.disassembly.operations.ByteTruncationCommand;
import com.github.rveach.disassembly.operations.CustomCallCommand;
import com.github.rveach.disassembly.operations.GotoCommand;
import com.github.rveach.disassembly.operations.HardcodeValueCommand;
import com.github.rveach.disassembly.operations.IfCommand;
import com.github.rveach.disassembly.operations.JumpSubroutineCommand;
import com.github.rveach.disassembly.operations.MultiRegisterCommand;
import com.github.rveach.disassembly.operations.NopCommand;
import com.github.rveach.disassembly.operations.NotCommand;
import com.github.rveach.disassembly.operations.Operation;
import com.github.rveach.disassembly.operations.OperationCommand;
import com.github.rveach.disassembly.operations.RegisterCommand;
import com.github.rveach.disassembly.utils.Util;

public final class PsxAssembly {

	public static final String REGISTERS[] = { //
			"$r0", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3", "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6",
			"$t7", "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7", "$t8", "$t9", "$k0", "$k1", "$gp", "$sp",
			"$fp", "$ra" //
	};

	private static final String HI_REGISTER = "$HI";
	private static final String LO_REGISTER = "$LO";

	private PsxAssembly() {
	}

	public static void execute(Holder holder, int startPosition, int offset, byte[] buffer) {
		final List<AssemblyRepresentation> representations = new ArrayList<>();
		int address = startPosition + offset;

		for (int position = 0; position < buffer.length; position += 4) {
			final int assembly = Util.read32LE(buffer, position);

			final AssemblyRepresentation representation = identify(address, assembly);

			representations.add(representation);

			address += 4;
		}

		if ((buffer.length % 4) != 0) {
			// TODO: ;*****Incorrect ending spot entered!
		}

		holder.setAssemblyRepresentations(representations);
	}

	private static AssemblyRepresentation identify(int address, int assembly) {
		if (assembly == 0) {
			return new AssemblyRepresentation(address, assembly, 4, "nop", NopCommand.get());
		}

		String rt;
		String rd;
		String rs;
		int parameter;
		int addr;
		int branch;

		switch (assembly >>> 26) {
		case 0: // Special
			switch (assembly & 0x3F) {
			case 0: // SLL
				return getRdRtSa(address, assembly, "sll   ", Operation.SHIFT_LEFT);
			case 2: // SRL
				return getRdRtSa(address, assembly, "srl   ", Operation.SHIFT_RIGHT_LOGICAL);
			case 3: // SRA
				return getRdRtSa(address, assembly, "sra   ", Operation.SHIFT_RIGHT_ARITHMETIC);
			case 4: // SLLV
				// TODO: technically RS is & 0x1f
				return getRdRtRs(address, assembly, "sllv  ", Operation.SHIFT_LEFT);
			case 6: // SRLV
				// TODO: technically RS is & 0x1f
				return getRdRtRs(address, assembly, "srlv  ", Operation.SHIFT_RIGHT_LOGICAL);
			case 7: // SRAV
				// TODO: technically RS is & 0x1f
				return getRdRtRs(address, assembly, "srav  ", Operation.SHIFT_RIGHT_ARITHMETIC);
			case 8: // JR
				rs = REGISTERS[rs(assembly)];

				return getCommand(address, assembly, "jr    " + rs, new GotoCommand(getRegister(rs)));
			case 9: // JALR
				rs = REGISTERS[rs(assembly)];
				rd = REGISTERS[rd(assembly)];

				return getCommand(address, assembly, concat("jalr  ", rs, rd),
						new JumpSubroutineCommand(getRegister(rs), getRegister(rd)));
			case 12: // SYSCALL
				parameter = assembly & 0xFFFFF;

				return getCustomCall(address, assembly, "syscall ", parameter, "syscall");
			case 13: // BREAK
				parameter = assembly & 0xFFFFF;

				return getCustomCall(address, assembly, "break ", parameter, "break");
			case 16: // MFHI
				rd = REGISTERS[rd(assembly)];

				return getCommand(address, assembly, "mfhi  " + rd, getAssignment(rd, getRegister(HI_REGISTER)));
			case 17: // MTHI
				rs = REGISTERS[rs(assembly)];

				return getCommand(address, assembly, "mthi  " + rs, getAssignment(HI_REGISTER, getRegister(rs)));
			case 18: // MFLO
				rd = REGISTERS[rd(assembly)];

				return getCommand(address, assembly, "mflo  " + rd, getAssignment(rd, getRegister(LO_REGISTER)));
			case 19: // MTLO
				rs = REGISTERS[rs(assembly)];

				return getCommand(address, assembly, "mtlo  " + rs, getAssignment(LO_REGISTER, getRegister(rs)));
			case 24: // MULT
				rs = REGISTERS[rs(assembly)];
				rt = REGISTERS[rt(assembly)];

				return getCommand(address, assembly, concat("mult  ", rs, rt),
						getAssignment(new MultiRegisterCommand(getRegister(HI_REGISTER), getRegister(LO_REGISTER)),
								getOperation(rs, Operation.MULTIPLY_SIGNED, rt)));
			case 25: // MULTU
				rs = REGISTERS[rs(assembly)];
				rt = REGISTERS[rt(assembly)];

				return getCommand(address, assembly, concat("multu ", rs, rt),
						getAssignment(new MultiRegisterCommand(getRegister(HI_REGISTER), getRegister(LO_REGISTER)),
								getOperation(rs, Operation.MULTIPLY_UNSIGNED, rt)));
			case 26: // DIV
				rs = REGISTERS[rs(assembly)];
				rt = REGISTERS[rt(assembly)];

				// alernatives:
				// LO = rs / rt; HI = rs % rt;
				// DIVIDE(HI, LO, RS, RT);
				return getCommand(address, assembly, concat("div   ", rs, rt),
						getAssignment(new MultiRegisterCommand(getRegister(HI_REGISTER), getRegister(LO_REGISTER)),
								getOperation(rs, Operation.DIVIDE_SIGNED, rt)));
			case 27: // DIVU
				rs = REGISTERS[rs(assembly)];
				rt = REGISTERS[rt(assembly)];

				return getCommand(address, assembly, concat("div   ", rs, rt),
						getAssignment(new MultiRegisterCommand(getRegister(HI_REGISTER), getRegister(LO_REGISTER)),
								getOperation(rs, Operation.DIVIDE_UNSIGNED, rt)));
			case 32: // ADD
				return getRdRsRt(address, assembly, "add   ", Operation.ADD_SIGNED);
			case 33: // ADDU
				if (rs(assembly) == 0) {
					rd = REGISTERS[rd(assembly)];
					rt = REGISTERS[rt(assembly)];

					return getCommand(address, assembly, concat("move  ", rd, rt),
							getAssignment(getRegister(rd), getRegister(rt)));
				} else if (rt(assembly) == 0) {
					rd = REGISTERS[rd(assembly)];
					rs = REGISTERS[rs(assembly)];

					return getCommand(address, assembly, concat("move  ", rd, rs),
							getAssignment(getRegister(rd), getRegister(rs)));
				}

				return getRdRsRt(address, assembly, "addu  ", Operation.ADD_UNSIGNED);
			case 34: // SUB
				return getRdRsRt(address, assembly, "sub   ", Operation.SUBTRACT_SIGNED);
			case 35: // SUBU
				return getRdRsRt(address, assembly, "subu  ", Operation.SUBTRACT_UNSIGNED);
			case 36: // AND
				return getRdRsRt(address, assembly, "and   ", Operation.AND);
			case 37: // OR
				return getRdRsRt(address, assembly, "or    ", Operation.OR);
			case 38: // XOR
				return getRdRsRt(address, assembly, "xor   ", Operation.XOR);
			case 39: // NOR
				rd = REGISTERS[rd(assembly)];
				rs = REGISTERS[rs(assembly)];
				rt = REGISTERS[rt(assembly)];

				if (rs(assembly) == 0) {
					return getCommand(address, assembly, concat("not   ", rd, rt),
							getAssignment(getRegister(rd), new NotCommand(getRegister(rt))));
				} else if (rt(assembly) == 0) {
					return getCommand(address, assembly, concat("not   ", rd, rs),
							getAssignment(getRegister(rd), new NotCommand(getRegister(rs))));
				}

				return getCommand(address, assembly, concat("not   ", rd, rs, rt),
						getAssignment(getRegister(rd), new NotCommand(getOperation(rs, Operation.OR, rs))));
			case 42: // SLT
				return getRdRsRt(address, assembly, "slt   ", Operation.LESS_THAN_SIGNED);
			case 43: // SLTU
				return getRdRsRt(address, assembly, "sltu  ", Operation.LESS_THAN_UNSIGNED);
			}
			break;
		case 1: // BCond
			switch (rt(assembly)) {
			case 0: // BLTZ
				return getRsBranch(address, assembly, "bltz  ", Operation.LESS_THAN_SIGNED);
			case 1: // BGEZ
				return getRsBranch(address, assembly, "bgez  ", Operation.GREATER_THAN_OR_EQUAL_SIGNED);
			case 16: // BLTZAL
				return getRsBranchAndLink(address, assembly, "bltzal ", Operation.LESS_THAN_SIGNED);
			case 17: // BGEZAL
				return getRsBranchAndLink(address, assembly, "bgezal ", Operation.GREATER_THAN_OR_EQUAL_SIGNED);
			}
			break;
		case 2: // J
			addr = (target(assembly) * 4) + (address & 0xf0000000);

			return getCommand(address, assembly, concat("j     ", addr), getGoto(addr));
		case 3: // JAL
			addr = (target(assembly) * 4) + (address & 0xf0000000);

			return getCommand(address, assembly, concat("jal   ", addr),
					new JumpSubroutineCommand(new HardcodeValueCommand(addr), getRegister(REGISTERS[31])));
		case 4: // BEQ
			branch = branch(assembly, address);

			if (rs(assembly) == 0) {
				if (rt(assembly) == 0) {
					return getCommand(address, assembly, concat("b     ", branch),
							new GotoCommand(new HardcodeValueCommand(branch)));
				} else {
					return getRtBranch(address, assembly, "bez   ", Operation.EQUAL);
				}
			} else if (rt(assembly) == 0) {
				return getRsBranch(address, assembly, "bez   ", Operation.EQUAL);
			}

			return getRsRtBranch(address, assembly, "beq   ", Operation.EQUAL);
		case 5: // BNE
			if (rt(assembly) == 0) {
				return getRsBranch(address, assembly, "bnz   ", Operation.NOT_EQUAL);
			}

			return getRsRtBranch(address, assembly, "bne   ", Operation.NOT_EQUAL);
		case 6: // BLEZ
			return getRsBranch(address, assembly, "blez  ", Operation.LESS_THAN_OR_EQUAL_SIGNED);
		case 7: // BGTZ
			return getRsBranch(address, assembly, "bgtz  ", Operation.GREATER_THAN_SIGNED);
		case 8: // ADDI
			return getRtRsImm(address, assembly, "addi  ", Operation.ADD_SIGNED);
		case 9: // ADDIU
			if (rs(assembly) == 0) {
				return getRtImm(address, assembly, "li    ", false);
			} else if (imm(assembly) == 0) {
				return getRtRs(address, assembly, "move  ");
			}

			return getRtRsImm(address, assembly, "addiu ", Operation.ADD_UNSIGNED);
		case 10: // SLTI
			return getRtRsImm(address, assembly, "slti  ", Operation.LESS_THAN_SIGNED);
		case 11: // SLTIU
			return getRtRsImm(address, assembly, "sltiu ", Operation.LESS_THAN_UNSIGNED);
		case 12: // ANDI
			return getRtRsImm(address, assembly, "andi  ", Operation.AND);
		case 13: // ORI
			if (rs(assembly) == 0) {
				return getRtImm(address, assembly, "li    ", false);
			}

			return getRtRsImm(address, assembly, "ori   ", Operation.OR);
		case 14: // XORI
			return getRtRsImm(address, assembly, "xori  ", Operation.OR);
		case 15: // LUI
			return getRtImm(address, assembly, "lui   ", true);
		case 16: // Cop0
			switch (rs(assembly)) {
			case 0: // MFC0
				break;
			case 2: // CFC0
				break;
			case 4: // MTC0
				break;
			case 6: // CTC0
				break;
			case 16: // RFE
				break;
			}
			break;
		case 18: // Cop2
			switch (assembly & 0x3F) {
			case 0: // Basic
				switch (rs(assembly)) {
				case 0: // MFC2
					break;
				case 2: // CFC2
					break;
				case 4: // MTC2
					break;
				case 6: // CTC2
					break;
				}
				break;
			// TODO
			case 1: // RTPS
				break;
			case 6: // NCLIP
				break;
			case 12: // OP
				break;
			case 16: // DPCS
				break;
			case 17: // INTPL
				break;
			case 18: // MVMVA
				break;
			case 19: // NCDS
				break;
			case 20: // CDP
				break;
			case 22: // NCDT
				break;
			case 27: // NCCS
				break;
			case 28: // CC
				break;
			case 30: // NCS
				break;
			case 32: // NCT
				break;
			case 40: // SQR
				break;
			case 41: // DCPL
				break;
			case 42: // DPCT
				break;
			case 45: // AVSZ3
				break;
			case 46: // AVSZ4
				break;
			case 48: // RTPT
				break;
			case 63: // NCCT
				break;
			}
			break;
		case 32: // LB
			return getRtImmRs(address, assembly, "lb    ", Operation.INDEX_SIGNED, 1);
		case 33: // LH
			return getRtImmRs(address, assembly, "lh    ", Operation.INDEX_SIGNED, 2);
		case 34: // LWL
			// TODO: (Memory & 0xFFFF0000) | (Register & 0x0000FFFF)
			break;
		case 35: // LW
			return getRtImmRs(address, assembly, "lw    ", Operation.INDEX_SIGNED, 4);
		case 36: // LBU
			return getRtImmRs(address, assembly, "lbu   ", Operation.INDEX_UNSIGNED, 1);
		case 37: // LHU
			return getRtImmRs(address, assembly, "lhu   ", Operation.INDEX_UNSIGNED, 2);
		case 38: // LWR
			// TODO: (Memory & 0x0000FFFF) | (Register & 0xFFFF0000)
		case 40: // SB
			return getRtImmRsReverse(address, assembly, "sb    ", Operation.INDEX_SIGNED, 1);
		case 41: // SH
			return getRtImmRsReverse(address, assembly, "sh    ", Operation.INDEX_SIGNED, 2);
		case 42: // SWL
			// TODO
			break;
		case 43: // SW
			return getRtImmRsReverse(address, assembly, "sw    ", Operation.INDEX_SIGNED, 4);
		case 46: // SWR
			// TODO
			break;
		case 50: // LWC2
			break;
		case 58: // SWC2
			break;
		case 59: // HLE
			break;
		}

		return new AssemblyRepresentation(address, assembly, 4, "ERROR", NopCommand.INSTANCE);
	}

	private static AssemblyRepresentation getCustomCall(int address, int assembly, String displayCommand, int parameter,
			String command) {
		return getCommand(address, assembly, concat(displayCommand, parameter),
				new CustomCallCommand(command, parameter));
	}

	private static AssemblyRepresentation getRdRtSa(int address, int assembly, String command, Operation operation) {
		final String rd = REGISTERS[rd(assembly)];
		final String rt = REGISTERS[rt(assembly)];
		final int sa = sa(assembly);

		return getCommand(address, assembly, concat(command, rd, rt, sa),
				getAssignment(rd, getOperation(rt, operation, sa)));
	}

	private static AssemblyRepresentation getRdRtRs(int address, int assembly, String command, Operation operation) {
		final String rd = REGISTERS[rd(assembly)];
		final String rt = REGISTERS[rt(assembly)];
		final String rs = REGISTERS[rs(assembly)];

		return getCommand(address, assembly, concat(command, rd, rt, rs),
				getAssignment(rd, getOperation(rt, operation, rs)));
	}

	private static AssemblyRepresentation getRdRsRt(int address, int assembly, String command, Operation operation) {
		final String rd = REGISTERS[rd(assembly)];
		final String rs = REGISTERS[rs(assembly)];
		final String rt = REGISTERS[rt(assembly)];

		return getCommand(address, assembly, concat(command, rd, rs, rt),
				getAssignment(rd, getOperation(rs, operation, rt)));
	}

	private static AssemblyRepresentation getRtRsImm(int address, int assembly, String command, Operation operation) {
		final String rt = REGISTERS[rt(assembly)];
		final String rs = REGISTERS[rs(assembly)];
		int imm = imm(assembly);
		if ((imm & 0x8000) != 0) {
			imm = -imm;
		}

		return getCommand(address, assembly, concat(command, rt, rs, imm),
				getAssignment(rt, getOperation(rs, operation, imm)));
	}

	private static AssemblyRepresentation getRtImm(int address, int assembly, String command, boolean upper) {
		final String rt = REGISTERS[rt(assembly)];
		final int imm = imm(assembly);

		return getCommand(address, assembly, concat(command, rt, imm),
				getAssignment(rt, getHardcoded(imm << (upper ? 16 : 0))));
	}

	private static AssemblyRepresentation getRtImmRs(int address, int assembly, String command, Operation operation,
			int byteSize) {
		final String rt = REGISTERS[rt(assembly)];
		final String rs = REGISTERS[rs(assembly)];
		final int imm = imm(assembly);

		return getCommand(address, assembly, concat(command, rt, imm, rs),
				getAssignment(rt, getByteTruncation(byteSize, getOperation(rs, operation, imm))));
	}

	private static AssemblyRepresentation getRtImmRsReverse(int address, int assembly, String command,
			Operation operation, int byteSize) {
		final String rt = REGISTERS[rt(assembly)];
		final String rs = REGISTERS[rs(assembly)];
		final int imm = imm(assembly);

		return getCommand(address, assembly, concat(command, rt, imm, rs),
				getAssignment(getByteTruncation(byteSize, getOperation(rs, operation, imm)), getRegister(rt)));
	}

	private static AssemblyRepresentation getRtRs(int address, int assembly, String command) {
		final String rt = REGISTERS[rt(assembly)];
		final String rs = REGISTERS[rs(assembly)];

		return getCommand(address, assembly, concat(command, rt, rs), getAssignment(rt, getRegister(rs)));
	}

	private static AssemblyRepresentation getRsRtBranch(int address, int assembly, String command,
			Operation operation) {
		final String rs = REGISTERS[rs(assembly)];
		final String rt = REGISTERS[rt(assembly)];
		final int branch = branch(assembly, address);

		return getCommand(address, assembly, concat(command, rs, rt, branch),
				getIfBranch(getOperation(rs, operation, rt), getGoto(branch)));
	}

	private static AssemblyRepresentation getRsBranch(int address, int assembly, String command, Operation operation) {
		final String rs = REGISTERS[rs(assembly)];
		final int branch = branch(assembly, address);

		return getCommand(address, assembly, concat(command, rs, Util.hex(branch)),
				getIfBranch(getOperation(rs, operation, 0), getGoto(branch)));
	}

	private static AssemblyRepresentation getRtBranch(int address, int assembly, String command, Operation operation) {
		final String rs = REGISTERS[rs(assembly)];
		final int branch = branch(assembly, address);

		return getCommand(address, assembly, concat(command, rs, Util.hex(branch)),
				getIfBranch(getOperation(rs, operation, 0), getGoto(branch)));
	}

	private static AssemblyRepresentation getRsBranchAndLink(int address, int assembly, String command,
			Operation operation) {
		final String rs = REGISTERS[rs(assembly)];
		final String ra = REGISTERS[30];
		final int branch = branch(assembly, address);

		return getCommand(address, assembly, concat(command, rs, Util.hex(branch)), getIfBranch(
				getOperation(rs, operation, 0), new JumpSubroutineCommand(getHardcoded(branch), getRegister(ra))));
	}

	private static AssemblyRepresentation getCommand(int address, int assembly, String display,
			AbstractCommand command) {
		return new AssemblyRepresentation(address, assembly, 4, display, command);
	}

	private static String concat(String command, int parameter) {
		return command + Util.hex(parameter);
	}

	private static String concat(String command, String s1, String s2, int i) {
		return command + s1 + ", " + s2 + ", " + Util.hex(i);
	}

	private static String concat(String command, String s1, String s2, String s3) {
		return command + s1 + ", " + s2 + ", " + s3;
	}

	private static String concat(String command, String s1, String s2) {
		return command + s2 + ", " + s1;
	}

	private static String concat(String command, String s1, int i) {
		return command + s1 + ", " + Util.hex(i);
	}

	private static String concat(String command, String s1, int i, String s2) {
		return command + s1 + ", " + Util.hex(i) + "(" + s2 + ")";
	}

	private static AbstractCommand getAssignment(String register, AbstractCommand command) {
		return new OperationCommand(getRegister(register), Operation.ASSIGNMENT, command);
	}

	private static AbstractCommand getAssignment(AbstractCommand left, AbstractCommand right) {
		return new OperationCommand(left, Operation.ASSIGNMENT, right);
	}

	private static AbstractCommand getOperation(String left, Operation operation, int right) {
		return new OperationCommand(getRegister(left), operation, getHardcoded(right));
	}

	private static AbstractCommand getOperation(String left, Operation operation, String right) {
		return new OperationCommand(getRegister(left), operation, getRegister(right));
	}

	private static AbstractCommand getRegister(String register) {
		return new RegisterCommand(register);
	}

	private static AbstractCommand getHardcoded(int value) {
		return new HardcodeValueCommand(value);
	}

	private static AbstractCommand getIfBranch(AbstractCommand condition, AbstractCommand operation) {
		return new IfCommand(condition, operation);
	}

	private static AbstractCommand getGoto(int address) {
		return new GotoCommand(getHardcoded(address));
	}

	private static AbstractCommand getByteTruncation(int byteSize, AbstractCommand command) {
		return new ByteTruncationCommand(byteSize, command);
	}

	private static int rs(int assembly) {
		return (assembly >>> 21) & 0x1F;
	}

	private static int rt(int assembly) {
		return ((assembly >> 16) & 0x1F);
	}

	private static int rd(int assembly) {
		return ((assembly >> 11) & 0x1F);
	}

	private static int sa(int assembly) {
		return ((assembly >> 6) & 0x1F);
	}

	private static short imm(int assembly) {
		return (short) (assembly & 0xFFFF);
	}

	private static int target(int assembly) {
		return (assembly & 0x3FFFFFF);
	}

	private static int branch(int assembly, int address) {
		return (imm(assembly) * 4) + address + 4;
	}

}
