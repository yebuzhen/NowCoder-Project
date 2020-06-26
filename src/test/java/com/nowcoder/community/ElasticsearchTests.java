package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

  @Autowired private DiscussPostMapper discussPostMapper;

  @Autowired private DiscussPostRepository discussPostRepository;

  @Test
  public void testInsert() {

    discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
    discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
    discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
  }

  @Test
  public void testInsertList() {

    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(101, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(102, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(103, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(111, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(112, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(131, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(132, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(133, 0, 100)));
    discussPostRepository.saveAll((discussPostMapper.selectDiscussPosts(134, 0, 100)));
  }

  @Test
  public void testUpdate() {

    DiscussPost post = discussPostMapper.selectDiscussPostById(231);
    post.setContent("我是新人, 使劲灌水");
    discussPostRepository.save(post);

  }

  @Test
  public void testDelete() {

//    discussPostRepository.deleteById(231);
    discussPostRepository.deleteAll();

  }

  @Test
  public void testSearchByRepository() {

    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
        .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
        .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
        .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
        .withPageable(PageRequest.of(0, 10))
        .withHighlightFields(
            new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
            new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
        ).build();

    Page<DiscussPost> page = discussPostRepository.search(searchQuery);
    System.out.println(page.getTotalElements());
    System.out.println(page.getTotalPages());
    System.out.println(page.getNumber());
    System.out.println(page.getSize());

    for (DiscussPost post : page) {
      System.out.println(post);
    }

  }

  @Test
  public void testSearchByTemplate() {

    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
        .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
        .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
        .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
        .withPageable(PageRequest.of(0, 10))
        .withHighlightFields(
            new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
            new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
        ).build();

    //Deprecated... See video for details

  }

}
