package com.example.paymentwalletservice;

import com.example.commonsecurity.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.paymentwalletservice",
        "com.example.commonsecurity"
})
@EnableConfigurationProperties(JwtProperties.class)
public class PaymentWalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentWalletServiceApplication.class, args);
    }
}
