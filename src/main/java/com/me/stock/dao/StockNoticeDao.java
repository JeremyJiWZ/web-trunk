package com.me.stock.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.me.stock.meta.StockNotice;

@Repository
public interface StockNoticeDao {
	
	@Select("select * from StockNotice where isRead = 0")
	public List<StockNotice> getUnreadNotices();
	
	@Insert("insert into StockNotice values (null, #{code},#{stockName},0,#{b1v},#{b1n},#{s1v},#{s1n},#{time},#{peType})")
	public long insert(StockNotice stockNotice);
	
	@Update("update StockNotice set isRead = 1 where time <= #{time}")
	public int setReadByTime(@Param("time")long time);
}
