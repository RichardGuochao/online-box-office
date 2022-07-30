package com.github.budwing.obo.schedule.service.impl;

import com.github.budwing.obo.schedule.entity.Schedule;
import com.github.budwing.obo.schedule.entity.Ticket;
import com.github.budwing.obo.schedule.repository.ScheduleRepository;
import com.github.budwing.obo.schedule.repository.TicketRepository;
import com.github.budwing.obo.schedule.service.ScheduleService;
import com.github.budwing.obo.schedule.vo.Seat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultScheduleService implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public void changeStatus(String scheduleId, Schedule.Status status) {
        Schedule schedule = scheduleRepository.getReferenceById(scheduleId);
        schedule.setStatus(status);
        scheduleRepository.save(schedule);

        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("http://localhost:8081/obo/cinema/%d/hall/%d/seat",
                1, 1);
        log.debug("send request to {} to query seats", url);
        List<Map> seats = restTemplate.getForObject(url, List.class);
        log.debug("got seats:{}", seats);
        List<Ticket> tickets = seats.stream()
                .map(m -> new Ticket(schedule, Seat.of(m), 40.00))
                .collect(Collectors.toList());
        ticketRepository.saveAll(tickets);
    }
}
