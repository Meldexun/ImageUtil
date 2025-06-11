package meldexun.imageutil.png;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.InflaterInputStream;

import javax.imageio.IIOException;

import meldexun.imageutil.Color;
import meldexun.imageutil.IOUtil;
import meldexun.imageutil.Image;
import meldexun.memoryutil.MemoryAccess;
import meldexun.memoryutil.UnsafeBufferUtil;
import meldexun.memoryutil.UnsafeByteBuffer;

@SuppressWarnings("unused")
public class PNGDecoder {

	private static final long SIGNATURE = 0x89504e470d0a1a0aL;
	private static final int CHUNK_IHDR = 0x49484452;
	private static final int CHUNK_PLTE = 0x504C5445;
	private static final int CHUNK_IDAT = 0x49444154;
	private static final int CHUNK_IEND = 0x49454e44;
	private static final int CHUNK_tRNS = 0x74524E53;
	private static final int CHUNK_acTL = 0x6163544c;
	private static final int CHUNK_fcTL = 0x6663544c;
	private static final int CHUNK_fdAT = 0x66644154;
	private static final int CHUNK_IHDR_LENGTH = 13;
	private static final int CHUNK_IEND_LENGTH = 0;
	private static final int CHUNK_acTL_LENGTH = 8;
	private static final int CHUNK_fcTL_LENGTH = 26;
	private static final int COMPRESSION_DEFLATE = 0;
	private static final int FILTER_DEFAULT = 0;
	private static final int INTERLACE_NO_INTERLACE = 0;
	private static final int INTERLACE_ADAM7_INTERLACE = 1;
	private static final int[] ADAM7_X_OFFSETS = { 0, 4, 0, 2, 0, 1, 0 };
	private static final int[] ADAM7_Y_OFFSETS = { 0, 0, 4, 0, 2, 0, 1 };
	private static final int[] ADAM7_X_STRIDES = { 8, 8, 4, 4, 2, 2, 1 };
	private static final int[] ADAM7_Y_STRIDES = { 8, 8, 8, 4, 4, 2, 2 };

	private static final int SCANLINE_PADDING = 8;
	private static final int SCANLINE_FILTER_INDEX = 7;

	private static void readSignature(PNGChunkReader in) throws IOException {
		if (in.readSignature() != SIGNATURE) {
			throw new IIOException("Signature does not match");
		}
	}

	public static Image decodeStatic(InputStream input) throws IOException {
		return decodeStatic(input, Color.RGBA);
	}

	public static Image decodeStatic(InputStream input, Color color) throws IOException {
		try (PNGChunkReader chunkReader = new PNGChunkReader(input)) {
			readSignature(chunkReader);

			chunkReader.openChunk(CHUNK_IHDR, CHUNK_IHDR_LENGTH);
			int width = chunkReader.readInt();
			int height = chunkReader.readInt();
			PNGBitDepth bitDepth = PNGBitDepth.valueOf(chunkReader.readByte());
			PNGColorType colorType = PNGColorType.byIndex(chunkReader.readByte());
			int compression = chunkReader.readByte();
			int filter = chunkReader.readByte();
			int interlace = chunkReader.readByte();
			chunkReader.closeChunk();

			if (compression != COMPRESSION_DEFLATE) throw new IIOException("Unsupported compression method");
			if (filter != FILTER_DEFAULT) throw new IIOException("Unsupported filter method");
			if (interlace != INTERLACE_NO_INTERLACE && interlace != INTERLACE_ADAM7_INTERLACE) throw new IIOException("Unsupported interlace method");

			byte[] PLTE = null;
			byte[] tRNS = null;
			if (colorType == PNGColorType.INDEXED_COLOR) {
				if (!chunkReader.findChunk(CHUNK_PLTE, type -> type == CHUNK_fcTL || type == CHUNK_IDAT || type == CHUNK_IEND)) {
					throw new IIOException("Missing PLTE chunk");
				}
				PLTE = new byte[chunkReader.length()];
				IOUtil.readFully(chunkReader, PLTE);
				chunkReader.closeChunk();

				if (chunkReader.findChunk(CHUNK_tRNS, type -> type == CHUNK_fcTL || type == CHUNK_IDAT || type == CHUNK_IEND)) {
					tRNS = new byte[chunkReader.length()];
					IOUtil.readFully(chunkReader, tRNS);
					chunkReader.closeChunk();
				}
			}

			if (!chunkReader.findChunk(CHUNK_IDAT, type -> type == CHUNK_IEND)) {
				throw new IIOException("Missing IDAT chunk");
			}
			UnsafeByteBuffer buffer = UnsafeBufferUtil.allocateByte(width * height * color.bytesPerPixel());
			decode(chunkReader, interlace, PLTE, tRNS, colorType, bitDepth, width, height, buffer, color);
			return new Image(width, height, color, buffer);
		}
	}

	public static CompressedAPNG readCompressed(InputStream input) throws IOException {
		try (PNGChunkReader chunkReader = new PNGChunkReader(input)) {
			readSignature(chunkReader);

			chunkReader.openChunk(CHUNK_IHDR, CHUNK_IHDR_LENGTH);
			int width = chunkReader.readInt();
			int height = chunkReader.readInt();
			PNGBitDepth bitDepth = PNGBitDepth.valueOf(chunkReader.readByte());
			PNGColorType colorType = PNGColorType.byIndex(chunkReader.readByte());
			int compression = chunkReader.readByte();
			int filter = chunkReader.readByte();
			int interlace = chunkReader.readByte();
			chunkReader.closeChunk();

			if (compression != COMPRESSION_DEFLATE) throw new IIOException("Unsupported compression method");
			if (filter != FILTER_DEFAULT) throw new IIOException("Unsupported filter method");
			if (interlace != INTERLACE_NO_INTERLACE && interlace != INTERLACE_ADAM7_INTERLACE) throw new IIOException("Unsupported interlace method");

			if (!chunkReader.findChunk(CHUNK_acTL, type -> type == CHUNK_IDAT || type == CHUNK_IEND)) {
				if (chunkReader.type() != CHUNK_IDAT) {
					throw new IIOException("Missing IDAT chunk");
				}
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				while (true) {
					IOUtil.copy(chunkReader, data, buffer, chunkReader.remaining());
					chunkReader.closeChunk();
					chunkReader.openChunk();
					if (chunkReader.type() != CHUNK_IDAT) {
						break;
					}
				}
				List<CompressedAPNG.Frame> frames = new ArrayList<>(1);
				frames.add(new CompressedAPNG.Frame(width, height, 0, 0, 1, 30, 0, 0, data.toByteArray()));
				return new CompressedAPNG(width, height, colorType, bitDepth, interlace, null, null, frames, 0);
			}
			List<CompressedAPNG.Frame> frames = new ArrayList<>(chunkReader.readInt());
			int plays = chunkReader.readInt();
			chunkReader.closeChunk();

			byte[] PLTE = null;
			byte[] tRNS = null;
			if (colorType == PNGColorType.INDEXED_COLOR) {
				if (!chunkReader.findChunk(CHUNK_PLTE, type -> type == CHUNK_fcTL || type == CHUNK_IDAT || type == CHUNK_IEND)) {
					throw new IIOException("Missing PLTE chunk");
				}
				PLTE = new byte[chunkReader.length()];
				IOUtil.readFully(chunkReader, PLTE);
				chunkReader.closeChunk();

				if (chunkReader.findChunk(CHUNK_tRNS, type -> type == CHUNK_fcTL || type == CHUNK_IDAT || type == CHUNK_IEND)) {
					tRNS = new byte[chunkReader.length()];
					IOUtil.readFully(chunkReader, tRNS);
					chunkReader.closeChunk();
				}
			}

			ByteArrayOutputStream data = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			while (chunkReader.findChunk(CHUNK_fcTL, type -> type == CHUNK_IEND)) {
				int sequence_number = chunkReader.readInt();
				int f_width = chunkReader.readInt();
				int f_height = chunkReader.readInt();
				int x_offset = chunkReader.readInt();
				int y_offset = chunkReader.readInt();
				int delay_num = chunkReader.readShort();
				int delay_den = chunkReader.readShort();
				int dispose_op = chunkReader.readByte();
				int blend_op = chunkReader.readByte();
				chunkReader.closeChunk();

				if (!chunkReader.findChunk(type -> type == CHUNK_IDAT || type == CHUNK_fdAT, type -> type == CHUNK_IEND)) {
					throw new IIOException("Missing IDAT/fdAT chunk");
				}
				data.reset();
				while (true) {
					if (chunkReader.type() == CHUNK_fdAT) {
						chunkReader.readInt();
					}
					IOUtil.copy(chunkReader, data, buffer, chunkReader.remaining());
					chunkReader.closeChunk();
					chunkReader.openChunk();
					if (chunkReader.type() != CHUNK_IDAT && chunkReader.type() != CHUNK_fdAT) {
						break;
					}
				}

				frames.add(new CompressedAPNG.Frame(f_width, f_height, x_offset, y_offset, delay_num, delay_den, dispose_op, blend_op, data.toByteArray()));
			}

			return new CompressedAPNG(width, height, colorType, bitDepth, interlace, PLTE, tRNS, frames, plays);
		}
	}

	public static void decodeFrame(CompressedAPNG apng, CompressedAPNG.Frame frame, MemoryAccess dst, Color dstColor) throws IOException {
		try (InputStream in = new ByteArrayInputStream(frame.data)) {
			decode(in, apng.interlace, apng.PLTE, apng.tRNS, apng.colorType, apng.bitDepth, frame.width, frame.height, dst, dstColor);
		}
	}

	public static void decode(InputStream input, int interlace, byte[] PLTE, byte[] tRNS, PNGColorType colorType, PNGBitDepth bitDepth, int width, int height, MemoryAccess dst, Color dstColor) throws IOException {
		try (InputStream in = new InflaterInputStream(input)) {
			int bitsPerPixel = colorType.channels() * bitDepth.value();
			byte[] scanline = new byte[(int) Math.ceil((double) (width * bitsPerPixel) / Byte.SIZE) + SCANLINE_PADDING];
			byte[] prevScanline = new byte[scanline.length];
			int scanlineOffset = bitDepth.value() < Byte.SIZE ? 1 : bitsPerPixel / Byte.SIZE;
			for (int pass = 0; pass < (interlace == INTERLACE_ADAM7_INTERLACE ? 7 : 1); pass++) {
				int offsetX;
				int offsetY;
				int strideX;
				int strideY;
				int pixelsPerScanline;
				int scanlinesPerPass;
				if (interlace == INTERLACE_ADAM7_INTERLACE) {
					offsetX = ADAM7_X_OFFSETS[pass];
					offsetY = ADAM7_Y_OFFSETS[pass];
					strideX = ADAM7_X_STRIDES[pass];
					strideY = ADAM7_Y_STRIDES[pass];
					pixelsPerScanline = width / strideX + (offsetX > 0 && width % strideX >= offsetX ? 1 : 0);
					scanlinesPerPass = height / strideY + (offsetY > 0 && height % strideY >= offsetY ? 1 : 0);
				} else {
					offsetX = 0;
					offsetY = 0;
					strideX = 1;
					strideY = 1;
					pixelsPerScanline = width;
					scanlinesPerPass = height;
				}
				int bytesPerScanline = (int) Math.ceil((double) (pixelsPerScanline * bitsPerPixel) / Byte.SIZE);
				for (int y = 0; y < scanlinesPerPass; y++) {
					IOUtil.readFully(in, scanline, SCANLINE_FILTER_INDEX, bytesPerScanline + 1);
					unfilter(scanline, prevScanline, scanlineOffset, bytesPerScanline + SCANLINE_PADDING);
					colorType.copyPixels(scanline, SCANLINE_PADDING, PLTE, tRNS, bitDepth, dst, (y * strideY + offsetY) * width * dstColor.bytesPerPixel(), dstColor, offsetX, strideX, pixelsPerScanline);
					byte[] tmp = scanline;
					scanline = prevScanline;
					prevScanline = tmp;
				}
			}
		}
	}

	private static void unfilter(byte[] scanline, byte[] prevScanline, int offset, int length) {
		byte filterType = scanline[SCANLINE_FILTER_INDEX];
		scanline[SCANLINE_FILTER_INDEX] = 0;
		switch (filterType) {
		case 0:
			break;
		case 1:
			unfilterSub(scanline, offset, length);
			break;
		case 2:
			unfilterUp(scanline, prevScanline, length);
			break;
		case 3:
			unfilterAverage(scanline, prevScanline, offset, length);
			break;
		case 4:
			unfilterPaeth(scanline, prevScanline, offset, length);
			break;
		default:
			throw new IllegalArgumentException("Unsupported filter type");
		}
	}

	private static void unfilterSub(byte[] scanline, int offset, int length) {
		for (int i = SCANLINE_PADDING; i < length; i++) {
			scanline[i] += scanline[i - offset];
		}
	}

	private static void unfilterUp(byte[] scanline, byte[] prevScanline, int length) {
		for (int i = SCANLINE_PADDING; i < length; i++) {
			scanline[i] += prevScanline[i];
		}
	}

	private static void unfilterAverage(byte[] scanline, byte[] prevScanline, int offset, int length) {
		for (int i = SCANLINE_PADDING; i < length; i++) {
			scanline[i] += average(scanline[i - offset], prevScanline[i]);
		}
	}

	private static void unfilterPaeth(byte[] scanline, byte[] prevScanline, int offset, int length) {
		for (int i = SCANLINE_PADDING; i < length; i++) {
			scanline[i] += paethPredictor(scanline[i - offset], prevScanline[i], prevScanline[i - offset]);
		}
	}

	private static byte average(byte a, byte b) {
		return (byte) average(a & 0xFF, b & 0xFF);
	}

	private static int average(int a, int b) {
		return (a + b) >> 1;
	}

	private static byte paethPredictor(byte a, byte b, byte c) {
		return (byte) paethPredictor(a & 0xFF, b & 0xFF, c & 0xFF);
	}

	private static int paethPredictor(int a, int b, int c) {
		int p = a + b - c;
		int pa = abs(p - a);
		int pb = abs(p - b);
		int pc = abs(p - c);
		if (pa <= pb && pa <= pc)
			return a;
		if (pb <= pc)
			return b;
		return c;
	}

	private static int abs(int x) {
		int m = x >> 31;
		return (x + m) ^ m;
	}

}
