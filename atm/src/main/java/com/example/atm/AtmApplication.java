package com.example.atm;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@MapperScan("com.example.atm.mapper")
@SpringBootApplication
@Slf4j
public class AtmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtmApplication.class, args);
    }
    @Bean
    public CommandLineRunner checkDatabase(DataSource dataSource) {
        return args -> {
            log.info("【数据库连接测试】正在强制检查...");
            try (Connection conn = dataSource.getConnection()) {
                log.info("【数据库连接成功！】当前数据库: {}", conn.getCatalog());
            } catch (Exception e) {
                log.error("【数据库连接失败！】{}" , e.getMessage(),e);

            }
        };
    }
}
