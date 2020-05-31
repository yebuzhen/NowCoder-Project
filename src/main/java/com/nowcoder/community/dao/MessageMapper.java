package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author barea
 */
@Mapper
public interface MessageMapper {

  //Query the conversation list of current user, returns the latest message for each conversation
  List<Message> selectConversations(int userId, int offset, int limit);

  //Query the number of conversations for current user
  int selectConversationCount(int userId);

  //Query the message(letter) list of one conversation
  List<Message> selectLetters(String conversationId, int offset, int limit);

  //Query the number of messages(letters) of one specific conversation
  int selectLetterCount(String conversation);

  //Query the unread message(letter) number of one user or one conversation
  int selectLetterUnreadCount(int userId, String conversationId);

}
