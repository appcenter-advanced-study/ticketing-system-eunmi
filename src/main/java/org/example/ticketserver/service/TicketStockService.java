package org.example.ticketserver.service;

import lombok.RequiredArgsConstructor;
import org.example.ticketserver.dto.TicketStockRequest;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.entity.TicketStock;
import org.example.ticketserver.repository.TicketRepository;
import org.example.ticketserver.repository.TicketStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketStockService {
    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;

    // 티켓 재고 등록
    @Transactional
    public Long save(TicketStock ticketStock) {
        TicketStock savedStock = ticketStockRepository.save(ticketStock);
        return savedStock.getId();
    }

    // 티켓 재고 수정
    @Transactional
    public TicketStock updateTicketStock(TicketStockRequest.UpdateTicketStock dto) {
        TicketStock ticketStock = findByTicketId(dto.getTicketId());
        ticketStock.setQuantity(dto.getQuantity());
        return ticketStock;
    }

    // 티켓 재고 조회
    @Transactional
    public TicketStock findByTicketId(Ticket ticketId) {
        return ticketStockRepository.findByTicketId(ticketId).orElseThrow(() -> new RuntimeException("티켓 재고가 존재하지 않습니다."));
    }

    // 티켓 재고 삭제
    @Transactional
    public void deleteTicketStock(Ticket ticketId) {
        TicketStock ticketStock = findByTicketId(ticketId);
        ticketStockRepository.delete(ticketStock);
    }

    @Transactional
    public void deleteAll() {
        ticketStockRepository.deleteAll();
    }
}
