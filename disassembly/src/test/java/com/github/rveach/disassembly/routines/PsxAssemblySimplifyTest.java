package com.github.rveach.disassembly.routines;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.rveach.disassembly.AbstractInstructionTest;

public final class PsxAssemblySimplifyTest extends AbstractInstructionTest {

	@Test
	public void testLuiAddiu() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"0D 80 03 3C", //
						"2C B8 63 24"), //
				Arrays.asList(//
						"lui   $v1, 0x800D", //
						"addiu $v1, $v1, 0xFFFFB82C"), //
				Arrays.asList(//
						"$v1 = 0x800D0000", //
						"$v1 += -0x47D4"), //
				Arrays.asList(//
						"$v1 = 0x800CB82C"//
				)//
		);
	}

	@Test
	public void testLuiAddiuDifferentRegisterAssignment() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"0C 80 03 3C", //
						"54 60 64 24"), //
				Arrays.asList(//
						"lui   $v1, 0x800C", //
						"addiu $a0, $v1, 0x6054"), //
				Arrays.asList(//
						"$v1 = 0x800C0000", //
						"$a0 = $v1 + 0x6054"), //
				Arrays.asList(//
						"$a0 = 0x800C6054" //
				)//
		);
	}

	@Test
	public void testLuiOri() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"10 80 04 3C", //
						"00 62 84 34"), //
				Arrays.asList(//
						"lui   $a0, 0x8010", //
						"ori   $a0, $a0, 0x6200"), //
				Arrays.asList(//
						"$a0 = 0x80100000", //
						"$a0 |= 0x6200"), //
				Arrays.asList(//
						"$a0 = 0x80106200"//
				)//
		);
	}

	@Test
	public void testR0() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"21 38 00 00"), //
				Arrays.asList(//
						"move  $a3, $r0"), //
				Arrays.asList(//
						"$a3 = $r0"), //
				Arrays.asList(//
						"$a3 = 0"//
				)//
		);
	}

	@Test
	public void testR0Assignment() {
		assertPsxCompleteProcess(//
				Arrays.asList(//
						"70 02 80 8F"), //
				Arrays.asList(//
						"lw    $r0, 0x270($gp)"), //
				Arrays.asList(//
						"$r0 = (long) $gp[0x270]"), //
				Arrays.asList(//
						""//
				)//
		);
	}

}
