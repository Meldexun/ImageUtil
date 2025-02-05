package meldexun.imageutil.gif;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import meldexun.memoryutil.MemoryAccess;

public class BitInputStream extends FilterInputStream {

	private final byte[] buffer = new byte[512 + 4];
	private final MemoryAccess bufferAccess = MemoryAccess.of(this.buffer);
	private int index;
	private int count;

	public BitInputStream(InputStream in) {
		super(in);
	}

	public int read(int bits) throws IOException {
		if (bits < 1 || bits > 31) {
			throw new IllegalArgumentException();
		}
		if (this.count - this.index < bits) {
			if (this.index > 8) {
				System.arraycopy(this.buffer, this.index / 8, this.buffer, 0, this.count / 8 - this.index / 8);
				this.count -= this.index & ~7;
				this.index &= 7;
			}
			this.count += this.in.read(this.buffer, this.count / 8, 512 - this.count / 8) * 8;
		}
		int rawData = this.bufferAccess.getInt(this.index / 8);
		int data = (rawData >>> (this.index & 7)) & ((1 << bits) - 1);
		this.index += bits;
		return data;
	}

}
