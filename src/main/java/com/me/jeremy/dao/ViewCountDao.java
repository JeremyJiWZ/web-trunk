package com.me.jeremy.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.me.jeremy.meta.ViewCount;

@Repository
public interface ViewCountDao {
	@Select("select * from viewCount where url = #{url}")
	public ViewCount selectByUrl(String url);
	
	@Insert("insert into viewCount values (null, #{url}, 0)")
	public int add(ViewCount viewCount);
	
	@Update("update viewCount set count = count+1 where url = #{url}")
	public int increase(@Param("url")String url);
}
