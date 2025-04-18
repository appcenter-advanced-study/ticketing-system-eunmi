package org.example.ticketserver.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "TICKET_ID")
    private Ticket ticketId;
    private String username;

    @Builder
    public Reservation(Ticket ticketId, String username) {
        this.ticketId = ticketId;
        this.username = username;
    }
}
