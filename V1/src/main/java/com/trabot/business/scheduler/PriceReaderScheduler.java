package com.trabot.business.scheduler;

import java.io.IOException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trabot.business.process.PriceReaderProcess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
@RequiredArgsConstructor
public class PriceReaderScheduler {
    
    
    private final PriceReaderProcess priceReaderProcess;

    private static final boolean SKIP = true;
    
//    @Async
//    @Scheduled( cron = "* * * * * ?")
    public void priceReader() throws IOException {
	
	if(SKIP) {
	    return;
	}
	
	long timestamp = System.currentTimeMillis();
	log.info("PriceReader scheduler start - {}", timestamp);
	
	priceReaderProcess.ingestPrice(timestamp);
	
	long diff = System.currentTimeMillis() - timestamp;
	
	if(diff > 1000) {
	    throw new RuntimeException("Price scheduler took more than one second");
	}else {
	    
	    log.info("PriceReader scheduler took {}ms to finish - {}", diff, timestamp);
	}
        
	
	/*
	 * 1 Call PriceReader to have a simple price list btc 28000; eth 1800; etc
	 * 2 Call InstantPriceCalculator passing the simple price and passing the thread safe data structure inside PriceContainer
	 * 3 Push the calculated InstantPRice inside PriceContainer and append it to PriceQueu.
	 * 
	 * In a different scheduler
	 * 1 Check if one of the pairs has condition to open a order
	 * 2 If yes ...
	 * 
	 * In a third scheduler (check if could be low priorised)
	 * 1 Pop the PriceQueue and write it into DB (maybe bulk write every minute or so
	 */
	
	
	
	
	
//	InstantPrice instantPrice = new InstantPrice();
//	BigDecimal price = new BigDecimal(bingXManager.getPrice(BingXManager.BITCOIN_USDT));
//	instantPrice.setVelocity(price);
//	instantPriceRepository.save(instantPrice);
	
//	List<String> cryptocurrencies = List.of(BingXManager.BITCOIN_USDT, BingXManager.ETH_USDT/* other cryptocurrencies */);
//
//        List<CompletableFuture<InstantPrice>> futures = cryptocurrencies.stream()
//                .map(this::getPrice)
//                .collect(Collectors.toList());
//
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenRun(() -> {
//                    List<InstantPrice> prices = futures.stream()
//                            .map(CompletableFuture::join)
//                            .collect(Collectors.toList());
//
//                    instantPriceRepository.saveAll(prices);
//                });
	
	
	
	
    }
    
//    private CompletableFuture <InstantPrice> getPrice(String pair) {
//	
//	long now = System.currentTimeMillis();
//	CompletableFuture<InstantPrice> future = new CompletableFuture<>();
//	InstantPrice instantPrice = new InstantPrice();
//	BigDecimal price = new BigDecimal(bingXManager.getPrice(pair));
//	instantPrice.setVelocity(price);
//	instantPrice.setPair(pair);
//	instantPrice.setTimestamp(now);
//	future.complete(instantPrice);
//	
//	return future;
//	
//    }
}
