package org.example.ticketserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;

public class TicketStockRequest {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class UpdateTicketStock {
        private Ticket ticketId;
        private Integer quantity;
    }
}
