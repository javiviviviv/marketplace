package com.polec.mscarrito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsCarritoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCarritoApplication.class, args);
    }

}
