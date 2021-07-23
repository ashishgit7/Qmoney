
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;
  private static final String TOKEN = "c4abf39698643b9f35764bfcb2bc0bd0ecc2405d"; 
  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
    throws JsonProcessingException, StockQuoteServiceException {
      if(from.compareTo(to)>=0) throw new RuntimeException();
      List<Candle> stocksStartToEndDate;
     String uri = buildUri(symbol, from, to);
     try{
     String stock = restTemplate.getForObject(uri, String.class);
    //  TiingoCandle[] stocks = ((RestTemplate) restTemplate).getForObject(uri, TiingoCandle[].class);
    ObjectMapper objectMapper = getObjectMapper(); 
    TiingoCandle[] stocks = objectMapper.readValue(stock, TiingoCandle[].class);
    if(stocks!=null){
      stocksStartToEndDate = Arrays.asList(stocks);
    }
    else{
      stocksStartToEndDate = Arrays.asList(new TiingoCandle[0]);
    }
  }catch(NullPointerException e){
    throw new StockQuoteServiceException("Error occured when requesting response from Tiingo API",e.getCause());
  }
    return stocksStartToEndDate;

      // List<Candle> stockList = Arrays.asList(stocks);

      //   if(stockList == null){
      //     return new ArrayList<Candle>();
      //   }
      //   else return stockList;

  }
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String key = "c4abf39698643b9f35764bfcb2bc0bd0ecc2405d";
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/"+symbol+"/prices?"
            + "startDate="+startDate.toString()+"&endDate="+endDate.toString()+"&token="+key;
       return uriTemplate;
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.





  // TODO: CRIO_TASK_MODULE_EXCEPTIONS
  //  1. Update the method signature to match the signature change in the interface.
  //     Start throwing new StockQuoteServiceException when you get some invalid response from
  //     Tiingo, or if Tiingo returns empty results for whatever reason, or you encounter
  //     a runtime exception during Json parsing.
  //  2. Make sure that the exception propagates all the way from
  //     PortfolioManager#calculateAnnualisedReturns so that the external user's of our API
  //     are able to explicitly handle this exception upfront.

  //CHECKSTYLE:OFF


}
