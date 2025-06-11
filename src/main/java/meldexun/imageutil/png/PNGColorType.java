package meldexun.imageutil.png;

import meldexun.imageutil.Color;
import meldexun.memoryutil.MemoryAccess;

enum PNGColorType {

	GREYSCALE(0, 1) {
		@Override
		protected byte red(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte green(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte blue(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte alpha(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return (byte) 255;
		}
	},
	TRUECOLOR(2, 3) {
		@Override
		protected byte red(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte green(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 1, this.channels);
		}

		@Override
		protected byte blue(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 2, this.channels);
		}

		@Override
		protected byte alpha(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return (byte) 255;
		}

		@Override
		public void copyPixels(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, MemoryAccess dst, long dstOffset, Color dstColor, int offsetX, int strideX, int width) {
			if (dstColor == Color.RGB && strideX == 1) {
				MemoryAccess.copyMemory(MemoryAccess.of(scanline), scanlineOffset, dst, dstOffset + offsetX * dstColor.bytesPerPixel(), width * dstColor.bytesPerPixel());
			} else {
				super.copyPixels(scanline, scanlineOffset, PLTE, tRNS, bitDepth, dst, dstOffset, dstColor, offsetX, strideX, width);
			}
		}
	},
	INDEXED_COLOR(3, 1) {
		@Override
		protected byte red(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return PLTE[bitDepth.get(scanline, scanlineOffset, x, 0, this.channels) * 3 + 0];
		}

		@Override
		protected byte green(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return PLTE[bitDepth.get(scanline, scanlineOffset, x, 0, this.channels) * 3 + 1];
		}

		@Override
		protected byte blue(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return PLTE[bitDepth.get(scanline, scanlineOffset, x, 0, this.channels) * 3 + 2];
		}

		@Override
		protected byte alpha(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			if (tRNS == null)
				return (byte) 255;
			int i = bitDepth.get(scanline, scanlineOffset, x, 0, this.channels);
			if (tRNS.length > 1)
				return tRNS[i];
			if (i == 0)
				return tRNS[0];
			return (byte) 255;
		}
	},
	GREYSCALE_ALPHA(4, 2) {
		@Override
		protected byte red(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte green(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte blue(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte alpha(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 1, this.channels);
		}
	},
	TRUECOLOR_ALPHA(6, 4) {
		@Override
		protected byte red(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 0, this.channels);
		}

		@Override
		protected byte green(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 1, this.channels);
		}

		@Override
		protected byte blue(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 2, this.channels);
		}

		@Override
		protected byte alpha(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x) {
			return bitDepth.getByte(scanline, scanlineOffset, x, 3, this.channels);
		}

		@Override
		public void copyPixels(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, MemoryAccess dst, long dstOffset, Color dstColor, int offsetX, int strideX, int width) {
			if (dstColor == Color.RGBA && strideX == 1) {
				MemoryAccess.copyMemory(MemoryAccess.of(scanline), scanlineOffset, dst, dstOffset + offsetX * dstColor.bytesPerPixel(), width * dstColor.bytesPerPixel());
			} else {
				super.copyPixels(scanline, scanlineOffset, PLTE, tRNS, bitDepth, dst, dstOffset, dstColor, offsetX, strideX, width);
			}
		}
	};

	private final int index;
	protected final int channels;

	private PNGColorType(int index, int channels) {
		this.index = index;
		this.channels = channels;
	}

	public static PNGColorType byIndex(int index) {
		switch (index) {
		case 0:
			return GREYSCALE;
		case 2:
			return TRUECOLOR;
		case 3:
			return INDEXED_COLOR;
		case 4:
			return GREYSCALE_ALPHA;
		case 6:
			return TRUECOLOR_ALPHA;
		default:
			throw new IllegalArgumentException();
		}
	}

	public int index() {
		return this.index;
	}

	public int channels() {
		return this.channels;
	}

	public void copyPixels(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, MemoryAccess dst, long dstOffset, Color dstColor, int offsetX, int strideX, int width) {
		if (dstColor.hasAlpha()) {
			this.copyPixelsRGBA(scanline, scanlineOffset, PLTE, tRNS, bitDepth, dst, dstOffset, dstColor, offsetX, strideX, width);
		} else {
			this.copyPixelsRGB(scanline, scanlineOffset, PLTE, tRNS, bitDepth, dst, dstOffset, dstColor, offsetX, strideX, width);
		}
	}

	private void copyPixelsRGBA(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, MemoryAccess dst, long dstOffset, Color dstColor, int offsetX, int strideX, int width) {
		for (int x = 0; x < width; x++) {
			dst.putByte(dstOffset + (x * strideX + offsetX) * dstColor.bytesPerPixel() + dstColor.redOffset(), this.red(scanline, scanlineOffset, PLTE, tRNS, bitDepth, x));
			dst.putByte(dstOffset + (x * strideX + offsetX) * dstColor.bytesPerPixel() + dstColor.greenOffset(), this.green(scanline, scanlineOffset, PLTE, tRNS, bitDepth, x));
			dst.putByte(dstOffset + (x * strideX + offsetX) * dstColor.bytesPerPixel() + dstColor.blueOffset(), this.blue(scanline, scanlineOffset, PLTE, tRNS, bitDepth, x));
			dst.putByte(dstOffset + (x * strideX + offsetX) * dstColor.bytesPerPixel() + dstColor.alphaOffset(), this.alpha(scanline, scanlineOffset, PLTE, tRNS, bitDepth, x));
		}
	}

	private void copyPixelsRGB(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, MemoryAccess dst, long dstOffset, Color dstColor, int offsetX, int strideX, int width) {
		for (int x = 0; x < width; x++) {
			dst.putByte(dstOffset + (x * strideX + offsetX) * dstColor.bytesPerPixel() + dstColor.redOffset(), this.red(scanline, scanlineOffset, PLTE, tRNS, bitDepth, x));
			dst.putByte(dstOffset + (x * strideX + offsetX) * dstColor.bytesPerPixel() + dstColor.greenOffset(), this.green(scanline, scanlineOffset, PLTE, tRNS, bitDepth, x));
			dst.putByte(dstOffset + (x * strideX + offsetX) * dstColor.bytesPerPixel() + dstColor.blueOffset(), this.blue(scanline, scanlineOffset, PLTE, tRNS, bitDepth, x));
		}
	}

	protected abstract byte red(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x);

	protected abstract byte green(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x);

	protected abstract byte blue(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x);

	protected abstract byte alpha(byte[] scanline, int scanlineOffset, byte[] PLTE, byte[] tRNS, PNGBitDepth bitDepth, int x);

}
