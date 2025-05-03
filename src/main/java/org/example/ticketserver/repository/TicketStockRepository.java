package org.example.ticketserver.repository;

import jakarta.persistence.LockModeType;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TicketStockRepository extends JpaRepository<TicketStock, Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketStock> findByTicketId(Ticket ticketId);
}
