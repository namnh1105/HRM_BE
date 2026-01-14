package com.hainam.worksphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorksphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorksphereApplication.class, args);
    }

}
