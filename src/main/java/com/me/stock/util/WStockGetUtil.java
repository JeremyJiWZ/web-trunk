package com.me.stock.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class WStockGetUtil {
	// 通过字节数组下载网页,这种方法更灵活，支持下载（抓取）图片、文件，以及压缩格式gzip的网页
		// 这里通过java的 ByteArrayOutputStream 来作为缓存，代码会简单些（不用自己管理缓存大小）
		// 返回下载的数据总长度（字节）；下载的数据通过data参数返回
		// wstock.net 2015/10/12
		public static byte[] wsGetHTMLByByte(String strURL){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();		// 初始化缓存
			
			try{
				URL url = new URL(strURL);
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(10*1000);				// 设置超时间为10秒
				conn.setRequestProperty("User-Agent", "wstock java demo:wsGetHTMLByByte");   //告诉服务器，自己程序的名称。
				
				InputStream in = conn.getInputStream();		// 输入流
				byte[] bs = new byte[10240];			// 每次最多读取10KB
				int len = 0;	int total_size=0;	
				// 循环判断，如果bs为空，则in.read()方法返回-1，具体请参考InputStream的read();
				while ((len = in.read(bs)) >0 ) { 			// while ((len = in.read(bs)) != -1) {
					baos.write(bs, 0, len);					// 将新收到的数据放入缓存baos尾部， java的 ByteArrayOutputStream 会自动管理缓存大小
					total_size+=len;	// System.out.println("downloading:" + total_size + "/" + total_len );   // 显示下载进度
				}
				in.close();
				if (total_size>0) {
					byte[] data = baos.toByteArray();
					baos.flush();
					baos.close();
					return data;
				} else {
					return null;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		// 下载指定URL的数据
		//  三个参数，依次为 URL、编码方式、存储的文件名
		// wstock.net  2015/11/06
//		private static int wsDownload(String URL, String encoding, String filename) {
//			byte[] html_buffer=wsGetHTMLByByte(URL);			// 下载文件 或者 获取对应网页的数据
//			if (html_buffer==null) {System.out.println("get 0 byte,return."); return -1;}
//			
//			// gzip解压缩
//			String str=ContentEncoding;
//			if ( str != null) {
//				if ( str.toLowerCase().indexOf("gzip")>=0 ) {		// 是gzip压缩方式，需要先解压缩
//					html_buffer=decompressGzip(html_buffer);		// 解压缩
//					System.out.println("gzip unzip OK.");
//				}
//			}
//			// gzip解压缩
//			
//			// zlib（wsz格式）解压缩
//			if ( boolWSZ  && html_buffer.length>=4) {		// 是wsz压缩方式，需要先解压缩
//				// AppendToFile("d:/temp/wsTest.wsz",html_buffer,0,html_buffer.length);		// 原始wsz数据可追加存储到文件，这个是二进制压缩数据
//				html_buffer=decompressWSZ(html_buffer);		// wsz格式解压缩
//				encoding="GBK";				// wsz格式是二进制数据，其中的“股票中文简称”采用的是GBK编码。因此设置编码方式为GBK
//				System.out.println("wsz unzip OK.");
//			}
//			// zlib（wsz格式）解压缩
//
//			if (filename.length()>0 && html_buffer.length > 0) {		// 存储到文件
//				AppendToFile(filename,html_buffer,0,html_buffer.length);		// 追加存储到文件
//				System.out.println("下载的数据已存储到文件：" + filename);
//			} else {			// 显示下载的内容
//				// 提取 编码方式
//				if ( encoding.length()<2 ) {
//					str=ContentType; if (str!=null ) { str=getCharset(str); if (str!=null ) { encoding=str;	} }  // 如果用户没有手动指定编码方式，则尝试从服务器端返回数据中提取 charset
//				} 
//				if ( encoding.length()<2 ) {		// 尝试从数据中提取 字符集，例如 html中的 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
//					int i=1024; if (i>html_buffer.length) {i=html_buffer.length;} 	str = new String(html_buffer, 0, i);		// 这里将 字节数组最多前面1K字节 转化为 字符串，直接按默认字符集转化，以便提取charset
//					if (str!=null ) { str=getCharset(str); if (str!=null ) { encoding=str;	} }
//				}
//				if ( encoding.length()<2 ) { encoding="utf-8"; }				// 如果无法正确提取到编码方式，则默认为utf-8
//				System.out.println("encoding:" + encoding);
//				// 提取 编码方式
//
//				try {
//					str = new String(html_buffer, 0, html_buffer.length, encoding);		// 这里将 字节数组 转化为 字符串，必须指定正确的编码方式（encoding）
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//				System.out.println(str);
//			}
//			return 0;
//		}
}
