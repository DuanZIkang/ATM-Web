package com.example.atm.mapper;


import com.example.atm.dto.LockResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AdminMapper {
	@Select("SELECT card,name,status FROM account WHERE status = 'LOCKED'")
	List<LockResponse> findAllLockedAccounts();

	@Update("UPDATE account SET status = 'ACTIVE' WHERE card = #{card}")
	void unlock(@Param("card") String card);
	@Update("UPDATE account SET max_limit = #{max},min_limit = #{min} WHERE card = #{card}")
	void limit(@Param("card") String card, @Param("max") BigDecimal max, @Param("min") BigDecimal min);
}
