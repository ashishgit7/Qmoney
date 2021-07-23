
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {

  public static final String TOKEN = "Z3W5RNAJEGY8DJ1Q";
public static final String Function = "TIME_SERIES_DAILY";
private RestTemplate restTemplate;
protected AlphavantageService (RestTemplate restTemplate) {
  this.restTemplate = restTemplate;
  }
  protected String buildUri (String symbol){
    String uri = String.format("https://www.alphavantage.co/query?function=%s&symbol=%s&output=full&apikey=%s",Function,symbol,TOKEN);
    return uri;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException,
      StockQuoteServiceException {
    String url = buildUri(symbol);
    
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<Candle> stocks = new ArrayList<>();
    try{
    String apiResponse = restTemplate.getForObject(url, String.class);
    Map<LocalDate,AlphavantageCandle> dailyResponses = objectMapper.readValue(apiResponse,
    AlphavantageDailyResponse.class).getCandles();
    
    for(LocalDate date = from;!date.isAfter(to);date = date.plusDays(1)){
      AlphavantageCandle candle = dailyResponses.get(date);
      if(candle!=null){
        candle.setDate(date); stocks.add(candle);
      }
    }
  }catch(NullPointerException e){
    throw new StockQuoteServiceException("Alphavantage returned invalid response");
  }
  return stocks;
  }
  

};
  // TODO: CRIO_TASK_MODULE_EXCEPTIONS
  //   1. Update the method signature to match the signature change in the interface.
  //   2. Start throwing new StockQuoteServiceException when you get some invalid response from
  //      Alphavantage, or you encounter a runtime exception during Json parsing.
  //   3. Make sure that the exception propagates all the way from PortfolioManager, so that the
  //      external user's of our API are able to explicitly handle this exception upfront.
  //CHECKSTYLE:OFF



