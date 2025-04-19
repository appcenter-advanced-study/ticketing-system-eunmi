package org.example.ticketserver.service;

import lombok.RequiredArgsConstructor;
import org.example.ticketserver.entity.Reservation;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.example.ticketserver.mapper.ReservationMapper;
import org.example.ticketserver.repository.ReservationRepository;
import org.example.ticketserver.repository.TicketRepository;
import org.example.ticketserver.repository.TicketStockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 티켓 예매
     * @param name
     * @param ticketId
     */
    @Transactional
    public void reserve(String name, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 티켓입니다."));
        TicketStock ticketStock = ticketStockRepository.findByTicketId(ticket)
                .orElseThrow(() -> new RuntimeException("해당 티켓의 재고를 찾을 수 없습니다."));
        ticketStock.reserve();
        Reservation reservation = ReservationMapper.toEntity(name, ticket);
        ticketStockRepository.save(ticketStock);
        reservationRepository.save(reservation);
        logger.info("현재 남은 티켓 수 : {}장", ticketStock.getQuantity());
    }

}
