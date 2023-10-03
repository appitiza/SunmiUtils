package net.appitiza.lib.sunmiutils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class BytesUtil {


	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	@SuppressLint("DefaultLocale")
	public static byte[] getBytesFromDecString(String decstring){
		if(decstring == null || decstring.equals("")){
			return null;
		}
		decstring = decstring.replace(" ", "");
		int size = decstring.length()/2;
		char[] decarray = decstring.toCharArray();
		byte[] rv = new byte[size];
		for(int i=0; i<size; i++){
			int pos = i * 2;
			rv[i] = (byte) (charToByte(decarray[pos])*10 + charToByte(decarray[pos + 1]));
		}
		return rv;
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	public static byte[] getZXingQRCode(String qr1, String qr2, int size) {
		try {
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix1 = new QRCodeWriter().encode(qr1, BarcodeFormat.QR_CODE,
					size, size, hints);
			BitMatrix bitMatrix2 = new QRCodeWriter().encode(qr2, BarcodeFormat.QR_CODE,
					size, size, hints);
			return getBytesFromBitMatrix(bitMatrix1, bitMatrix2, 40);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] getBytesFromBitMatrix(BitMatrix bits1, BitMatrix bits2, int space) {
		if (bits1 == null || bits2 == null) return null;

		int h1 = bits1.getHeight();
		int w1 = bits1.getWidth();
		int h2 = bits2.getHeight();
		int w2 = bits2.getWidth();
		int h = Math.max(h1, h2);
		int w = (w1 + w2 + space + 7)/8;

		byte[] rv = new byte[h * w + 4];

		rv[0] = (byte) w;//xL
		rv[1] = (byte) (w >> 8);//xH
		rv[2] = (byte) h;
		rv[3] = (byte) (h >> 8);

		int k = 4;
		byte b;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				for (int n = 0; n < 8; n++) {
					int pos = j * 8 + n;
					if(pos < w1) {
						if(i < h1) {
							b = getBitMatrixColor(bits1, pos, i);
						} else {
							b = 0;
						}
						rv[k] += rv[k] + b;
					} else if(pos < (w1 + space)) {
						rv[k] += rv[k];
					} else {
						if(i < h2) {
							b = getBitMatrixColor(bits2, pos - w1 - space, i);
						} else {
							b = 0;
						}
						rv[k] += rv[k] + b;
					}
				}
				k++;
			}
		}
		return rv;
	}

	private static byte getBitMatrixColor(BitMatrix bits, int x, int y) {
		int width = bits.getWidth();
		int height = bits.getHeight();
		if (x >= width || y >= height || x < 0 || y < 0) return 0;
		if (bits.get(x, y)) {
			return 1;
		} else {
			return 0;
		}
	}

	public static byte[] getBytesFromBitMap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int bw = (width - 1) / 8 + 1;

		byte[] rv = new byte[height * bw + 4];
		rv[0] = (byte) bw;//xL
		rv[1] = (byte) (bw >> 8);//xH
		rv[2] = (byte) height;
		rv[3] = (byte) (height >> 8);

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int clr = pixels[width * i + j];
				int red = (clr & 0x00ff0000) >> 16;
				int green = (clr & 0x0000ff00) >> 8;
				int blue = clr & 0x000000ff;
				byte gray = (RGB2Gray(red, green, blue));
				rv[bw*i + j/8 + 4] = (byte) (rv[bw*i + j/8 + 4] | (gray << (7 - j % 8)));
			}
		}

		return rv;
	}

	public static byte[] getBytesFromBitMap(Bitmap bitmap, int mode) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width*height];
		if(mode == 0 || mode == 1){
			byte[] res = new byte[width*height/8 + 5*height/8];
			bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
			for(int i = 0; i < height/8; i++){
				res[0 + i*(width+5)] = 0x1b;
				res[1 + i*(width+5)] = 0x2a;
				res[2 + i*(width+5)] = (byte) mode;
				res[3 + i*(width+5)] = (byte) (width%256);
				res[4 + i*(width+5)] = (byte) (width/256);
				for(int j = 0; j < width; j++){
					byte gray = 0;
					for(int m = 0; m < 8; m++){
						int clr = pixels[j + width*(i*8+m)];
						int red = (clr & 0x00ff0000) >> 16;
						int green = (clr & 0x0000ff00) >> 8;
						int blue = clr & 0x000000ff;
						gray = (byte) ((RGB2Gray(red, green, blue)<<(7-m))|gray);
					}
					res[5 + j + i*(width+5)] = gray;
				}
			}
			return res;
		}else if(mode == 32 || mode == 33){
			byte[] res = new byte[width*height/8 + 5*height/24];
			bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
			for(int i = 0; i < height/24; i++){
				res[0 + i*(width*3+5)] = 0x1b;
				res[1 + i*(width*3+5)] = 0x2a;
				res[2 + i*(width*3+5)] = (byte) mode;
				res[3 + i*(width*3+5)] = (byte) (width%256);
				res[4 + i*(width*3+5)] = (byte) (width/256);
				for(int j = 0; j < width; j++){
					for(int n = 0; n < 3; n++){
						byte gray = 0;
						for(int m = 0; m < 8; m++){
							int clr = pixels[j + width*(i*24 + m + n*8)];
							int red = (clr & 0x00ff0000) >> 16;
							int green = (clr & 0x0000ff00) >> 8;
							int blue = clr & 0x000000ff;
							gray = (byte) ((RGB2Gray(red, green, blue)<<(7-m))|gray);
						}
						res[5 + j*3 + i*(width*3+5) + n] = gray;
					}
				}
			}
			return res;
		}else{
			return new byte[]{0x0A};
		}

	}



	private static byte RGB2Gray(int r, int g, int b) {
		return (false ? ((int) (0.29900 * r + 0.58700 * g + 0.11400 * b) > 200)
				: ((int) (0.29900 * r + 0.58700 * g + 0.11400 * b) < 200)) ? (byte) 1 : (byte) 0;
	}

}



