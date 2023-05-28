package com.github.rveach.disassembly.routines;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.rveach.disassembly.AbstractInstructionTest;

public final class PsxAssemblyTest extends AbstractInstructionTest {

	@Test
	public void test() {
		assertPsxProcess(Arrays.asList("70 02 84 8F"), //
				Arrays.asList("$a0 = (long) $gp[0x270]"));
	}

	@Test
	public void testImmediate() {
		assertPsxProcess(Arrays.asList("D8 FF BD 27"), //
				Arrays.asList("$sp += -0x28"));
	}

}
