package org.example.ticketserver.service;

import lombok.RequiredArgsConstructor;
import org.example.ticketserver.dto.reservation.ReservationRequest;
import org.example.ticketserver.entity.Reservation;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.example.ticketserver.kafka.KafkaProducer;
import org.example.ticketserver.mapper.ReservationMapper;
import org.example.ticketserver.repository.ReservationRepository;
import org.example.ticketserver.repository.TicketRepository;
import org.example.ticketserver.repository.TicketStockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;
    private final ReservationRepository reservationRepository;


    /**
     * 티켓 예매
     * @param dto
     */
    @Transactional
    @KafkaListener(topics = KafkaProducer.TOPIC_NAME, groupId = "ticket")
    public void reserve(ReservationRequest.CreateReservationRequest dto) throws InterruptedException {
        logger.info("Reservation request ticketId: {}", dto.getTicketId());
        Ticket ticket = ticketRepository.findById(dto.getTicketId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 티켓입니다."));
        TicketStock ticketStock = ticketStockRepository.findByTicketId(ticket)
                .orElseThrow(() -> new RuntimeException("해당 티켓의 재고를 찾을 수 없습니다."));
        ticketStock.reserve();
        Reservation reservation = ReservationMapper.toEntity(dto.getName(), ticket);
        reservationRepository.save(reservation);
        ticketStockRepository.save(ticketStock);
        logger.info("현재 남은 티켓 수 : {}장", ticketStock.getQuantity());
    }

    // 예매 취소(삭제)
    @Transactional
    public void cancel(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    public int getReservationCount(Ticket ticketId) {
        List<Reservation> reservation = reservationRepository.findAllByTicketId(ticketId);
        return reservation.size();
    }

    @Transactional
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    public void deleteAll() {
        reservationRepository.deleteAll();
    }
}
