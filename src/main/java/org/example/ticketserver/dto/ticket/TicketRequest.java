package org.example.ticketserver.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class TicketRequest {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class UpdateTicketRequest {
        private Long id;
        private String name;
    }
}
