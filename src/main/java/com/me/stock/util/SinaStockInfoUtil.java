package com.me.stock.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.me.stock.meta.StockInfo;

public class SinaStockInfoUtil {
	static private Logger logger = Logger.getLogger(SinaStockInfoUtil.class);

	static public List<StockInfo> stockInfosFromString(String str) {
		List<StockInfo> stockInfos = new ArrayList<>();
		if (str == null) {
			// logger.info("get null from http get");
			return null;
		}
		String[] stockStrs = str.split("\n");
		for (String stockStr : stockStrs) {
			StockInfo stock = stockInfoFromString(stockStr);
			stockInfos.add(stock);
		}
		return stockInfos;
	}

	static public StockInfo stockInfoFromString(String str) {
		StockInfo stockInfo = new StockInfo();
		StringBuffer stringBuffer = new StringBuffer(str);
		int indexOfCode = 13;// stringBuffer.indexOf("var hq_str_sb")+13;
		int indexOfEq = stringBuffer.indexOf("=");
		String stockCode = stringBuffer.substring(indexOfCode, indexOfEq);
		String[] elements = stringBuffer.substring(indexOfEq + 2, stringBuffer.length() - 2).split(",");
		stockInfo.setCode(stockCode);
		setStockInfo(elements, stockInfo);
		return stockInfo;
	}

	static private void setStockInfo(String[] elements, StockInfo stockInfo) {
		if (stockInfo.getCode().indexOf("833553") >= 0) {
			logger.info("debugging");
		}
		stockInfo.setStockName(elements[0]);
		stockInfo.setOpenTdy(Float.valueOf(elements[1]));
		stockInfo.setClosedYdy(Float.valueOf(elements[2]));
		stockInfo.setCurrentPrice(Float.valueOf(elements[3]));
		stockInfo.setHighestTdy(Float.valueOf(elements[4]));
		stockInfo.setLowestTdy(Float.valueOf(elements[5]));
		stockInfo.setBuyValue(Float.valueOf(elements[6]));
		stockInfo.setSaleValue(Float.valueOf(elements[7]));
		stockInfo.setDealNum(Integer.valueOf(elements[8]));
		stockInfo.setB1n(Integer.valueOf(elements[10]));
		stockInfo.setB1v(Float.valueOf(elements[11]));
		stockInfo.setB2n(Integer.valueOf(elements[12]));
		stockInfo.setB2v(Float.valueOf(elements[13]));
		stockInfo.setS1n(Integer.valueOf(elements[20]));
		stockInfo.setS1v(Float.valueOf(elements[21]));
		stockInfo.setDate(elements[30]);
		stockInfo.setTime(elements[31]);
	}

	public static void main(String[] args) {
	}
}
