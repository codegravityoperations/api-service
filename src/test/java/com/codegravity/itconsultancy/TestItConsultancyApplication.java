package com.codegravity.itconsultancy;

import org.springframework.boot.SpringApplication;

public class TestItConsultancyApplication {

	public static void main(String[] args) {
		SpringApplication.from(ItConsultancyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
