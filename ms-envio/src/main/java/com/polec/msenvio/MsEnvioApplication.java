package com.polec.msenvio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsEnvioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsEnvioApplication.class, args);
    }

}
