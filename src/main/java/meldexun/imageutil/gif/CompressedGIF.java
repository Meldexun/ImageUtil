package meldexun.imageutil.gif;

import java.util.List;

public class CompressedGIF {

	public static class Frame {

		public static class GraphicControl {

			public final int packed;
			public final int delay;
			public final int transparentColorIndex;

			public GraphicControl(int packed, int delay, int transparentColorIndex) {
				this.packed = packed;
				this.delay = delay;
				this.transparentColorIndex = transparentColorIndex;
			}

		}

		public final int left;
		public final int top;
		public final int width;
		public final int height;
		public final int packed;
		public final byte[] localColorTable;
		public final byte[] data;
		public final GraphicControl graphicControl;

		public Frame(int left, int top, int width, int height, int packed, byte[] localColorTable, byte[] data, GraphicControl graphicControl) {
			this.left = left;
			this.top = top;
			this.width = width;
			this.height = height;
			this.packed = packed;
			this.localColorTable = localColorTable;
			this.data = data;
			this.graphicControl = graphicControl;
		}

	}

	public final int width;
	public final int height;
	public final int packed;
	public final int backgroundColor;
	public final int aspectRatio;
	public final byte[] globalColorTable;
	public final List<Frame> frames;

	public CompressedGIF(int width, int height, int packed, int backgroundColor, int aspectRatio, byte[] globalColorTable, List<Frame> frames) {
		this.width = width;
		this.height = height;
		this.packed = packed;
		this.backgroundColor = backgroundColor;
		this.aspectRatio = aspectRatio;
		this.globalColorTable = globalColorTable;
		this.frames = frames;
	}

}
