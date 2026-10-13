package com.ims.ims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImsApplication {

	public static void main(String[] args) {

		SpringApplication.run(ImsApplication.class, args);
		System.out.println("🎉 تم تشغيل نظام إدارة المخزون بنجاح!");
		System.out.println("📋 افتح المتصفح على: http://localhost:8080/");
	}

}
