package meldexun.imageutil.gif;

import static meldexun.memoryutil.UnsafeUtil.UNSAFE;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import meldexun.imageutil.IOUtil;
import sun.misc.Unsafe;

public class GIFInputStream extends FilterInputStream {

	private final byte[] buffer = new byte[4];

	public GIFInputStream(InputStream in) {
		super(in);
	}

	public int readByte() throws IOException {
		return IOUtil.read(this);
	}

	public int readShort() throws IOException {
		IOUtil.readFully(this, this.buffer, 0, 2);
		return UNSAFE.getShort(this.buffer, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET) & 0xFFFF;
	}

	public int read3Byte() throws IOException {
		IOUtil.readFully(this, this.buffer, 0, 3);
		return UNSAFE.getInt(this.buffer, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET) & 0xFFFFFF;
	}

}
