package org.example.ticketserver.kafka;

import org.awaitility.Awaitility;
import org.example.ticketserver.dto.reservation.ReservationRequest;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.example.ticketserver.service.ReservationService;
import org.example.ticketserver.service.TicketService;
import org.example.ticketserver.service.TicketStockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {KafkaProducer.TOPIC_NAME})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaProducerTest {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerTest.class);

    @Autowired
    private KafkaProducer kafkaProducer;
    @Autowired
    private TicketStockService ticketStockService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private ReservationService reservationService;

    private Long ticketId;

    @BeforeEach
    void beforeEach() {
        this.ticketId = null;
        log.info("afterEach : 데이터베이스 초기화");
        reservationService.deleteAll();
        ticketStockService.deleteAll();
        ticketService.deleteAll();
    }

    @Test
    @DisplayName("동시성 테스트 - 100명이 100장 티켓 예매 성공")
    void concurrentReservationTest() throws Exception {
        int threadCount = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // given
        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket newTicket = new Ticket("ticket1");
        Long ticketId = ticketService.createTicket(newTicket);
        TicketStock newTicketStock = new TicketStock(newTicket, 30);
        ticketStockService.save(newTicketStock);
        this.ticketId = ticketId;
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            executor.submit(() -> {
                try {
                    ReservationRequest.CreateReservationRequest request = new ReservationRequest.CreateReservationRequest();
                    request.setName("member" + idx);
                    request.setTicketId(ticketId);
                    kafkaProducer.send(request);
                } catch (Exception e) {
                    log.warn("예매 실패: {}번 회원 - {}", idx, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Ticket ticket = ticketService.findTicketById(ticketId);
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))  // DB 처리 시간 대기
                .until(() -> reservationService.getReservationCount(ticket) >= 30);

        TicketStock stock = ticketStockService.findByTicketId(ticket);
        assertThat(reservationService.getReservationCount(ticket)).isEqualTo(30);
        assertThat(stock.getQuantity()).isZero(); // 재고가 정확히 0이어야 성공
    }

    @Test
    @DisplayName("예외 테스트 - 100명이 동시에 1장 예매, 1명만 성공")
    void singleTicketReservationTest() throws Exception {
        // given
        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket newTicket = new Ticket("ticket1");
        Long ticketId = ticketService.createTicket(newTicket);
        TicketStock newTicketStock = new TicketStock(newTicket, 1);
        ticketStockService.save(newTicketStock);
        this.ticketId = ticketId;
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            executor.submit(() -> {
                try {
                    ReservationRequest.CreateReservationRequest request = new ReservationRequest.CreateReservationRequest();
                    request.setName("member" + idx);
                    request.setTicketId(ticketId);
                    kafkaProducer.send(request);
                } catch (Exception e) {
                    log.warn("예매 실패: {}번 회원 - {}", idx, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Ticket ticket = ticketService.findTicketById(ticketId);
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))  // DB 처리 시간 대기
                .until(() -> reservationService.getReservationCount(ticket) >= 1);
        TicketStock stock = ticketStockService.findByTicketId(ticket);
        assertThat(reservationService.getReservationCount(ticket)).isEqualTo(1);
        assertThat(stock.getQuantity()).isZero(); // 재고가 정확히 0이어야 성공
    }
}