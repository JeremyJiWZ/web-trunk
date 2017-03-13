package com.me.stock.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.me.common.util.HttpUtil;
import com.me.stock.dao.StockNoticeDao;
import com.me.stock.meta.StockInfo;
import com.me.stock.meta.StockNotice;
import com.me.stock.util.SinaStockInfoUtil;
import com.me.stock.util.WStockGetUtil;
import com.me.stock.util.ZlibUtil;

@Component
public class StockService {
	static private Logger logger = Logger.getLogger(StockService.class);
	static private List<String> sinaUrls = new ArrayList<>();
	static private Properties peProperties;
	static private final String sinaBaseUrl = "http://hq.sinajs.cn/list=";
	static private final String wStockUrl = "http://ip136.wstock.cn/cgi-bin/wsRTAPI/wsr2.asp?m=SO&u=u8868&p=it9322";

	@Resource
	private StockNoticeDao stockNoticeDao;
	private Set<String> codeList = ConcurrentHashMap.<String> newKeySet();

	@SuppressWarnings("rawtypes")
	@PostConstruct
	private void init() throws IOException {
		logger.info("intialize Stock Service...");
		Properties properties = new Properties();
		peProperties = new Properties();
		InputStream in, peInputStream;
		try {
			in = getClass().getResourceAsStream("/xsbcode.properties");
			peInputStream = getClass().getResourceAsStream("/pe.properties");
			properties.load(in);
			peProperties.load(peInputStream);
			Enumeration e = properties.propertyNames();
			while (e.hasMoreElements()) {
				generateUrlByProperties(e);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info(sinaUrls.toString());

		// String urlBase =
		// "http://www.neeq.com.cn/nqhqController/detailCompany.do?zqdm=";
		// Properties properties = new Properties();
		// Properties outProperties = new Properties();
		// InputStream in;
		// OutputStream out = new
		// FileOutputStream("/Users/jiwentadashi/Desktop/a.properties");
		// try {
		// in = getClass().getResourceAsStream("/xsbcode.properties");
		//
		// properties.load(in);
		// Enumeration e = properties.propertyNames();
		// while (e.hasMoreElements()) {
		// String code = (String) e.nextElement();
		// float earningPerShare = getEarningPerShare(urlBase+code);
		// outProperties.setProperty(code, earningPerShare+"");
		// }
		// outProperties.store(out, "test");
		// out.close();
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// logger.info("done!");
	}

	// 后台线程：执行一个http请求刷股票列票是否有符合要求的
	private class StockRefreshThread extends Thread {
		int i;
		boolean wStockData;

		public StockRefreshThread(int i, boolean wstockData) {
			this.i = i;
			this.wStockData = wstockData;
		}

		@Override
		public void run() {
			List<StockInfo> stockInfos;
			long httpGetStart = System.currentTimeMillis();
			if (wStockData) { // 获取wstock数据
				byte[] bytes = WStockGetUtil.wsGetHTMLByByte(wStockUrl);
				stockInfos = ZlibUtil.parseWsz(bytes);
			} else { // 获取新浪数据
				String result = HttpUtil.httpGet(sinaUrls.get(i));
				stockInfos = SinaStockInfoUtil.stockInfosFromString(result);
			}
			long httpGetEnd = System.currentTimeMillis();

			if (stockInfos == null || stockInfos.size() <= 0)
				return;
			// 计算延迟时间
			long delay = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			StockInfo stockInfo = stockInfos.get(0);
			try {
				Date date = sdf.parse(stockInfo.getDate() + " " + stockInfo.getTime());
				delay = (httpGetEnd - date.getTime()) / 1000;
			} catch (ParseException e) {
			}

			// 扫描
			for (StockInfo info : stockInfos) {
				if (codeList.contains(info.getCode())) {
					continue;
				}
				if (info.getS1n() < 1) {
					continue;
				}
				StockNotice stockNotice;
				float earningPerShare = Float.valueOf(peProperties.getProperty(info.getCode()));
				float peRate = info.getS1v() / earningPerShare;
				if (peRate > 0 && peRate < 3) { // 市盈率5倍以下，0以上
					stockNotice = new StockNotice();
					stockNotice.setPeType((byte) 1);
					codeList.add(info.getCode());
				} else if ((info.getB1v() - info.getS1v()) / info.getS1v() > 0.05) {
					stockNotice = new StockNotice();
					stockNotice.setPeType((byte) 0);
					if (info.getB2v() - info.getS1v() > 0) {
						stockNotice.setPeType((byte) 2);
					}
					codeList.add(info.getCode());
				} else {
					continue;
				}
				stockNotice.setB1n(info.getB1n());
				stockNotice.setB1v(info.getB1v());
				stockNotice.setS1n(info.getS1n());
				stockNotice.setS1v(info.getS1v());
				stockNotice.setCode(info.getCode());
				stockNotice.setIsRead((byte) 0);
				stockNotice.setStockName(info.getStockName());
				stockNotice.setTime(System.currentTimeMillis());
				stockNoticeDao.insert(stockNotice);

				logger.info(info.getStockName() + ": 代码:" + info.getCode());
			}
			long threadEndTime = System.currentTimeMillis();
			logger.info("Thread" + i + ":" + (httpGetEnd - httpGetStart) + ", process: " + (threadEndTime - httpGetEnd)
					+ ", delay: " + delay);

		}
	}

	@Scheduled(cron = "*/1 * 9-11,13-16 * * 1-5")
	private void refreshWstockData() {
		Thread thread = new StockRefreshThread(0, true);
		thread.start();
		logger.info("scanning data from wstock...");
	}

	@Scheduled(cron = "*/1 * 9-11,13-16 * * 1-5")
	private void refreshSinaData() {
		for (int i = 0; i < sinaUrls.size(); i++) {
			Thread thread = new StockRefreshThread(i, false);
			thread.start();
		}
		logger.info("scanning data from sina...");
	}

	@Scheduled(cron = "0 30 12,18 * * 1-5")
	private void refreshCodeList() {
		codeList.clear();
	}

	public List<StockNotice> getUnreadNotice() {
		long readTime = System.currentTimeMillis();
		List<StockNotice> stockNotices = stockNoticeDao.getUnreadNotices();
		if (stockNotices.size() > 0) {
			stockNoticeDao.setReadByTime(readTime);
		}
		return stockNotices;
	}

	static public void main(String[] args) {
		// String url = "http://hq.sinajs.cn/list=sb837763,sb833048";
		// String stocksStr = HttpUtil.httpGet(url);
		// stockInfos = SinaStockInfoUtil.stockInfosFromString(stocksStr);

		// 获取股票列表
		// String urlBase =
		// "http://www.neeq.com.cn/nqhqController/nqhq.do?type=X&zqdm=&sortfield=&sorttype=&xxfcbj=&keyword=&page=";
		// int startPage = 401, endPage = 449;
		// for (int i = startPage; i <= endPage; i++) {
		// String url = urlBase + i;
		// String res = HttpUtil.httpGet(url);
		// int index = res.indexOf("hqzqdm");
		// while (index > 0) {
		// String code = res.substring(index + 9, index + 9 + 6);
		// int indexOfName = res.indexOf("hqzqjc");
		// res = res.substring(indexOfName);
		// index = res.indexOf(':');
		// res = res.substring(index + 2);
		// index = res.indexOf('"');
		// String name = res.substring(0, index);
		// index = res.indexOf("hqzqdm");
		// System.out.println(code + "=" + name);
		// }
		// }

		// String urlBase =
		// "http://www.neeq.com.cn/nqhqController/detailCompany.do?zqdm=";
		// Properties properties = new Properties();
		// Properties outProperties = new Properties();
		// InputStream in;
		// OutputStream out = new
		// FileOutputStream("/Users/jiwentadashi/Desktop/a.properties");
		// try {
		// in =
		// StockService.class.getClass().getResourceAsStream("/xsbcode.properties");
		//
		// properties.load(in);
		// Enumeration e = properties.propertyNames();
		// while (e.hasMoreElements()) {
		// String code = (String) e.nextElement();
		// float earningPerShare = getEarningPerShare(urlBase+code);
		// outProperties.setProperty(code, earningPerShare);
		// }
		// outProperties.store(out, "test");
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// String urlBase =
		// "http://www.neeq.com.cn/nqhqController/detailCompany.do?zqdm=430002";
		// float earningPerShare = getEarningPerShare(urlBase);
		// System.out.println(earningPerShare);

	}

	@SuppressWarnings("unused")
	static private float getEarningPerShare(String url) {
		String res = HttpUtil.httpGet(url);
		int index = res.indexOf("earningsPerShare") + "earningsPerShare".length() + 1;
		int endIndex = 0;
		try {
			res = res.substring(index);
			index = res.indexOf('"') + 1;
			res = res.substring(index);
			endIndex = res.indexOf('"');
			if (res.substring(0, endIndex).equals("null")) {
				return -1;
			}
		} catch (RuntimeException e) {
			System.out.println(url);
			return -1;
		}

		float result = -1;
		try {
			result = Float.valueOf(res.substring(0, endIndex));
		} catch (NumberFormatException e) {
			System.out.println(url);
			System.out.println(res.substring(0, endIndex));
		}

		return result;
	}

	@SuppressWarnings("rawtypes")
	private void generateUrlByProperties(Enumeration e) {
		int i = 0;
		StringBuffer url = new StringBuffer(sinaBaseUrl);
		if (e.hasMoreElements()) {
			url.append("sb" + e.nextElement());
		}
		while (++i < 500 && e.hasMoreElements()) {
			url.append(",sb" + e.nextElement());
		}
		if (url.indexOf("833553") > 0) {
			logger.info("get 833553");
		}
		sinaUrls.add(url.toString());
	}
}
