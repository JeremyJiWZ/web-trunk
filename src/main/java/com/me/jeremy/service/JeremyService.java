package com.me.jeremy.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.me.jeremy.dao.IdtableDao;
import com.me.jeremy.dao.ViewCountDao;
import com.me.jeremy.meta.ViewCount;

@Service
public class JeremyService {
	static private Logger logger = Logger.getLogger(JeremyService.class);
	@Resource private IdtableDao idtableDao;
	@Resource private ViewCountDao viewCountDao;
	
	public int getViewCount(String url){
		return viewCountDao.selectByUrl(url).getCount();
	}
	
	public boolean increaseViewCount(String url){
		ViewCount viewCount = viewCountDao.selectByUrl(url);
		if(viewCount==null){
			viewCount = new ViewCount();
			viewCount.setUrl(url);
			viewCount.setCount(0);
			try {
				viewCountDao.add(viewCount);
			} catch (DuplicateKeyException e) {
			}
			
		}
		return viewCountDao.increase(url)>0;
	}
	public static void main(String[] args){
	}
}
