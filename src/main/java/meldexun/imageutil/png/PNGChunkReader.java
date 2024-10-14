package meldexun.imageutil.png;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.IntPredicate;

import javax.imageio.IIOException;

import meldexun.imageutil.IOUtil;

class PNGChunkReader extends FilterInputStream {

	private final byte[] buffer = new byte[8];
	private int type = -1;
	private int length = -1;
	private int remaining = -1;

	public PNGChunkReader(InputStream in) {
		super(in);
	}

	public long readSignature() throws IOException {
		return IOUtil.readLong(this.in, this.buffer);
	}

	public int readByte() throws IOException {
		return IOUtil.readByte(this) & 0xFF;
	}

	public int readShort() throws IOException {
		return IOUtil.readShort(this, this.buffer) & 0xFFFF;
	}

	public int readInt() throws IOException {
		int read = IOUtil.readInt(this, this.buffer);
		if (read < 0) {
			throw new IIOException("Value is not a PNG int: " + read);
		}
		return read;
	}

	private boolean isChunkOpen() {
		return this.type >= 0;
	}

	private void ensureChunkDataAvailable() throws IOException {
		if (!this.isChunkOpen()) {
			this.openChunk();
		}
		while (this.remaining == 0) {
			this.closeChunk();
			this.openChunk();
		}
	}

	@Override
	public int read() throws IOException {
		this.ensureChunkDataAvailable();
		int read = super.read();
		if (read < 0) {
			return read;
		}
		this.remaining--;
		return read;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		this.ensureChunkDataAvailable();
		int read = super.read(b, off, Math.min(len, this.remaining));
		if (read < 0) {
			return read;
		}
		this.remaining -= read;
		return read;
	}

	@Override
	public long skip(long n) throws IOException {
		long skipped = super.skip(Math.min(n, this.remaining));
		this.remaining -= skipped;
		return skipped;
	}

	public void openChunk() throws IOException {
		this.openChunk(-1, -1);
	}

	public void openChunk(int expectedType, int expectedLength) throws IOException {
		if (this.isChunkOpen()) {
			throw new IllegalStateException();
		}
		this.length = IOUtil.readInt(this.in, this.buffer);
		if (expectedLength >= 0 && this.length != expectedLength) {
			throw new IIOException("PNG chunk length mismatch");
		}
		this.type = IOUtil.readInt(this.in, this.buffer);
		if (expectedType >= 0 && this.type != expectedType) {
			throw new IIOException("PNG chunk type mismatch");
		}
		this.remaining = this.length;
	}

	public void closeChunk() throws IOException {
		this.closeChunk(false, 0);
	}

	public void closeChunk(boolean checkCrc, int calculatedCrc) throws IOException {
		if (!this.isChunkOpen()) {
			throw new IllegalStateException();
		}
		if (this.remaining > 0) {
			IOUtil.skip(this.in, this.remaining);
		}
		int crc = IOUtil.readInt(this.in, this.buffer);
		if (checkCrc && calculatedCrc != crc) {
			throw new IIOException("PNG chunk crc mismatch");
		}
		this.type = -1;
		this.length = -1;
		this.remaining = -1;
	}

	public boolean findChunk(int type, IntPredicate failCondition) throws IOException {
		if (!this.isChunkOpen()) {
			this.openChunk();
		}
		while (true) {
			if (this.type == type) {
				return true;
			}
			if (failCondition != null && failCondition.test(this.type)) {
				return false;
			}
			this.closeChunk();
			this.openChunk();
		}
	}

	public int type() {
		return this.type;
	}

	public int length() {
		return this.length;
	}

	public int remaining() {
		return this.remaining;
	}

}
