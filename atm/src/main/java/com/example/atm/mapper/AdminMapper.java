package com.example.atm.mapper;


import com.example.atm.dto.LockResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminMapper {
	@Select("SELECT card,name,status FROM account WHERE status = 'LOCKED'")
	List<LockResponse> findAllLockedAccounts();

	@Update("UPDATE account SET status = 'ACTIVE' WHERE card = #{card}")
	void unlock(@Param("card") String card);
}
