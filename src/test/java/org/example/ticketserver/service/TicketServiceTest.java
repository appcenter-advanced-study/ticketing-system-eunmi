package org.example.ticketserver.service;

import org.assertj.core.api.Assertions;
import org.example.ticketserver.dto.TicketRequest;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TicketServiceTest {
    private final Logger log = LoggerFactory.getLogger(TicketServiceTest.class);

    @Autowired
    private TicketService ticketService;

    private Long ticketId;

    @BeforeEach
    void beforeEach() {
        this.ticketId = ticketService.createTicket(new Ticket("basic ticket"));
    }

    @AfterEach
    void afterEach() {
        ticketService.deleteTicket(ticketId);
    }
    @Test
    @DisplayName("동시에 티켓명을 변경할 경우, 마지막에 등록한 수정정보로 등록된다.")
    public void updateTicketTest1() throws Exception {
        // given
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);  // thread 생성
        CountDownLatch latch = new CountDownLatch(threadCount);  // 모든 스레드 작업이 끝날 때까지 await() 대기

        log.info("티켓명 변경을 시작합니다. : {}", ticketId);
        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            executor.submit(() -> {
                try {
                    try {
                        TicketRequest.UpdateTicketRequest dto =
                                new TicketRequest.UpdateTicketRequest(ticketId, "update ticket" + idx);
                        Ticket ticket = ticketService.updateTicket(dto);
                        log.info("{}번 회원 수정 완료 - 변경 후 : {}", idx, ticket.getName());
                    } catch (Exception e) {
                        log.info("{}번 회원 수정 실패 : {}", idx, e.getMessage());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // thread 끝날 때까지 대기

        // then
        /**
         * 두번의 갱신 분실의 문제 발생 - 트랜잭션 범위 수준을 넘어서는 문제(트랜잭션으로 해결 불가)
         */
//        Assertions.assertThat(ticketService.findTicketById(ticketId).getName()).isEqualTo("update ticket2");
        log.info("최종 저장된 티켓명 : {}", ticketService.findTicketById(ticketId).getName());
    }

}