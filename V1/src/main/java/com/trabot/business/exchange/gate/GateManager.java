package com.trabot.business.exchange.gate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.stereotype.Service;

import com.trabot.business.exchange.ExchangeManager;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.pojo.CryptoPrice;

import io.gate.gateapi.ApiClient;
import io.gate.gateapi.ApiException;
import io.gate.gateapi.api.SpotApi;
import io.gate.gateapi.api.SpotApi.APIlistOrderBookRequest;
import io.gate.gateapi.models.BatchOrder;
import io.gate.gateapi.models.Order;
import io.gate.gateapi.models.Order.TimeInForceEnum;
import io.gate.gateapi.models.Order.TypeEnum;
import io.gate.gateapi.models.OrderBook;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GateManager implements ExchangeManager {//japan

    private static final String API_KEY = "e196403cd6ae3e1868933074dcf55af1";
    private static final String SECRET_KEY = "111b932c154c79c02f6d8da83b4085c117fb61f6fdacade9418bde2687ea4fdd";

    private final SpotApi spotApi;

    public GateManager() {

	ApiClient defaultClient = new ApiClient();// Configuration.getDefaultApiClient();
	defaultClient.setBasePath("https://api.gateio.ws/api/v4");

	defaultClient.setApiKeySecret(API_KEY, SECRET_KEY);
	spotApi = new SpotApi(defaultClient);
	

    }

    public void placeOrder(Position position) throws ClientProtocolException, IOException {

	Order order = positionToOrder(position);
	try {
	    Order created = spotApi.createOrder(order);
	    log.info("Order status {}", created);
	} catch (ApiException e) {
	    log.error(e.getMessage(), e);
	}

    }

    public void placeOrdersBatch(List<Position> positions) throws ClientProtocolException, IOException {

	List<Order> orders = positions.stream().map(this::positionToOrder).collect(Collectors.toList());

	try {
	    log.info("sending {}", positions);
	    List<BatchOrder> created = spotApi.createBatchOrders(orders);
	    log.info("Order status {}", created);

	} catch (ApiException e) {
	    log.error(e.getMessage(), e);
	}

    }

    @Override
    public Set<CryptoPrice> readPriceList(Set<String> symbols) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Map<String, String> buildGetParamsFromSymbols(Set<String> symbols) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getBaseUrl() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getApiUrl() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void getMarketData(String symbol) throws IOException {

	APIlistOrderBookRequest response = spotApi.listOrderBook(symbol);
	try {
	    OrderBook orderBook = response.execute();
	    log.info("{} Order book: {}", symbol, orderBook);
	    
	} catch (ApiException e) {
	    log.error(e.getMessage(), e);
	}
    }

    @Override
    public void history(String stockUsdt) {
	// TODO Auto-generated method stub

    }

//    public static void main(String[] args) {
//	GateManager gateManager = new GateManager();
//	int totalUSDTtoInvest = 20;
//	String symbol = "BTC_USDT";
//	BigDecimal price = new BigDecimal(24000);
//	BigDecimal volume = new BigDecimal(totalUSDTtoInvest).divide(price, 4, RoundingMode.HALF_UP);
//	Position position = new Position(symbol, volume);
//	position.setPrice(price);
//	try {
//	    gateManager.placeOrder(position);
//	} catch (ClientProtocolException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	} catch (IOException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
//    }

    private Order positionToOrder(Position position) {

	TimeInForceEnum timeInForceEnum = null;
	Order.TypeEnum orderTypeEnum = null;
	switch (position.getTradeType()) {
	case LIMIT:
	    timeInForceEnum = TimeInForceEnum.GTC;
	    orderTypeEnum = TypeEnum.LIMIT;
	    break;

	case IOC:
	    timeInForceEnum = TimeInForceEnum.IOC;
	    orderTypeEnum = TypeEnum.LIMIT;
	    break;

	default:
	    throw new NotYetImplementedException(
		    String.format("trade type %s not yet implemented", position.getTradeType()));
	}

	Order order = new Order();
	order.setAccount(Order.AccountEnum.SPOT);
	order.setSide(Order.SideEnum.BUY);
	order.setAutoBorrow(false);
	order.setTimeInForce(timeInForceEnum);
	order.setType(orderTypeEnum);
	order.setAmount(position.getVolume().toString());
	order.setPrice(position.getPrice().toString());
	order.setCurrencyPair(position.getSymbol());
	order.setText("t-Ciao");
	return order;

    }

}
