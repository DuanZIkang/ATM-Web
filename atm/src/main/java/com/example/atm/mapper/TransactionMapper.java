package com.example.atm.mapper;

import com.example.atm.entity.Transaction;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionMapper {

	@Insert("INSERT INTO `transaction`(card, type, amount, remark, to_name, time) " +
			"VALUES(#{card}, #{type}, #{amount}, #{remark}, #{toName}, #{time})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(Transaction t);

	@Select("SELECT id, card, type, amount, remark, to_name AS toName, time " +
			"FROM `transaction` WHERE card = #{card} ORDER BY time DESC LIMIT 50")
	List<Transaction> findByCard(String card);

	@Delete("DELETE FROM transaction WHERE card = #{card} AND id NOT IN (" +
			"SELECT id FROM (" +
			"    SELECT id FROM transaction WHERE card = #{card} ORDER BY time DESC LIMIT 10" +
			") AS temp)")
	void trimOldRecords(@Param("card") String card);

}
