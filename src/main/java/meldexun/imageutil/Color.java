package meldexun.imageutil;

import meldexun.memoryutil.MemoryAccess;

public enum Color {

	RGB(0, 1, 2),
	RGBA(0, 1, 2, 3),
	BGR(2, 1, 0),
	BGRA(2, 1, 0, 3),
	ARGB(1, 2, 3, 0),
	ABGR(3, 2, 1, 0);

	private final boolean hasAlpha;
	private final int bytesPerPixel;
	private final int redOffset;
	private final int greenOffset;
	private final int blueOffset;
	private final int alphaOffset;

	private Color(int redOffset, int greenOffset, int blueOffset) {
		this(false, redOffset, greenOffset, blueOffset, -1);
	}

	private Color(int redOffset, int greenOffset, int blueOffset, int alphaOffset) {
		this(true, redOffset, greenOffset, blueOffset, alphaOffset);
	}

	private Color(boolean hasAlpha, int redOffset, int greenOffset, int blueOffset, int alphaOffset) {
		this.hasAlpha = hasAlpha;
		this.bytesPerPixel = this.hasAlpha ? 4 : 3;
		this.redOffset = redOffset;
		this.greenOffset = greenOffset;
		this.blueOffset = blueOffset;
		this.alphaOffset = alphaOffset;
	}

	public static void transform(MemoryAccess src, long srcOffset, MemoryAccess dst, long dstOffset, Color srcColor, Color dstColor, long pixels) {
		if (srcColor == dstColor) {
			MemoryAccess.copyMemory(src, srcOffset, dst, dstOffset, pixels * srcColor.bytesPerPixel);
			return;
		}
		if (dstColor.hasAlpha) {
			if (srcColor.hasAlpha) {
				transformRGBA(src, srcOffset, dst, dstOffset, srcColor, dstColor, pixels);
			} else {
				transformRGBtoRGBA(src, srcOffset, dst, dstOffset, srcColor, dstColor, pixels);
			}
		} else {
			transformRGB(src, srcOffset, dst, dstOffset, srcColor, dstColor, pixels);
		}
	}

	private static void transformRGB(MemoryAccess src, long srcOffset, MemoryAccess dst, long dstOffset, Color srcColor, Color dstColor, long pixels) {
		for (long i = 0; i < pixels; i++) {
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.redOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.redOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.greenOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.greenOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.blueOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.blueOffset));
		}
	}

	private static void transformRGBA(MemoryAccess src, long srcOffset, MemoryAccess dst, long dstOffset, Color srcColor, Color dstColor, long pixels) {
		for (long i = 0; i < pixels; i++) {
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.redOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.redOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.greenOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.greenOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.blueOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.blueOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.alphaOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.alphaOffset));
		}
	}

	private static void transformRGBtoRGBA(MemoryAccess src, long srcOffset, MemoryAccess dst, long dstOffset, Color srcColor, Color dstColor, long pixels) {
		for (long i = 0; i < pixels; i++) {
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.redOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.redOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.greenOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.greenOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.blueOffset, src.getByte(srcOffset + i * srcColor.bytesPerPixel + srcColor.blueOffset));
			dst.putByte(dstOffset + i * dstColor.bytesPerPixel + dstColor.alphaOffset, (byte) 255);
		}
	}

	public boolean hasAlpha() {
		return this.hasAlpha;
	}

	public int bytesPerPixel() {
		return this.bytesPerPixel;
	}

	public int redOffset() {
		return this.redOffset;
	}

	public int greenOffset() {
		return this.greenOffset;
	}

	public int blueOffset() {
		return this.blueOffset;
	}

	public int alphaOffset() {
		return this.alphaOffset;
	}

}
