package org.example.ticketserver.service;

import org.example.ticketserver.dto.reservation.ReservationRequest;
import org.slf4j.Logger;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ReservationServiceTest {
    private static final Logger log = LoggerFactory.getLogger(ReservationServiceTest.class);

    private Long ticketId;

    @Autowired
    private TicketService ticketService;
    @Autowired
    private TicketStockService ticketStockService;
    @Autowired
    private ReservationService reservationService;


    @BeforeEach
    void beforeEach() {
        this.ticketId = null;
    }

    @AfterEach
    void afterEach() {
        log.info("afterEach : 데이터베이스 초기화");
        reservationService.deleteAll();
        ticketStockService.deleteAll();
        ticketService.deleteAll();
    }

    @Test
    public void getCountTest() throws Exception {
        // given
        ReservationRequest.CreateReservationRequest request = new ReservationRequest.CreateReservationRequest();
        String name = "eunmi";
        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket newTicket = new Ticket("ticket1");
        Long ticketId = ticketService.createTicket(newTicket);
        request.setName(name);
        request.setTicketId(ticketId);
        TicketStock newTicketStock = new TicketStock(newTicket, 1);
        ticketStockService.save(newTicketStock);
        this.ticketId = ticketId;
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);

        // when
        reservationService.reserve(request);


        // then
        Ticket ticket = ticketService.findTicketById(ticketId);
        int reservationCount = reservationService.getReservationCount(ticket);
        log.info("예약 갯수 : {}", reservationCount);


    }

    @Test
    @DisplayName("티켓 예매자는 티켓을 예매한다.")
    public void reservationTest() throws Exception {
        // given
        ReservationRequest.CreateReservationRequest request = new ReservationRequest.CreateReservationRequest();
        String name = "eunmi";
        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket newTicket = new Ticket("ticket1");
        Long ticketId = ticketService.createTicket(newTicket);
        request.setName(name);
        request.setTicketId(ticketId);
        TicketStock newTicketStock = new TicketStock(newTicket, 1);
        ticketStockService.save(newTicketStock);
        this.ticketId = ticketId;
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);

        // when
        reservationService.reserve(request);

        // then
        Ticket ticket = ticketService.findTicketById(ticketId);
        TicketStock ticketStock = ticketStockService.findByTicketId(ticket);
        assertThat(ticketStock.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("티켓 재고가 0이하일 경우 예외가 발생한다.")
    public void reservationTest2() throws Exception {
        // given
        ReservationRequest.CreateReservationRequest request1 = new ReservationRequest.CreateReservationRequest();
        ReservationRequest.CreateReservationRequest request2 = new ReservationRequest.CreateReservationRequest();
        String name = "eunmi";
        String name2 = "spring";

        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket newTicket = new Ticket("ticket1");
        Long ticketId = ticketService.createTicket(newTicket);
        request1.setName(name);
        request1.setTicketId(ticketId);
        request2.setName(name2);
        request2.setTicketId(ticketId);
        TicketStock newTicketStock = new TicketStock(newTicket, 1);
        ticketStockService.save(newTicketStock);
        this.ticketId = ticketId;
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);

        // when
        reservationService.reserve(request1);

        // then
        // 재고 부족으로 예외 발생
        assertThatThrownBy(() -> reservationService.reserve(request2))
                .isInstanceOf(RuntimeException.class);

        // 재고 확인
        Ticket ticket = ticketService.findTicketById(ticketId);
        TicketStock ticketStock = ticketStockService.findByTicketId(ticket);
        assertThat(ticketStock.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("동시성 테스트 - 100명이 동시에 같은 재고가 100장인 티켓을 예매할 경우, 예매가 성공한다.")
    public void reservationException1() throws Exception {
        // given
        int threadCount = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);  // thread 생성
        CountDownLatch latch = new CountDownLatch(threadCount);  // 모든 스레드 작업이 끝날 때까지 await() 대기

        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket newTicket = new Ticket("ticket1");
        Long ticketId = ticketService.createTicket(newTicket);
        TicketStock newTicketStock = new TicketStock(newTicket, 500);
        ticketStockService.save(newTicketStock);
        this.ticketId = ticketId;
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);

        log.info("티켓 예매를 시작합니다. : {}", ticketId);
        AtomicInteger success_cnt = new AtomicInteger();
        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            executor.submit(() -> {
                try {
                    while (true) {
                        try {
                            Thread.sleep(100);  // 과도한 루프 방지용 살짝 쉬기
                            ReservationRequest.CreateReservationRequest request = new ReservationRequest.CreateReservationRequest();
                            String name = "member" + idx;
                            request.setName(name);
                            request.setTicketId(ticketId);
                            reservationService.reserve(request);
                            log.info("예매 성공 : {}", "member" + idx);
                            success_cnt.incrementAndGet();
                            break;  // 성공 시 루프 탈출
                        } catch (Exception e) {
                            if (e.getMessage().contains("매진") || e.getMessage().contains("재고 부족")) {
                                log.info("예매 종료 : {}번 회원 - {}", idx, e.getMessage());
                                break;  // 재고 소진 시 루프 종료
                            }
                            log.info("예매 재시도 : {}번 회원 - {}", idx, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
//        for (int i = 0; i < threadCount; i++) {
//            final int idx = i + 1;
//            executor.submit(() -> {
//                try {
//                    try {
//                        Thread.sleep(100);
//                        reservationService.reserve("member" + idx, ticketId);
//                        log.info("예매 성공 : {}", "member" + idx);
//                        success_cnt.addAndGet(1);
//                    } catch (Exception e) {
//                        log.info("예매 실패 : {}번 회원 {}", idx, e.getMessage());
//                    }
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }

        latch.await(); // thread 끝날 때까지 대기

        // then
        Ticket ticket = ticketService.findTicketById(ticketId);
        TicketStock stock = ticketStockService.findByTicketId(ticket);
        log.info("결과 - 예매 성공 : {} 장, 남은 티켓 : {} 장", success_cnt, stock.getQuantity());
        assertThat(reservationService.findAll().size()).isEqualTo(success_cnt.get());
        assertThat(stock.getQuantity()).isZero(); // 재고가 정확히 0이어야 성공
    }

    @Test
    @DisplayName("예외 테스트 - 동시에 1장의 티켓을 예매할 경우, 1명의 회원만 예매가 성공한다.")
    public void reservationException2() throws Exception {
        // given
        log.info("beforeEach : 티켓 생성 및 재고 등록");
        Ticket newTicket = new Ticket("ticket1");
        Long ticketId = ticketService.createTicket(newTicket);
        TicketStock newTicketStock = new TicketStock(newTicket, 1);
        ticketStockService.save(newTicketStock);
        this.ticketId = ticketId;
        log.info("티켓 초기화 완료 : 티켓 번호는 {}", ticketId);

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);  // thread 생성
        CountDownLatch latch = new CountDownLatch(threadCount);  // 모든 스레드 작업이 끝날 때까지 await() 대기

        log.info("티켓 예매를 시작합니다. : {}", ticketId);
        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            executor.submit(() -> {
                try {
                    try {
                        Thread.sleep(100);
                        ReservationRequest.CreateReservationRequest request = new ReservationRequest.CreateReservationRequest();
                        String name = "member" + idx;
                        request.setName(name);
                        request.setTicketId(ticketId);
                        reservationService.reserve(request);
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
        assertThat(reservationService.findAll().size()).isEqualTo(1);
        Ticket ticket = ticketService.findTicketById(ticketId);
        TicketStock stock = ticketStockService.findByTicketId(ticket);
        assertThat(stock.getQuantity()).isZero(); // 재고가 정확히 0이어야 성공
    }
}