package meldexun.imageutil.gif;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOException;

import meldexun.imageutil.Color;
import meldexun.imageutil.IOUtil;
import meldexun.imageutil.gif.CompressedGIF.Frame.GraphicControl;
import meldexun.memoryutil.MemoryAccess;

@SuppressWarnings("unused")
public class GIFDecoder {

	private static final int SIGNATURE = 'G' | 'I' << 8 | 'F' << 16;
	private static final int GIF87a = '8' | '7' << 8 | 'a' << 16;
	private static final int GIF89a = '8' | '9' << 8 | 'a' << 16;

	private static final int GLOBAL_COLOR_TABLE_SIZE_MASK = 0b111;
	private static final int COLOR_TABLE_SORT_FLAG =       0b1000;
	private static final int COLOR_RESOLUTION_MASK =    0b1110000;
	private static final int GLOBAL_COLOR_TABLE_FLAG = 0b10000000;

	private static final int LOCAL_COLOR_TABLE_SIZE_MASK =      0b111;
	private static final int SORT_FLAG =                     0b100000;
	private static final int INTERLACE_FLAG =               0b1000000;
	private static final int LOCAL_COLOR_TABLE_FLAG =      0b10000000;

	private static final int LOCAL_IMAGE_DESCRIPTOR = 0x2C;
	private static final int TRAILER = 0x3B;
	private static final int EXTENSION = 0x21;
	private static final int GRAPHICS_CONTROL_EXTENSION_BLOCK = 0xF9;
	private static final int PLAIN_TEXT_EXTENSION_BLOCK = 0x01;
	private static final int APPLICATION_EXTENSION_BLOCK = 0xFF;
	private static final int COMMENT_EXTENSION_BLOCK = 0xFE;

	private static void readSignature(GIFInputStream in) throws IOException {
		if (in.read3Byte() != SIGNATURE) {
			throw new IIOException("Signature does not match");
		}
	}

	public static CompressedGIF decode(InputStream input) throws IOException {
		try (GIFInputStream in = new GIFInputStream(input)) {
			readSignature(in);
			int version = in.read3Byte();
			int width = in.readShort();
			int height = in.readShort();
			int packed = in.readByte();
			int backgroundColor = in.readByte();
			int aspectRatio = in.readByte();
			byte[] globalColorTable = null;
			if ((packed & GLOBAL_COLOR_TABLE_FLAG) != 0) {
				globalColorTable = new byte[(1 << ((packed & GLOBAL_COLOR_TABLE_SIZE_MASK) + 1)) * 3];
				IOUtil.readFully(in, globalColorTable);
			}

			List<CompressedGIF.Frame> frames = new ArrayList<>();
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			GraphicControl graphicControl = null;
			int separator;
			while ((separator = in.readByte()) != TRAILER) {
				if (separator == EXTENSION) {
					int label = in.readByte();
					switch (label) {
					case GRAPHICS_CONTROL_EXTENSION_BLOCK:
						IOUtil.skip(in, 1);
						int gc_packed = in.readByte();
						int delay = in.readShort();
						int transparentColorIndex = in.readByte();
						IOUtil.skip(in, 1);
						graphicControl = new CompressedGIF.Frame.GraphicControl(gc_packed, delay, transparentColorIndex);
						break;
					case PLAIN_TEXT_EXTENSION_BLOCK:
						skipPlainTextExtensionBlock(in);
						break;
					case APPLICATION_EXTENSION_BLOCK:
						skipApplicationExtensionBlock(in);
						break;
					case COMMENT_EXTENSION_BLOCK:
						skipCommentExtensionBlock(in);
						break;
					default:
						throw new IIOException("Unknown extension block");
					}
				} else if (separator == LOCAL_IMAGE_DESCRIPTOR) {
					int left = in.readShort();
					int top = in.readShort();
					int f_width = in.readShort();
					int f_height = in.readShort();
					int f_packed = in.readByte();
					byte[] localColorTable = null;
					if ((f_packed & LOCAL_COLOR_TABLE_FLAG) != 0) {
						localColorTable = new byte[(1 << ((f_packed & LOCAL_COLOR_TABLE_SIZE_MASK) + 1)) * 3];
						IOUtil.readFully(in, localColorTable);
					}

					data.reset();
					data.write(in.readByte());
					int size;
					while ((size = in.readByte()) != 0) {
						IOUtil.copy(in, data, buffer, size);
					}

					frames.add(new CompressedGIF.Frame(left, top, f_width, f_height, f_packed, localColorTable, data.toByteArray(), graphicControl));
					graphicControl = null;
				} else {
					throw new IIOException("Unkown separator");
				}
			}

			return new CompressedGIF(width, height, packed, backgroundColor, aspectRatio, globalColorTable, frames);
		}
	}

	public static void decodeFrame(CompressedGIF.Frame frame, byte[] globalColorTable, MemoryAccess dst, long dstOffset, Color dstColor) throws IOException {
		byte[] colorTable = frame.localColorTable != null ? frame.localColorTable : globalColorTable;
		int transparentColorIndex = frame.graphicControl != null ? frame.graphicControl.transparentColorIndex : -1;
		try (DataInputStream in = new DataInputStream(new LZWInputStream(new ByteArrayInputStream(frame.data)))) {
			decode(in, colorTable, transparentColorIndex, frame.width, frame.height, dst, dstOffset, dstColor);
		}
	}

	private static void decode(DataInputStream input, byte[] colorTable, int transparentColorIndex, int width, int height, MemoryAccess dst, long dstOffset, Color dstColor) throws IOException {
		try (DataInputStream in = input) {
			if (dstColor.hasAlpha()) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						long o = dstOffset + ((y * width + x) * dstColor.bytesPerPixel());
						int i = IOUtil.read(in);
						dst.putByte(o + dstColor.redOffset(), colorTable[i * 3 + 0]);
						dst.putByte(o + dstColor.greenOffset(), colorTable[i * 3 + 1]);
						dst.putByte(o + dstColor.blueOffset(), colorTable[i * 3 + 2]);
						dst.putByte(o + dstColor.alphaOffset(), i == transparentColorIndex ? 0 : (byte) 255);
					}
				}
			} else {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						long o = dstOffset + ((y * width + x) * dstColor.bytesPerPixel());
						int i = IOUtil.read(in);
						dst.putByte(o + dstColor.redOffset(), colorTable[i * 3 + 0]);
						dst.putByte(o + dstColor.greenOffset(), colorTable[i * 3 + 1]);
						dst.putByte(o + dstColor.blueOffset(), colorTable[i * 3 + 2]);
					}
				}
			}
		}
	}

	private static void skipPlainTextExtensionBlock(GIFInputStream in) throws IOException {
		IOUtil.skip(in, 13);
		skipSubBlocks(in);
	}

	private static void skipApplicationExtensionBlock(GIFInputStream in) throws IOException {
		IOUtil.skip(in, 12);
		skipSubBlocks(in);
	}

	private static void skipCommentExtensionBlock(GIFInputStream in) throws IOException {
		skipSubBlocks(in);
	}

	private static void skipSubBlocks(GIFInputStream in) throws IOException {
		int size;
		while ((size = in.readByte()) != 0) {
			IOUtil.skip(in, size);
		}
	}

}
