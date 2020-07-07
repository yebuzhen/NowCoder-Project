package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands.BitOperation;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** @author barea */
@Service
public class DataService {

  @Autowired private RedisTemplate redisTemplate;

  private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

  // Put IP address as UV into Redis
  public void recordUV(String ip) {

    String redisKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(new Date()));
    redisTemplate.opsForHyperLogLog().add(redisKey, ip);
  }

  // Get UV from a time period
  public long calculateUV(Date startDate, Date endDate) {

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Arguments cannot be null");
    }

    // Put keys during this time period
    List<String> keyList = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    while (!calendar.getTime().after(endDate)) {

      String key = RedisKeyUtil.getUVKey(simpleDateFormat.format(calendar.getTime()));
      keyList.add(key);
      calendar.add(Calendar.DATE, 1);
    }

    // Union the data
    String redisKey =
        RedisKeyUtil.getUVKey(simpleDateFormat.format(startDate), simpleDateFormat.format(endDate));
    redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray(new String[0]));

    // Return the statistic
    return redisTemplate.opsForHyperLogLog().size(redisKey);
  }

  // Put user ID as DAU into Redis
  public void recordDAU(int userId) {

    String redisKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(new Date()));
    redisTemplate.opsForValue().setBit(redisKey, userId, true);
  }

  // Get DAU from a time period
  public long calculateDAU(Date startDate, Date endDate) {

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Arguments cannot be null");
    }

    // Put keys during this time period
    List<byte[]> keyList = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    while (!calendar.getTime().after(endDate)) {

      String key = RedisKeyUtil.getDAUKey(simpleDateFormat.format(calendar.getTime()));
      keyList.add(key.getBytes());
      calendar.add(Calendar.DATE, 1);
    }

    // Perform OR calculation
    return (long)
        redisTemplate.execute(
            (RedisCallback)
                connection -> {
                  String redisKey =
                      RedisKeyUtil.getDAUKey(
                          simpleDateFormat.format(startDate), simpleDateFormat.format(endDate));
                  connection.bitOp(
                      BitOperation.OR, redisKey.getBytes(), keyList.toArray(new byte[0][0]));

                  return connection.bitCount(redisKey.getBytes());
                });
  }
}
