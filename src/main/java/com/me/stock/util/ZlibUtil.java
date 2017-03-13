package com.me.stock.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import com.me.stock.meta.StockInfo;

public class ZlibUtil {
	// 解压微盛wsz数据，（微盛推送式接口 采用wsz压缩）
	// data就是接收到的数据；返回解压缩后的数据（字节数组）
	// wstock.net 2015/11/05
	public static byte[] decompressWSZ(byte[] data) {
		byte[] output = new byte[0];
		String str = "";
		int pos = 0;

		while (pos + 4 <= data.length) {
			int dwZipSize = bytesToInt(data, pos); // 压缩块长度
			if (dwZipSize > 16777216 || dwZipSize <= 0) {
				// 这是一个信息块，不必处理；也可在此处存储到 日志文件中
				String str2 = new String(data);
				System.out.println("wsz message info:" + str2);
				break; // wsz中最多只包括一个信息块，因此如果为信息块，则处理后即可退出循环
			} else {
				if (pos + 4 > data.length) {
					break;
				} // 长度不足，终止循环
				int dwOriSize = bytesToInt(data, pos + 4);
				if (dwOriSize <= 0) {
					break;
				} // 解压缩后原始数据长度；如果不正确，则终止循环
				byte[] unzip = decompress(data, pos + 8, dwZipSize - 4); // 解压缩，因为dwZipSize中包括了原始数据4字节，因此这里需要
																			// dwZipSize-4
				pos = pos + 4 + dwZipSize; // 指定下次解压缩的位置；因为wsz中可能包括多个压缩块
				if (unzip == null) {
					System.out.println("wsz 解压缩错误，返回null.");
				} else if (unzip.length == 1 && unzip[0] == 0) {
					System.out.println("wsz 解压缩错误，返回1个字节0x00");
				} else if (unzip.length != dwOriSize) {
					System.out.println("wsz 解压缩错误，返回" + unzip.length + "个字节；而原始长度应该为：" + dwOriSize + "字节");
				} else {
					// AppendToFile("d:/temp/wsTest.dat",data,0,data.length); //
					// 原始数据追加存储到文件，这个是解压缩后的 二进制记录数据，可存储到文件中
					str += wsRpt156or64ToTXT(unzip); // 将二进制记录数据，转化为txt格式
				}
			}
		}
		if (str != null) {
			if (str.length() > 0) {
				try {
					output = str.getBytes("GBK"); // 这里将 字符串 转化为
													// 字节数组，必须指定正确的编码方式；这里采用的是GBK编码
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return output;
	}

	// 微盛Rpt156 或者 MT64 数据结构 转 txt
	// 自动识别是 156字节的数据结构，还是 MT市场（64字节）的数据结构
	// wstock.net 2015/11/05
	public static String wsRpt156or64ToTXT(byte[] b) {
		boolean boolMT = false;
		int recLen = 156; // 默认每条记录156字节

		if (b.length < 64) {
			return "error,b.length<64,  b.length=" + b.length;
		} // 至少应该有64字节

		String str = "时间,代码,名称,总笔数或前结算价,现量,持仓或保留,结算价或保留,前收盘价,开盘价,最高价,最低价,最新价,总量,总金额,委买价1,委买价2,委买价3,委买价4,委买价5,委买量1,委买量2,委买量3,委买量4,委买量5,委卖价1,委卖价2,委卖价3,委卖价4,委卖价5,委卖量1,委卖量2,委卖量3,委卖量4,委卖量5\r\n"; // 字段名称
		if (b[4] == 70 && b[5] == 88 && b.length % 64 == 0) {
			boolMT = true;
			recLen = 64;
			str = "时间,代码,名称,开盘价,最高价,最低价,委买价1,委卖价1\r\n";
		} // MT市场的代码为FX字头。FX的ASCII码为70、88，因此通过此特征，结合每条记录长度为64字节来判断
		if (!boolMT) {
			if (b.length % 156 != 0 || b.length < 156) {
				return "error,b.length=" + b.length;
			}
		}

		for (int i = 0; i < b.length; i += recLen) {
			String s2 = "";
			// 转化时间
			int k = bytesToInt(b, i);
			long k2 = k;
			k2 = k2 * 1000;
			Timestamp ts = new Timestamp(k2); // System.out.println("k=" + k +
												// "\tk2=" + k2 + "\tts=" + ts);
			DateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				s2 = sdf.format(ts);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 股票代码
			String m_szLabel = "";
			m_szLabel = new String(b, i + 4, 12);
			k = m_szLabel.indexOf("\00");
			if (k >= 0) {
				m_szLabel = m_szLabel.substring(0, k);
			}
			String m_szName = "";

			// 股票的中文简称
			try {
				m_szName = new String(b, i + 16, (boolMT ? 12 : 16), "GBK");
				k = m_szName.indexOf("\00");
				if (k >= 0) {
					m_szName = m_szName.substring(0, k);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			s2 = s2 + "," + m_szLabel + "," + m_szName;

			if (boolMT) { // 每条记录64字节的情况，首先是4字节的float，然后是4个8字节的double
				float f = bytesToFloat(b, i + 28);
				s2 += String.format(",%1$.5f", f); // 开盘价
				for (int j = 0; j < 4; j++) { // 每条记录64字节的情况，从32字节开始，以后总共4个double，利用循环转出
					double d = bytesToDouble(b, i + 32 + j * 8);
					s2 += String.format(",%1$.5f", d);
				}
			} else {
				for (int j = 0; j < 31; j++) { // 每条记录156字节的情况，从32字节开始，之后全部为float，总共31个float，利用循环转出
					float f = bytesToFloat(b, i + 32 + j * 4);
					s2 += String.format(",%1$.3f", f);
				}
			}

			str += s2 + "\r\n";
		}
		return str;
	}

	// 字节数组转整型，该函数中字节数组是高字节在后的方式；java中，byte、int、long均是带符号方式（有正负）
	// wstock.net 2015/11/04
	public static int bytesToInt(byte[] b, int pos) {
		if (b.length < pos + 4) {
			return 0;
		}
		// int i = (b[pos] << 24) & 0xFF000000; i |= (b[pos+1] << 16) &
		// 0xFF0000; i |= (b[pos+2] << 8) & 0xFF00; i |= b[pos+3] & 0xFF; //
		// 高字节在前
		int i = (b[pos + 3] << 24) & 0xFF000000;
		i |= (b[pos + 2] << 16) & 0xFF0000;
		i |= (b[pos + 1] << 8) & 0xFF00;
		i |= b[pos] & 0xFF; // 高字节在后
		return i;
	}

	// java字节数组转8字节整型，本函数中对应的 字节数组，采用的是高字节在后的方式
	// 如果不强制转换为long，那么默认会当作int，导致最高32位丢失
	public static long bytesToLong(byte[] b, int pos) {
		if (b.length < pos + 4) {
			return 0;
		}
		long l = ((long) b[pos + 7] << 56) & 0xFF00000000000000L;
		l |= ((long) b[pos + 6] << 48) & 0xFF000000000000L;
		l |= ((long) b[pos + 5] << 40) & 0xFF0000000000L;
		l |= ((long) b[pos + 4] << 32) & 0xFF00000000L;
		l |= ((long) b[pos + 3] << 24) & 0xFF000000L;
		l |= ((long) b[pos + 2] << 16) & 0xFF0000L;
		l |= ((long) b[pos + 1] << 8) & 0xFF00L;
		l |= (long) b[pos] & 0xFFL;
		return l;
	}

	// 字节数组 转 double
	public static double bytesToDouble(byte[] b, int pos) {
		return Double.longBitsToDouble(bytesToLong(b, pos));
	}

	// 字节数组转 float
	public static float bytesToFloat(byte[] b, int pos) {
		return Float.intBitsToFloat(bytesToInt(b, pos));
	}

	// 解压缩zlib数据（微盛推送式接口 采用zlib压缩）
	// @param data 待解压缩的数据
	// @return byte[] 解压缩后的数据
	public static byte[] decompress(byte[] data, int offset, int length) {
		byte[] output = new byte[0];

		Inflater decompresser = new Inflater();
		decompresser.reset();
		decompresser.setInput(data, offset, length);

		ByteArrayOutputStream o = new ByteArrayOutputStream(data.length); // ByteArrayOutputStream会自动管理内存空间，这里用
																			// data.length只是一个初始化长度（压缩块长度，实际返回时，已变更为解压缩后的长度）
		try {
			byte[] buf = new byte[1024];
			while (!decompresser.finished()) {
				int i = decompresser.inflate(buf);
				o.write(buf, 0, i);
			}
			output = o.toByteArray();
		} catch (Exception e) {
			// output = data; // 如需要，出现异常后，也可返回原始数据
			e.printStackTrace();
		} finally {
			try {
				o.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		decompresser.end();
		return output;
	}

	static public int getI() {
		int i = 0;
		i++;
		return i;
	}

	static public void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(getI());
		}
	}

	public static List<StockInfo> parseStockBytes(byte[] b) {
		boolean boolMT = false;
		int recLen = 156; // 默认每条记录156字节

		if (!boolMT) {
			if (b.length % 156 != 0 || b.length < 156) {
				return null; // 无法解析数据，不符合格式要求
			}
		}

		List<StockInfo> stockInfoList = new ArrayList<>();
		for (int i = 0; i < b.length; i += recLen) {
			StockInfo stockInfo = new StockInfo();

			// 转化时间
			int k = bytesToInt(b, i);
			long k2 = k;
			k2 = k2 * 1000;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = sdf.format(k2);
			stockInfo.setDate(time.substring(0, 10));
			stockInfo.setTime(time.substring(11, time.length()));

			// 股票代码
			String m_szLabel = "";
			m_szLabel = new String(b, i + 4, 12);
			k = m_szLabel.indexOf("\00");
			if (k >= 0) {
				m_szLabel = m_szLabel.substring(0, k);
				stockInfo.setCode(m_szLabel);
			}

			// 股票的中文简称
			String m_szName = "";
			try {
				m_szName = new String(b, i + 16, (boolMT ? 12 : 16), "GBK");
				k = m_szName.indexOf("\00");
				if (k >= 0) {
					m_szName = m_szName.substring(0, k);
					String nameUtf = new String(m_szName.getBytes(), "utf-8");
					stockInfo.setStockName(nameUtf);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// for (int j = 0; j < 31; j++) { //
			// 每条记录156字节的情况，从32字节开始，之后全部为float，总共31个float，利用循环转出
			// float f = bytesToFloat(b, i + 32 + j * 4);
			// s2 += String.format(",%1$.3f", f);
			// }
			// 31个float
			float totalNum = bytesToFloat(b, i + 32);
			float volNow = bytesToFloat(b, i + 36);
			float b1v = bytesToFloat(b, i + 32 + 4 * 11);
			float b2v = bytesToFloat(b, i + 32 + 4 * 12);
			float b1n = bytesToFloat(b, i + 32 + 4 * 16);
			float b2n = bytesToFloat(b, i + 32 + 4 * 17);
			float s1v = bytesToFloat(b, i + 32 + 4 * 21);
			float s2v = bytesToFloat(b, i + 32 + 4 * 22);
			float s1n = bytesToFloat(b, i + 32 + 4 * 26);
			float s2n = bytesToFloat(b, i + 32 + 4 * 27);
			stockInfo.setDealNum((int) totalNum);
			stockInfo.setCurrentPrice(volNow);
			stockInfo.setB1v(b1v);
			stockInfo.setB2v(b2v);
			stockInfo.setB1n((int) b1n);
			stockInfo.setB2n((int) b2n);
			stockInfo.setS1v(s1v);
			stockInfo.setS2v(s2v);
			stockInfo.setS1n((int) s1n);
			stockInfo.setS2n((int) s2n);
			stockInfoList.add(stockInfo);
		}
		return stockInfoList;
	}

	// 解压微盛wsz数据，（微盛推送式接口 采用wsz压缩）
	// data就是接收到的数据；返回解压缩后的数据
	public static List<StockInfo> parseWsz(byte[] data) {
		int pos = 0;
		List<StockInfo> stockInfoList = new ArrayList<>();
		if (data == null) {
			return null;
		}
		while (pos + 4 <= data.length) {
			int dwZipSize = bytesToInt(data, pos); // 压缩块长度
			if (dwZipSize > 16777216 || dwZipSize <= 0) {
				// 这是一个信息块，不必处理；也可在此处存储到 日志文件中
				String str2 = new String(data);
				System.out.println("wsz message info:" + str2);
				break; // wsz中最多只包括一个信息块，因此如果为信息块，则处理后即可退出循环
			} else {
				if (pos + 4 > data.length) {
					break;
				} // 长度不足，终止循环
				int dwOriSize = bytesToInt(data, pos + 4);
				if (dwOriSize <= 0) {
					break;
				} // 解压缩后原始数据长度；如果不正确，则终止循环
				byte[] unzip = decompress(data, pos + 8, dwZipSize - 4); // 解压缩，因为dwZipSize中包括了原始数据4字节，因此这里需要
																			// dwZipSize-4
				pos = pos + 4 + dwZipSize; // 指定下次解压缩的位置；因为wsz中可能包括多个压缩块
				if (unzip == null) {
					System.out.println("wsz 解压缩错误，返回null.");
				} else if (unzip.length == 1 && unzip[0] == 0) {
					System.out.println("wsz 解压缩错误，返回1个字节0x00");
				} else if (unzip.length != dwOriSize) {
					System.out.println("wsz 解压缩错误，返回" + unzip.length + "个字节；而原始长度应该为：" + dwOriSize + "字节");
				} else {
					stockInfoList.addAll(parseStockBytes(unzip));
				}
			}
		}
		return stockInfoList;
	}
}
