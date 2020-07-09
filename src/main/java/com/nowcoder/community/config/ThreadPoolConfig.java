package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author barea
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

}
