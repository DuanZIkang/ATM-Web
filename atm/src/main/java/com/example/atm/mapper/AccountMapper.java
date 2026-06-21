package com.example.atm.mapper;

import com.example.atm.entity.Account;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface AccountMapper {

    @Select("""
        SELECT card,
               name,
               password,
               balance,
               daily_limit AS dailyLimit,
               sex
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
               sex
        FROM account
        WHERE card = #{card}
    """)
    Account login(String card);

    @Insert("""
        INSERT INTO account(card, name, password, balance, daily_limit, sex)
        VALUES(#{card}, #{name}, #{password}, #{balance}, #{dailyLimit}, #{sex})
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

}
