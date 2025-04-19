package org.example.ticketserver.service;

import lombok.RequiredArgsConstructor;
import org.example.ticketserver.dto.TicketRequest;
import org.example.ticketserver.entity.Ticket;
import org.example.ticketserver.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    // 티켓 등록
    @Transactional
    public Long createTicket(Ticket ticket) {
        Ticket savedTicket = ticketRepository.save(ticket);
        return savedTicket.getId();
    }

    // 티켓 단건 조회
    public Ticket findTicketById(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("존재하지 않는 티켓입니다."));
    }

    // 티켓명 수정
    @Transactional
    public Ticket updateTicket(TicketRequest.UpdateTicketRequest dto) {
        Ticket ticket = findTicketById(dto.getId());
        ticket.setName(dto.getName());
        return ticket;
    }

    // 티켓 삭제
    @Transactional
    public void deleteTicket(Long ticketId) {
        Ticket ticket = findTicketById(ticketId);
        ticketRepository.delete(ticket);
    }
}
