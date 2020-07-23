package com.nowcoder.community.actuator;

import com.nowcoder.community.util.CommunityUtil;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

/** @author barea */
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

  @Autowired private DataSource dataSource;

  @ReadOperation
  public String checkConnection() {

    try (Connection ignored = dataSource.getConnection(); ) {
      return CommunityUtil.getJSONString(0, "Successfully connected to MySQL");
    } catch (SQLException exception) {

      logger.error("Failed to connect to MySQL in endpoint." + exception.getMessage());
      return CommunityUtil.getJSONString(1, "Failed to connect to MySQL");
    }
  }
}
