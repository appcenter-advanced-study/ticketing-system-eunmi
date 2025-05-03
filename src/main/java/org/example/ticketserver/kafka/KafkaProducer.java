package org.example.ticketserver.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ticketserver.dto.reservation.ReservationRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {
    public static final String TOPIC_NAME = "ticket-reservation";

    private final KafkaTemplate<String, ReservationRequest.CreateReservationRequest> kafkaTemplate;

    public void send(ReservationRequest.CreateReservationRequest dto){
        try{
            CompletableFuture<SendResult<String, ReservationRequest.CreateReservationRequest>> future = kafkaTemplate.send(TOPIC_NAME, dto);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[SUCCESS] Produce Reserving ticket id {}, name {}", dto.getTicketId(), dto.getName());

                } else {
                    log.error("[ERROR] Reserving ticket id {}, name {}", dto.getTicketId(), dto.getName());
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
