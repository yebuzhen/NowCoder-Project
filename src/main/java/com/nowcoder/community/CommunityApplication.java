package com.nowcoder.community;

import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {

  public static void main(String[] args) {
    SpringApplication.run(CommunityApplication.class, args);
  }

  @PostConstruct
  public void init() {

    // Solve the conflict when Netty starts.
    // See Netty4Utils.setAvailableProcessors(final int availableProcessors)
    System.setProperty("es.set.netty.runtime.available.processors", "false");
  }
}
