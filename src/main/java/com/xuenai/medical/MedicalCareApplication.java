package com.xuenai.medical;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.xuenai.medical.mapper")
@EnableTransactionManagement
public class MedicalCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalCareApplication.class, args);
    }

}
