package meldexun.imageutil;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

	private static final byte[] BUFFER = new byte[8192];

	public static void copy(InputStream in, OutputStream out, int len) throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(BUFFER, 0, Math.min(len - n, BUFFER.length));
			if (count < 0)
				throw new EOFException();
			out.write(BUFFER, 0, count);
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

	public static short readShort(InputStream in) throws IOException {
		readFully(in, BUFFER, 0, 2);
		return (short) ((BUFFER[0] & 0xFF) << 8 | (BUFFER[1] & 0xFF) << 0);
	}

	public static int readInt(InputStream in) throws IOException {
		readFully(in, BUFFER, 0, 4);
		return (BUFFER[0] & 0xFF) << 24
				| (BUFFER[1] & 0xFF) << 16
				| (BUFFER[2] & 0xFF) << 8
				| (BUFFER[3] & 0xFF) << 0;
	}

	public static long readLong(InputStream in) throws IOException {
		readFully(in, BUFFER, 0, 8);
		return (BUFFER[0] & 0xFFL) << 56
				| (BUFFER[1] & 0xFFL) << 48
				| (BUFFER[2] & 0xFFL) << 40
				| (BUFFER[3] & 0xFFL) << 32
				| (BUFFER[4] & 0xFFL) << 24
				| (BUFFER[5] & 0xFFL) << 16
				| (BUFFER[6] & 0xFFL) << 8
				| (BUFFER[7] & 0xFFL) << 0;
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
			int skipped = (int) Math.min(n, BUFFER.length);
			readFully(in, BUFFER, 0, skipped);
			n -= skipped;
		}
	}

}
