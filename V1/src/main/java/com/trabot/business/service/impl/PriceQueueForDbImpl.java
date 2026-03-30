package com.trabot.business.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trabot.business.service.PriceQueueForDb;
import com.trabot.persistance.model.entities.InstantPrice;
import com.trabot.persistance.repository.InstantPriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceQueueForDbImpl implements PriceQueueForDb {
    
    private final ConcurrentLinkedQueue<InstantPrice> queue = new ConcurrentLinkedQueue<>();
    private final InstantPriceRepository instantPriceRepository;

    @Override
    public void append(Set<InstantPrice> prices) {

	for (InstantPrice instantPrice : prices) {
            queue.add(instantPrice);
        }
    }
    
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processQueue() {
        List<InstantPrice> instantPrices = new ArrayList<>();
        while(!queue.isEmpty()) {
            InstantPrice instantPrice = queue.poll();
            if (instantPrice != null) {
                instantPrices.add(instantPrice);
            }
        }
        instantPriceRepository.saveAll(instantPrices);
    }

}
