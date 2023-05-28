package com.github.rveach.disassembly.routines;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.rveach.disassembly.AbstractInstructionTest;

public final class PsxAssemblyFixTest extends AbstractInstructionTest {

	@Test
	public void testBranchFix() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"FF FF 60 14", //
						"04 00 84 24", //
						"00 00 00 00"), //
				Arrays.asList(//
						"bnz   $v1, 0x0", //
						"addiu $a0, $a0, 0x4", //
						"nop"), //
				Arrays.asList(//
						"if ($v1 != 0) goto LAB_0", //
						"$a0 += 4"), //
				Arrays.asList(//
						"LAB_0", //
						"$a0 += 4", //
						"if ($v1 != 0) goto LAB_0" //
				)//
		);
	}

	@Test
	public void testBranchFixNop() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"FF FF 60 14", //
						"00 00 00 00", //
						"04 00 84 24"), //
				Arrays.asList(//
						"bnz   $v1, 0x0", //
						"nop", //
						"addiu $a0, $a0, 0x4"), //
				Arrays.asList(//
						"if ($v1 != 0) goto LAB_0", //
						"$a0 += 4"), //
				Arrays.asList(//
						"LAB_0", //
						"if ($v1 != 0) goto LAB_0", //
						"$a0 += 4" //
				)//
		);
	}

	@Test
	public void testBranchFixVariableConflict() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"25 00 03 24", //
						"FF FF 40 10", //
						"57 00 02 24"), //
				Arrays.asList(//
						"li    $v1, 0x25", //
						"bez   $v0, 0x4", //
						"li    $v0, 0x57"), //
				Arrays.asList(//
						"$v1 = 0x25", //
						"if ($v0 == 0) goto LAB_4", //
						"$v0 = 0x57"), //
				Arrays.asList(//
						"LAB_0", //
						"$v1 = 0x25", //
						"LAB_4", //
						"if ($v0 != 0) goto LAB_8", //
						"$v0 = 0x57", //
						"goto LAB_4", //
						"LAB_8", //
						"$v0 = 0x57" //
				)//
		);
	}

	@Test
	public void testBranchUnconditional() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"08 00 E0 03", //
						"18 00 BD 27"), //
				Arrays.asList(//
						"jr    $ra", //
						"addiu $sp, $sp, 0x18"), //
				Arrays.asList(//
						"goto $ra", //
						"$sp += 0x18"), //
				Arrays.asList(//
						"$sp += 0x18", //
						"goto $ra" //
				)//
		);
	}

	@Test
	public void testCall() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"CE E3 00 0C", //
						"21 80 00 00"), //
				Arrays.asList(//
						"jal   0x38F38", //
						"move  $s0, $r0"), //
				Arrays.asList(//
						"$ra <- 0x38F38()", //
						"$s0 = $r0"), //
				Arrays.asList(//
						"$s0 = 0", //
						"$ra <- 0x38F38()" //
				)//
		);
	}

}
