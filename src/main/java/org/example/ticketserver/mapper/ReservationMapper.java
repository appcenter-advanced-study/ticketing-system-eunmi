package org.example.ticketserver.mapper;

import org.example.ticketserver.entity.Reservation;
import org.example.ticketserver.entity.Ticket;

public class ReservationMapper {
    public static Reservation toEntity(String name, Ticket ticket) {
        return Reservation.builder()
                .username(name)
                .ticketId(ticket)
                .build();
    }
}
