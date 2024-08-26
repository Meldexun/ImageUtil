package meldexun.imageutil;

import meldexun.memoryutil.UnsafeBuffer;

public class Image implements AutoCloseable {

	public final int width;
	public final int height;
	public final Color color;
	public final UnsafeBuffer data;

	public Image(int width, int height, Color color, UnsafeBuffer data) {
		this.width = width;
		this.height = height;
		this.color = color;
		this.data = data;
	}

	@Override
	public void close() {
		this.data.close();
	}

}
