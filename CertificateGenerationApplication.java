package com.certificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CertificateGenerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificateGenerationApplication.class, args);
    }
}
