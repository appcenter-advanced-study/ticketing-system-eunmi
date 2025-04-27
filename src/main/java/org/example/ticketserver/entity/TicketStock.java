package org.example.ticketserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "TICKET_ID")
    private Ticket ticketId;
    private Integer quantity;
//    @Version
    private Integer version;

    public TicketStock(Ticket ticketId, int quantity) {
        this.ticketId = ticketId;
        this.quantity = quantity;
    }

    public void reserve() {
        if(this.quantity <= 0) {
            throw new RuntimeException("재고 수량이 0이하입니다.");
        }
        this.quantity--;
    }
}
