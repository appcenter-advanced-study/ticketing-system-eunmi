package org.example.ticketserver.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
//    @KafkaListener(topics = KafkaProducer.TOPIC_NAME, groupId = "ticket")
    public void consumer(String message){
        System.out.println("receive message : " + message);
    }
}
