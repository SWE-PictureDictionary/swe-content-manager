package com.swe.project.contentmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.swe.project")
public class ContentManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContentManagerApplication.class, args);
	}

}
