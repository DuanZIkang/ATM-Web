package com.example.atm.mapper;

import com.example.atm.entity.Account;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface AccountMapper {

	@Select("""
			    SELECT card,
			           name,
			           password,
			           balance,
			           daily_limit AS dailyLimit,
			           sex,
			           status
			    FROM account
			    WHERE card = #{card}
			""")
	Account findByCard(String card);

	@Select("""
			    SELECT card,
			           name,
			           password,
			           balance,
			           daily_limit AS dailyLimit,
			           sex,
			           status,
			           close_time AS closeTime
			    FROM account
			    WHERE card = #{card}
			""")
	Account login(String card);

	@Insert("""
			    INSERT INTO account(card, name, password, balance, daily_limit, sex, status)
			    VALUES(#{card}, #{name}, #{password}, #{balance}, #{dailyLimit}, #{sex}, 'ACTIVE')
			""")
	void insert(Account account);

	@Update("""
			    UPDATE account
			    SET balance = #{balance}
			    WHERE card = #{card}
			""")
	void updateBalance(@Param("card") String card,
	                   @Param("balance") BigDecimal balance);

	@Update("""
			    UPDATE account
			    SET password = #{newPwd}
			    WHERE card = #{card}
			""")
	void updatePassword(@Param("card") String card,
	                    @Param("newPwd") String newPwd);

	@Update("""
			    UPDATE account
			    SET status = 'LOCKED'
			    WHERE card = #{card}
			""")
	void lockAccount(String card);

	@Select("""
			    SELECT card, name, password, balance, daily_limit AS dailyLimit, sex, status
			    FROM account
			    WHERE card = #{card}
			    FOR UPDATE
			""")
	Account findByCardForUpdate(String card);

	@Update("""
			    UPDATE account
			    SET status = 'CANCEL',close_time = #{closeTime}
				WHERE card = #{card}
			    AND balance = #{balance}
			""")
	int closeAccount(String card, LocalDateTime closeTime,BigDecimal balance);

	@Delete("""
			DELETE FROM account where status = 'CANCEL'
			AND close_time IS NOT NULL
			AND close_time < #{threshold}
			AND balance = 0
			""")
	int deleteCancelAccount(@Param("threshold") LocalDateTime threshold);

	@Update("""
			UPDATE account
			SET status = 'ACTIVE'
			, close_time = NULL
			WHERE card = #{card}
			""")
	void restoreCancelAccount(@Param("card") String card);
}