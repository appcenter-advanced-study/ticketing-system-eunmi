package org.example.ticketserver.repository;

import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketStockRepository extends JpaRepository<TicketStock, Long> {
    Optional<TicketStock> findByTicketId(Ticket ticketId);
}
