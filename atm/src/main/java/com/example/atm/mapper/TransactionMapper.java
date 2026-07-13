package com.example.atm.mapper;

import com.example.atm.entity.Transaction;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionMapper {

	@Insert("INSERT INTO `transaction`(card, type, amount, remark, to_name, time) " +
			"VALUES(#{card}, #{type}, #{amount}, #{remark}, #{toName}, #{time})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(Transaction t);

	@Select("SELECT id, card, type, amount, remark, to_name AS toName, time " +
			"FROM `transaction` WHERE card = #{card} ORDER BY time DESC LIMIT 10")
	List<Transaction> findByCard(String card);

	@Select("SELECT id, card, type, amount, remark, to_name AS toName, time FROM `transaction` WHERE card =#{card} " +
			"ORDER BY time DESC")
	List<Transaction> allTransactions(String card);
}
