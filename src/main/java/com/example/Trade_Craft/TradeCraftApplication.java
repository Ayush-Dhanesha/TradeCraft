package com.example.Trade_Craft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.Trade_Craft", "com.tradecraft"})
public class TradeCraftApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeCraftApplication.class, args);
	}

}
