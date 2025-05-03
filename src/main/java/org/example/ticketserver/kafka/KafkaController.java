package org.example.ticketserver.kafka;

import lombok.RequiredArgsConstructor;
import org.example.ticketserver.dto.reservation.ReservationRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaProducer kafkaProducer;

    @GetMapping("/send")
    public void sendMessage(@RequestBody ReservationRequest.CreateReservationRequest dto){
        kafkaProducer.send(dto);
    }
}
