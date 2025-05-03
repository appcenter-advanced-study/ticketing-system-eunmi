package org.example.ticketserver.repository;

import org.example.ticketserver.entity.Reservation;
import org.example.ticketserver.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByTicketId(Ticket ticketId);
}
