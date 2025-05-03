package org.example.ticketserver.dto.reservation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

public class ReservationRequest {
    @Getter
    @Setter
    @JsonDeserialize
    @JsonSerialize
    public static class CreateReservationRequest {
        private String name;
        private Long ticketId;
    }
}
