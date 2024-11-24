package com.weathersolar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@ComponentScan(basePackages = {"com.weathersolar", "com.weathersolar.config"})
public class WeatherSolarApplication {

   public static void main(String[] args) {
       SpringApplication.run(WeatherSolarApplication.class, args);
   }

   @Bean
   public WebClient.Builder webClientBuilder() {
       return WebClient.builder();
   }
}