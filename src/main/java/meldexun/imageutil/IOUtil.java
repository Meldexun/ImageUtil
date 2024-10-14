package meldexun.imageutil;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

	private static final byte[] SKIP_BUFFER = new byte[8192];

	public static void copy(InputStream in, OutputStream out, byte[] b, int len) throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(b, 0, Math.min(len - n, b.length));
			if (count < 0)
				throw new EOFException();
			out.write(b, 0, count);
			n += count;
		}
	}

	public static void readFully(InputStream in, byte[] b) throws IOException {
		readFully(in, b, 0, b.length);
	}

	public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}

	public static int read(InputStream in) throws IOException {
		int b = in.read();
		if (b < 0)
			throw new EOFException();
		return b;
	}

	public static byte readByte(InputStream in) throws IOException {
		return (byte) read(in);
	}

	public static short readShort(InputStream in, byte[] b) throws IOException {
		readFully(in, b, 0, 2);
		return (short) ((b[0] & 0xFF) << 8 | (b[1] & 0xFF) << 0);
	}

	public static int readInt(InputStream in, byte[] b) throws IOException {
		readFully(in, b, 0, 4);
		return (b[0] & 0xFF) << 24
				| (b[1] & 0xFF) << 16
				| (b[2] & 0xFF) << 8
				| (b[3] & 0xFF) << 0;
	}

	public static long readLong(InputStream in, byte[] b) throws IOException {
		readFully(in, b, 0, 8);
		return (b[0] & 0xFFL) << 56
				| (b[1] & 0xFFL) << 48
				| (b[2] & 0xFFL) << 40
				| (b[3] & 0xFFL) << 32
				| (b[4] & 0xFFL) << 24
				| (b[5] & 0xFFL) << 16
				| (b[6] & 0xFFL) << 8
				| (b[7] & 0xFFL) << 0;
	}

	public static void skip(InputStream in, long n) throws IOException {
		while (n > 0) {
			long skipped = in.skip(n);
			if (skipped == 0) {
				skip0(in, n);
				break;
			}
			n -= skipped;
		}
	}

	private static void skip0(InputStream in, long n) throws IOException {
		while (n > 0) {
			int skipped = (int) Math.min(n, SKIP_BUFFER.length);
			readFully(in, SKIP_BUFFER, 0, skipped);
			n -= skipped;
		}
	}

}
