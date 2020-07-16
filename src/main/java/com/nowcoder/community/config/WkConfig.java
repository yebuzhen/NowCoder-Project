package com.nowcoder.community.config;

import java.io.File;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** @author barea */
@Configuration
public class WkConfig {

  private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

  @Value("${wk.image.storage}")
  private String wkImageStorage;

  @PostConstruct
  public void init() {

    // Construct the directory of images
    File file = new File(wkImageStorage);

    if (!file.exists()) {
      file.mkdir();
      logger.info("Created WK image directory: " + wkImageStorage);
    }
  }
}
