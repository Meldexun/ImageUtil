package meldexun.imageutil.png;

import java.util.List;

public class CompressedAPNG {

	public static class Frame {

		public final int width;
		public final int height;
		public final int x_offset;
		public final int y_offset;
		public final int delay_num;
		public final int delay_den;
		public final int dispose_op;
		public final int blend_op;
		public final byte[] data;

		public Frame(int width, int height, int x_offset, int y_offset, int delay_num, int delay_den, int dispose_op, int blend_op, byte[] data) {
			this.width = width;
			this.height = height;
			this.x_offset = x_offset;
			this.y_offset = y_offset;
			this.delay_num = delay_num;
			this.delay_den = delay_den;
			this.dispose_op = dispose_op;
			this.blend_op = blend_op;
			this.data = data;
		}

	}

	public final int width;
	public final int height;
	public final PNGColorType colorType;
	public final PNGBitDepth bitDepth;
	public final byte[] PLTE;
	public final byte[] tRNS;
	public final List<Frame> frames;
	public final int plays;

	public CompressedAPNG(int width, int height, PNGColorType colorType, PNGBitDepth bitDepth, byte[] PLTE, byte[] tRNS, List<Frame> frames, int plays) {
		this.width = width;
		this.height = height;
		this.colorType = colorType;
		this.bitDepth = bitDepth;
		this.PLTE = PLTE;
		this.tRNS = tRNS;
		this.frames = frames;
		this.plays = plays;
	}

}
