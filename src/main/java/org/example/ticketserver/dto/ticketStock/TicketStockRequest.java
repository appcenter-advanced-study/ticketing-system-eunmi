package org.example.ticketserver.dto.ticketStock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.ticketserver.entity.Ticket;

public class TicketStockRequest {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class UpdateTicketStock {
        private Ticket ticketId;
        private Integer quantity;
    }
}
