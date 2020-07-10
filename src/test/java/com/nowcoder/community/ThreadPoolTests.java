package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

  private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

  // Ordinary JDK Thread pool
  private ExecutorService executorService = Executors.newFixedThreadPool(5);

  // JDK Thread pool that can execute scheduled tasks
  private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

  // Ordinary Spring Thread pool
  @Autowired private ThreadPoolTaskExecutor taskExecutor;

  // Spring Thread pool that can execute scheduled tasks
  @Autowired private ThreadPoolTaskScheduler taskScheduler;

  @Autowired private AlphaService alphaService;

  private void sleep(long m) {

    try {
      Thread.sleep(m);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  // 1.Ordinary JDK Thread pool
  @Test
  public void testExecutorService() {

    Runnable task = () -> logger.debug("Hello ExecutorService");

    for (int i = 0; i < 10; i++) {
      executorService.submit(task);
    }

    sleep(10000);
  }

  // 2.JDK Thread pool that can execute scheduled tasks
  @Test
  public void testScheduleExecutorService() {

    Runnable task = () -> logger.debug("Hello ScheduleExecutorService");

    scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);

    sleep(30000);
  }

  // 3.Ordinary Spring Thread pool
  @Test
  public void testThreadPoolTaskExecutor() {

    Runnable task = () -> logger.debug("Hello ThreadPoolTaskExecutor");

    for (int i = 0; i < 10; i++) {
      taskExecutor.submit(task);
    }

    sleep(10000);
  }

  // 4.Spring Thread pool that can execute scheduled tasks
  @Test
  public void testThreadPoolTaskScheduler() {

    Runnable task = () -> logger.debug("Hello ThreadPoolTaskScheduler");

    var startTime = new Date(System.currentTimeMillis() + 10000);
    taskScheduler.scheduleAtFixedRate(task, startTime, 1000);

    sleep(30000);
  }

  // 5.Ordinary Spring Thread pool (simple way)
  @Test
  public void testThreadPoolTaskExecutorSimple() {

    for (int i = 0; i < 10; i++) {
      alphaService.execute1();
    }

    sleep(10000);
  }

  // 6.Spring Thread pool that can execute scheduled tasks (simple way)
  @Test
  public void testThreadPoolTaskSchedulerSimple() {
    sleep(30000);
  }

}
