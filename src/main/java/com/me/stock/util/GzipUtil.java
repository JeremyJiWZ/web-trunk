package com.me.stock.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GzipUtil {
	
	//  解压Gzip数据，（微盛请求式接口 采用Gzip压缩）
	// data就是接收到的数据；返回解压缩后的数据（字节数组）
	public static byte[] decompressGzip(byte[] data) {
		GZIPInputStream gis = null;
		try {
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				gis = new GZIPInputStream(bais);
				int count;
				byte data2[] = new byte[data.length];
				while ((count = gis.read(data2, 0, data.length)) != -1) {
				    baos.write(data2, 0, count);
				}
				gis.close();	  data = baos.toByteArray();
				baos.flush();	baos.close();	bais.close();            //         System.out.println("解压成功");
		} catch (IOException e) {
			e.printStackTrace();		//System.out.println(ex);           
		} finally {
			try {
				gis.close();
		} catch (IOException e) {
				e.printStackTrace();// System.out.println(ex);
            }
        }
        return data;
    }
}
