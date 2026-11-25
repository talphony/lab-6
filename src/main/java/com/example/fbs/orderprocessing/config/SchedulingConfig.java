package com.example.fbs.orderprocessing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Включает выполнение методов с @Scheduled. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
