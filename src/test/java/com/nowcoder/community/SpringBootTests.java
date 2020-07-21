package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

  @Autowired private DiscussPostService discussPostService;

  private DiscussPost data;

  @BeforeAll
  public static void beforeClass() {
    System.out.println("beforeClass");
  }

  @AfterAll
  public static void afterClass() {
    System.out.println("afterClass");
  }

  @BeforeEach
  public void before() {

    System.out.println("before");

    // Initialize testing data
    data = new DiscussPost();
    data.setUserId(111);
    data.setTitle("Test Title");
    data.setContent("Test Content");
    data.setCreateTime(new Date());
    discussPostService.addDiscussPost(data);
  }

  @AfterEach
  public void after() {

    // Delete testing data
    System.out.println("after");
    discussPostService.updateStatus(data.getId(), 2);
  }

  @Test
  public void test1() {
    System.out.println("test1");
  }

  @Test
  public void test2() {
    System.out.println("test2");
  }

  @Test
  public void testFindById() {

    DiscussPost post = discussPostService.findDiscussPostById(data.getId());
    Assertions.assertNotNull(post);
    Assertions.assertEquals(data.getTitle(), post.getTitle());
    Assertions.assertEquals(data.getContent(), post.getContent());
  }

  @Test
  public void testUpdateScore() {

    int rows = discussPostService.updateScore(data.getId(), 2000.00);
    Assertions.assertEquals(1, rows);

    DiscussPost post = discussPostService.findDiscussPostById(data.getId());
    Assertions.assertEquals(2000.00, post.getScore(), 2);
  }
}
