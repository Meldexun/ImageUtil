package meldexun.imageutil.gif;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class LZWInputStream extends FilterInputStream {

	private final BitInputStream bitInput;
	private final int minBitsPerCode;
	private final int clearCode;
	private final int eofCode;

	private byte[][] table = new byte[4096][];
	private int tableSize;
	private int bitsPerCode;
	private int prevCode;

	private byte[] buffer = new byte[0];
	private int bufferIndex;

	public LZWInputStream(InputStream in) throws IOException {
		super(in);
		this.bitInput = new BitInputStream(in);
		this.minBitsPerCode = in.read();
		if (this.minBitsPerCode < 0) {
			throw new EOFException();
		}
		this.clearCode = 1 << this.minBitsPerCode;
		this.eofCode = (1 << this.minBitsPerCode) + 1;

		for (int i = 0; i < (1 << this.minBitsPerCode) + 2; i++) {
			this.table[i] = new byte[] { (byte) i };
		}
		this.tableSize = (1 << this.minBitsPerCode) + 2;
		this.bitsPerCode = this.minBitsPerCode + 1;
		this.prevCode = -1;
	}

	@Override
	public int read() throws IOException {
		if (this.bufferIndex == this.buffer.length && !this.next()) {
			return -1;
		}
		return this.buffer[this.bufferIndex++] & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (this.bufferIndex == this.buffer.length && !this.next()) {
			return -1;
		}
		int read = Math.min(len, this.buffer.length - this.bufferIndex);
		System.arraycopy(this.buffer, this.bufferIndex, b, off, read);
		this.bufferIndex += read;
		return read;
	}

	@Override
	public long skip(long n) throws IOException {
		if (this.bufferIndex == this.buffer.length && !this.next()) {
			return 0;
		}
		int skipped = this.buffer.length - this.bufferIndex;
		this.bufferIndex += skipped;
		return skipped;
	}

	private boolean next() throws IOException {
		int code = this.bitInput.read(this.bitsPerCode);
		if (code < 0) {
			return false;
		}
		if (code == this.eofCode) {
			return false;
		}
		if (code == this.clearCode) {
			this.tableSize = (1 << this.minBitsPerCode) + 2;
			this.bitsPerCode = this.minBitsPerCode + 1;
			this.prevCode = -1;
			return this.next();
		}
		byte suffix;
		if (code < this.tableSize) {
			suffix = this.table[code][0];
		} else if (code == this.tableSize) {
			if (this.prevCode < 0) {
				throw new IOException("Illegal previous code");
			}
			suffix = this.table[this.prevCode][0];
		} else {
			throw new IOException("Illegal code");
		}
		if (this.prevCode >= 0 && this.tableSize < 4096) {
			byte[] a = this.table[this.prevCode];
			a = Arrays.copyOf(a, a.length + 1);
			a[a.length - 1] = suffix;
			this.table[this.tableSize++] = a;
			if (this.tableSize == 1 << this.bitsPerCode && this.tableSize < 4096) {
				this.bitsPerCode++;
			}
		}
		this.buffer = this.table[code];
		this.bufferIndex = 0;
		this.prevCode = code;
		return true;
	}

}
