package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** @author barea */
@Component
public class EventProducer {

  @Autowired private KafkaTemplate kafkaTemplate;

  // Handle the event
  public void fireEvent(Event event) {

    // Send the specific event to targeted topic
    kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
  }
}
