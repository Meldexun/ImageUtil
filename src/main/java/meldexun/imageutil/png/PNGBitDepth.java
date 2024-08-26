package meldexun.imageutil.png;

enum PNGBitDepth {

	ONE(1) {
		@Override
		public byte getByte(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = this.get(scanline, scanlineOffset, x, colorIndex, colorChannels);
			return (byte) (i * 255);
		}

		@Override
		public int get(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = x * colorChannels + colorIndex;
			return scanline[scanlineOffset + i / 8] >> ((7 - (i & 7)) * 1) & 1;
		}
	},
	TWO(2) {
		@Override
		public byte getByte(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = this.get(scanline, scanlineOffset, x, colorIndex, colorChannels);
			return (byte) (i * (255 / 3));
		}

		@Override
		public int get(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = x * colorChannels + colorIndex;
			return scanline[scanlineOffset + i / 4] >> ((3 - (i & 3)) * 2) & 3;
		}
	},
	FOUR(4) {
		@Override
		public byte getByte(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = this.get(scanline, scanlineOffset, x, colorIndex, colorChannels);
			return (byte) (i * (255 / 15));
		}

		@Override
		public int get(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = x * colorChannels + colorIndex;
			return scanline[scanlineOffset + i / 2] >> ((1 - (i & 1)) * 4) & 15;
		}
	},
	EIGHT(8) {
		@Override
		public byte getByte(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			return scanline[scanlineOffset + x * colorChannels + colorIndex];
		}

		@Override
		public int get(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			return this.getByte(scanline, scanlineOffset, x, colorIndex, colorChannels) & 255;
		}
	},
	SIXTEEN(16) {
		@Override
		public byte getByte(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = this.get(scanline, scanlineOffset, x, colorIndex, colorChannels);
			return (byte) ((i * 255 + 32895) >> 16);
		}

		@Override
		public int get(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels) {
			int i = scanlineOffset + (x * colorChannels + colorIndex) * 2;
			return (scanline[i] & 255) << 8 | (scanline[i + 1] & 255);
		}
	};

	private final int value;

	private PNGBitDepth(int value) {
		this.value = value;
	}

	public static PNGBitDepth valueOf(int bitDepth) {
		switch (bitDepth) {
		case 1:
			return ONE;
		case 2:
			return TWO;
		case 4:
			return FOUR;
		case 8:
			return EIGHT;
		case 16:
			return SIXTEEN;
		default:
			throw new IllegalArgumentException();
		}
	}

	public int value() {
		return this.value;
	}

	public abstract byte getByte(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels);

	public abstract int get(byte[] scanline, int scanlineOffset, int x, int colorIndex, int colorChannels);

}
