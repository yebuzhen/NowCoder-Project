package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** @author barea */
@Mapper
public interface MessageMapper {

  // Query the conversation list of current user, returns the latest message for each conversation
  List<Message> selectConversations(int userId, int offset, int limit);

  // Query the number of conversations for current user
  int selectConversationCount(int userId);

  // Query the message(letter) list of one conversation
  List<Message> selectLetters(String conversationId, int offset, int limit);

  // Query the number of messages(letters) of one specific conversation
  int selectLetterCount(String conversation);

  // Query the unread message(letter) number of one user or one conversation
  int selectUnreadLetterCount(int userId, String conversationId);

  // Add new message
  int insertMessage(Message message);

  // Update the status of the message
  int updateStatus(List<Integer> ids, int status);

  // Query the latest system notice of one topic
  Message selectLatestNotice(int userId, String topic);

  // Query the number of system notices of one topic
  int selectNoticeCount(int userId, String topic);

  // Query the number of unread system notices of one topic
  int selectUnreadNoticeCount(int userId, String topic);

  // Query the list of system notices of one topic for one user
  List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
