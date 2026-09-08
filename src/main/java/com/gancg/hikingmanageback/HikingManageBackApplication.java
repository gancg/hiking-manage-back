package com.gancg.hikingmanageback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.gancg.hikingmanageback.security.AuthProperties;
import com.gancg.hikingmanageback.security.JwtProperties;

@SpringBootApplication
@MapperScan("com.gancg.hikingmanageback.mapper")
@EnableConfigurationProperties({JwtProperties.class, AuthProperties.class})
public class HikingManageBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(HikingManageBackApplication.class, args);
	}

}
