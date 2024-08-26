package meldexun.imageutil.gif;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream extends FilterInputStream {

	private int current;
	private int remaining;

	public BitInputStream(InputStream in) {
		super(in);
	}

	public int read(int bits) throws IOException {
		if (bits < 1 || bits > 31) {
			throw new IllegalArgumentException();
		}
		int result = 0;
		int n = 0;
		while (n < bits) {
			if (this.remaining == 0) {
				int b = this.read();
				if (b < 0) {
					return -1;
				}
				this.current = b;
				this.remaining = 8;
			}
			int count = Math.min(bits - n, this.remaining);
			result |= (this.current & ((1 << count) - 1)) << n;
			n += count;
			this.current >>>= count;
			this.remaining -= count;
		}
		return result;
	}

}
