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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
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
    private ReservationService reservationService;


    @BeforeEach
    @Transactional
    void beforeEach() {
        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket ticket = new Ticket("ticket1");
        Ticket savedTicket = ticketRepository.save(ticket);

        TicketStock ticketStock = new TicketStock(ticket, 1);
        ticketStockRepository.save(ticketStock);
        this.ticketId = savedTicket.getId();
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);
    }

    @AfterEach
    @Transactional
    void afterEach() {
        log.info("afterEach : 데이터베이스 초기화");
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
        reservationService.reserve(name, ticketId);

        // then
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        TicketStock ticketStock = ticketStockRepository.findByTicketId(ticket).orElseThrow();
        assertThat(ticketStock.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("경쟁 조건을 설정 하지 않은 경우, 동시에 같은 티켓수를 보고 모두 예매가 가능한 오류가 발생하게 된다.")
    public void reservationException1() throws Exception {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);  // thread 생성
        CountDownLatch latch = new CountDownLatch(threadCount);  // 모든 스레드 작업이 끝날 때까지 await() 대기

        log.info("티켓 예매를 시작합니다. : {}", ticketId);
        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            executor.submit(() -> {
                try {
                    try {
                        reservationService.reserve("member" + idx, ticketId);
                        log.info("예매 성공 : {}", "member" + idx);
                    } catch (Exception e) {
                        log.info("예매 실패 : {}번 회원 {}", idx, e.getMessage());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // thread 끝날 때까지 대기

        // then
        assertThat(reservationRepository.findAll().size()).isEqualTo(10);  // 문제 상황 ! 모든 구매자가 구매를 성공하게 되는 오류 발생 ..
        TicketStock stock = ticketStockRepository.findById(ticketId).orElseThrow();
        assertThat(stock.getQuantity()).isZero(); // 재고가 정확히 0이어야 성공
    }
}