package com.me.jeremy.dao;


import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.me.jeremy.meta.Idtable;

@Repository
public interface IdtableDao {
	@Select("select * from idtable where id = ${id}")
	Idtable getIdtableById(@Param("id") int id);

	@Select("select count(*) from idtable")
	int getCount();
	
	@Insert("insert into idtable values (#{id})")
	@Options(useGeneratedKeys=true)
//	@SelectKey(before = false, keyProperty = "id",  statement="call identity()", resultType = int.class)
	int insert(Idtable idtable);
	
	@Select("select * from idtable")
	List<Idtable> getList();
	
}
