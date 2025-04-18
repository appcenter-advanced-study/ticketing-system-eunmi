package org.example.ticketserver.service;

import org.slf4j.Logger;
import org.assertj.core.api.Assertions;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.example.ticketserver.repository.ReservationRepository;
import org.example.ticketserver.repository.TicketRepository;
import org.example.ticketserver.repository.TicketStockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReservationServiceTest {
    private static final Logger log = LoggerFactory.getLogger(ReservationServiceTest.class);

    private Long ticketId;

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketStockRepository ticketStockRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationService ticketService;


    @BeforeEach
    void beforeEach() {
        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket ticket = new Ticket("ticket1");
        Ticket savedTicket = ticketRepository.save(ticket);

        TicketStock ticketStock = new TicketStock(ticket, 1);
        ticketStockRepository.save(ticketStock);
        this.ticketId = savedTicket.getId();
    }

    @AfterEach
    void afterEach() {
        reservationRepository.deleteAll();
        ticketStockRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("티켓 예매자는 티켓을 예매한다.")
    public void reservationTest() throws Exception {
        // given
        String name = "eunmi";

        // when
        ticketService.reserve(name, ticketId);

        // then
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        TicketStock ticketStock = ticketStockRepository.findByTicketId(ticket).orElseThrow();
        Assertions.assertThat(ticketStock.getQuantity()).isEqualTo(0);
    }
}