package com.nowcoder.community;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.BitOperation;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

  @Autowired private RedisTemplate redisTemplate;

  @Test
  public void testStrings() {

    String redisKey = "test:count";

    redisTemplate.opsForValue().set(redisKey, 1);

    System.out.println(redisTemplate.opsForValue().get(redisKey));
    System.out.println(redisTemplate.opsForValue().increment(redisKey));
    System.out.println(redisTemplate.opsForValue().decrement(redisKey));
  }

  @Test
  public void testHashes() {

    String redisKey = "test:user";

    redisTemplate.opsForHash().put(redisKey, "id", 1);
    redisTemplate.opsForHash().put(redisKey, "username", "Bob");

    System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
    System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
  }

  @Test
  public void testLists() {

    String redisKey = "test:ids";

    redisTemplate.opsForList().leftPush(redisKey, 101);
    redisTemplate.opsForList().leftPush(redisKey, 102);
    redisTemplate.opsForList().leftPush(redisKey, 103);

    System.out.println(redisTemplate.opsForList().size(redisKey));
    System.out.println(redisTemplate.opsForList().index(redisKey, 0));
    System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

    System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    System.out.println(redisTemplate.opsForList().leftPop(redisKey));
  }

  @Test
  public void testSets() {

    String redisKey = "test:teachers";

    redisTemplate.opsForSet().add(redisKey, "Adam", "Bob", "Christ", "David", "Ellen");

    System.out.println(redisTemplate.opsForSet().size(redisKey));
    System.out.println(redisTemplate.opsForSet().pop(redisKey));
    System.out.println(redisTemplate.opsForSet().members(redisKey));
  }

  @Test
  public void testSortedSets() {

    String redisKey = "test:students";

    redisTemplate.opsForZSet().add(redisKey, "Alice", 80);
    redisTemplate.opsForZSet().add(redisKey, "Barea", 90);
    redisTemplate.opsForZSet().add(redisKey, "Charlie", 50);
    redisTemplate.opsForZSet().add(redisKey, "Dao", 70);
    redisTemplate.opsForZSet().add(redisKey, "Elephant", 60);

    System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
    System.out.println(redisTemplate.opsForZSet().score(redisKey, "Barea"));
    System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "Barea"));
    System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
  }

  @Test
  public void testKeys() {

    redisTemplate.delete("test:user");

    System.out.println(redisTemplate.hasKey("test:user"));

    redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
  }

  // Use one key for multiple times.
  @Test
  public void testBoundOperations() {

    String redisKey = "test:count";
    BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
    operations.increment();
    operations.increment();
    operations.increment();
    operations.increment();
    operations.increment();
    System.out.println(operations.get());
  }

  // Programmatic Transaction
  @Test
  public void testTransactional() {

    Object object =
        redisTemplate.execute(
            new SessionCallback() {

              @Override
              public Object execute(RedisOperations operations) throws DataAccessException {

                String redisKey = "test:tx";

                operations.multi();

                operations.opsForSet().add(redisKey, "Alice");
                operations.opsForSet().add(redisKey, "Bob");
                operations.opsForSet().add(redisKey, "Charlie");

                System.out.println(operations.opsForSet().members(redisKey));

                return operations.exec();
              }
            });

    System.out.println(object);
  }

  // Calculate the number of unique data pieces in 200,000 pieces
  @Test
  public void testHyperLogLog() {

    String redisKey = "test:hll:01";

    for (int i = 1; i <= 100000; i++) {
      redisTemplate.opsForHyperLogLog().add(redisKey, i);
    }

    for (int i = 1; i <= 100000; i++) {

      int r = (int) (Math.random() * 100000 + 1);
      redisTemplate.opsForHyperLogLog().add(redisKey, r);
    }

    long size = redisTemplate.opsForHyperLogLog().size(redisKey);
    System.out.println(size);
  }

  // Merge three datasets, and then calculate the number of unique data pieces
  @Test
  public void testHyperLogLogUnion() {

    String redisKey = "test:hll:02";
    for (int i = 1; i <= 10000; i++) {
      redisTemplate.opsForHyperLogLog().add(redisKey, i);
    }

    redisKey = "test:hll:03";
    for (int i = 5001; i <= 15000; i++) {
      redisTemplate.opsForHyperLogLog().add(redisKey, i);
    }

    redisKey = "test:hll:04";
    for (int i = 10001; i <= 20000; i++) {
      redisTemplate.opsForHyperLogLog().add(redisKey, i);
    }

    String unionKey = "test:hll:union";
    redisTemplate.opsForHyperLogLog().union(unionKey, "test:hll:02", "test:hll:03", "test:hll:04");

    long size = redisTemplate.opsForHyperLogLog().size(unionKey);
    System.out.println(size);
  }

  // Calculate the sum of boolean values from one array of data
  @Test
  public void testBitMap() {

    String redisKey = "test:bm:01";

    // Set
    redisTemplate.opsForValue().setBit(redisKey, 1, true);
    redisTemplate.opsForValue().setBit(redisKey, 4, true);
    redisTemplate.opsForValue().setBit(redisKey, 7, true);

    // Get
    System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
    System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
    System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

    // Get statistics
    Object object =
        redisTemplate.execute(
            new RedisCallback() {
              @Override
              public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
              }
            });

    System.out.println(object);
  }

  // Conduct OR operation on three set of boolean values
  @Test
  public void testBitMapOperation() {

    String redisKey = "test:nm:02";
    redisTemplate.opsForValue().setBit(redisKey, 0, true);
    redisTemplate.opsForValue().setBit(redisKey, 1, true);
    redisTemplate.opsForValue().setBit(redisKey, 2, true);

    redisKey = "test:nm:03";
    redisTemplate.opsForValue().setBit(redisKey, 2, true);
    redisTemplate.opsForValue().setBit(redisKey, 3, true);
    redisTemplate.opsForValue().setBit(redisKey, 4, true);

    redisKey = "test:nm:04";
    redisTemplate.opsForValue().setBit(redisKey, 4, true);
    redisTemplate.opsForValue().setBit(redisKey, 5, true);
    redisTemplate.opsForValue().setBit(redisKey, 6, true);

    String orKey = "test:bm:or";
    Object object =
        redisTemplate.execute(
            new RedisCallback() {
              @Override
              public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(
                    BitOperation.OR,
                    orKey.getBytes(),
                    "test:nm:02".getBytes(),
                    "test:nm:03".getBytes(),
                    "test:nm:04".getBytes());
                return redisConnection.bitCount(orKey.getBytes());
              }
            });

    System.out.println(object);

    for (int i = 0; i < 7; i++) {
      System.out.println(redisTemplate.opsForValue().getBit(orKey, i));
    }
  }
}
