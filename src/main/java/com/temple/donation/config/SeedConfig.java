package com.temple.donation.config;

import com.temple.donation.service.SeedService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedConfig {

    @Bean
    public CommandLineRunner seedRunner(SeedService seedService) {
        return args -> seedService.ensureSeed();
    }
}
