package org.example.ticketserver.repository;

import jakarta.persistence.LockModeType;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TicketStockRepository extends JpaRepository<TicketStock, Long> {
    // 락 / 트랜잭션 격리 x
//    Optional<TicketStock> findByTicketId(Ticket ticketId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketStock> findByTicketId(Ticket ticketId);
}
