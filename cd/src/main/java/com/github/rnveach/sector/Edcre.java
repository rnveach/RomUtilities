package com.github.rnveach.sector;

public final class Edcre {

	private static final int GF8_PRIM_POLY = 0x11d;// x^8 + x^4 + x^3 + x^2 + 1
	private static final int EDC_POLY = 0x8001801b; // (x^16 + x^15 + x^2 + 1) (x^16 + x^2 + x + 1)
	private static final int LEC_HEADER_OFFSET = 12;
	private static final int LEC_DATA_OFFSET = 16;
	private static final int LEC_MODE1_DATA_LEN = 2048;
	private static final int LEC_MODE1_EDC_OFFSET = 2064;
	private static final int LEC_MODE1_INTERMEDIATE_OFFSET = 2068;
	private static final int LEC_MODE1_P_PARITY_OFFSET = 2076;
	private static final int LEC_MODE1_Q_PARITY_OFFSET = 2248;
	private static final int LEC_MODE2_FORM1_DATA_LEN = (2048 + 8);
	private static final int LEC_MODE2_FORM1_EDC_OFFSET = 2072;
	private static final int LEC_MODE2_FORM2_DATA_LEN = (2324 + 8);
	private static final int LEC_MODE2_FORM2_EDC_OFFSET = 2348;

	private static final int[] CRC_TABLE = new int[256];

	public static void main(String... arguments) {
		System.out.println("Hello");
	}

	static {
		for (int i = 0; i < 256; i++) {
			int r = mirrorBits(i, 8);
			r <<= 24;

			for (int j = 0; j < 8; j++) {
				if ((r & 0x80000000) != 0) {
					r <<= 1;
					r ^= EDC_POLY;
				} else {
					r <<= 1;
				}
			}

			r = mirrorBits(r, 32);
			CRC_TABLE[i] = r;
		}
	}

	private static int mirrorBits(int d, int bits) {
		int r = 0;
		for (int i = 0; i < bits; i++) {
			r <<= 1;
			if ((d & 0x1) != 0) {
				r |= 0x1;
			}
			d >>= 1;
		}
		return r;
	}

	public static void calcPParity(byte[] sector) {
		int i, j;
		int p01Msb, p01Lsb;
		int pLsbStart;
		int p0Offset, p1Offset;
		byte d0, d1;

		pLsbStart = LEC_HEADER_OFFSET;

		p1Offset = LEC_MODE1_P_PARITY_OFFSET;
		p0Offset = LEC_MODE1_P_PARITY_OFFSET + (2 * 43);

		for (i = 0; i <= 42; i++) {
			p01Lsb = p01Msb = 0;

			for (j = 19; j <= 42; j++) {
				d0 = sector[pLsbStart];
				d1 = sector[pLsbStart + 1];

				p01Lsb ^= CF8_Q_COEFFS_RESULTS_01[j][d0 & 0xFF];
				p01Msb ^= CF8_Q_COEFFS_RESULTS_01[j][d1 & 0xFF];

				pLsbStart += 2 * 43;
			}

			sector[p0Offset] = (byte) p01Lsb;
			sector[p0Offset + 1] = (byte) p01Msb;

			sector[p1Offset] = (byte) (p01Lsb >> 8);
			sector[p1Offset + 1] = (byte) (p01Msb >> 8);

			p0Offset += 2;
			p1Offset += 2;

			pLsbStart += 2;
		}
	}

}
