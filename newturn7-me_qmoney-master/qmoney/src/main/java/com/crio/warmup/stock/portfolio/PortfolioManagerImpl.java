
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;
import java.io.*;

public class PortfolioManagerImpl implements PortfolioManager {

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  private RestTemplate restTemplate;
  private StockQuotesService stockQuoteService;
  private ExecutorService threadPool = null;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  protected PortfolioManagerImpl(StockQuotesService stockQuoteService) {
    this.stockQuoteService = stockQuoteService;
  }

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
        return stockQuoteService.getStockQuote(symbol,from,to);
    //  if(from.compareTo(to)>=0) throw new RuntimeException();

    //  String uri = buildUri(symbol, from, to);
    //  TiingoCandle[] stocks = ((RestTemplate) restTemplate).getForObject(uri, TiingoCandle[].class);
    //   List<Candle> stockList = Arrays.asList(stocks);

    //     if(stockList == null){
    //       return new ArrayList<Candle>();
    //     }
    //     else return stockList;
  
    }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String key = "c4abf39698643b9f35764bfcb2bc0bd0ecc2405d";
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/"+symbol+"/prices?"
            + "startDate="+startDate.toString()+"&endDate="+endDate.toString()+"&token="+key;
       return uriTemplate;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
      throws StockQuoteServiceException {
    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    for(int i=0; i< portfolioTrades.size();i++){
      annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i),endDate);
      annualizedReturns.add(annualizedReturn);
    }
    Comparator<AnnualizedReturn> sortByAnnReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns,sortByAnnReturn);
    return annualizedReturns;
  }

  private AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate)
      throws StockQuoteServiceException {
    // AnnualizedReturn annualizedReturn;
    // String symbol = trade.getSymbol();
    // LocalDate startLocalDate = trade.getPurchaseDate();
    // try{
    //   List<Candle> stocksStartToEndDate;
    //   stocksStartToEndDate = getStockQuote(symbol, startLocalDate, endLocalDate);
    //   Candle stockLatest = stocksStartToEndDate.get(stocksStartToEndDate.size()-1);
    //   Candle stockStartDate = stocksStartToEndDate.get(0);
    //   Double buyPrice = stockStartDate.getOpen();
    //   Double sellPrice = stockLatest.getClose();
    //   Double totalReturn = (sellPrice - buyPrice)/buyPrice;
    //   Double numYears = (double) ChronoUnit.DAYS.between(startLocalDate,endLocalDate)/365;
    //   Double annualReturns = Math.pow(1+totalReturn,(1/numYears))-1;
    //   annualizedReturn = new AnnualizedReturn(symbol, annualReturns, totalReturn);
    // }
    // catch (JsonProcessingException e){
    //   annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    // }
    
    // return annualizedReturn;
    LocalDate startDate = trade.getPurchaseDate();
  String symbol = trade.getSymbol(); 
 
  Double buyPrice = 0.0, sellPrice = 0.0;
 
  try {
    LocalDate startLocalDate = trade.getPurchaseDate();
 
    List<Candle> stocksStartToEndFull = getStockQuote(symbol, startLocalDate, endDate);
 
    Collections.sort(stocksStartToEndFull, (candle1, candle2) -> { 
      return candle1.getDate().compareTo(candle2.getDate()); 
    });
    
    Candle stockStartDate = stocksStartToEndFull.get(0);
    Candle stocksLatest = stocksStartToEndFull.get(stocksStartToEndFull.size() - 1);
 
    buyPrice = stockStartDate.getOpen();
    sellPrice = stocksLatest.getClose();
    endDate = stocksLatest.getDate();
 
  } catch (JsonProcessingException e) {
    throw new RuntimeException();
  }
  Double totalReturn = (sellPrice - buyPrice) / buyPrice;
 
  long daysBetweenPurchaseAndSelling = ChronoUnit.DAYS.between(startDate, endDate);
  Double totalYears = (double) (daysBetweenPurchaseAndSelling) / 365;
 
  Double annualizedReturn = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;
  return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);
  }

  @Override
public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
    List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads) 
    throws InterruptedException, StockQuoteServiceException {

      List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
      List<Callable<List<Object>>> callableTasks = new ArrayList<>();
  
      if (threadPool == null)
        threadPool = Executors.newFixedThreadPool(numThreads);
  
      for (PortfolioTrade trade : portfolioTrades) {
        String symbol = trade.getSymbol();
        LocalDate startDate = trade.getPurchaseDate();
  
        callableTasks.add(() -> {
          List<Candle> quotes = stockQuoteService.getStockQuote(symbol, startDate, endDate);
  
          return Arrays.asList(quotes, symbol, startDate);
        });
      }
  
      List<Future<List<Object>>> futureTasks = threadPool.invokeAll(callableTasks);
  
      for (Future<List<Object>> task : futureTasks) {
        LocalDate startDate = LocalDate.now();
        String symbol = "";
        List<Candle> quotes = new ArrayList<>();
  
        try {
          quotes = (List<Candle>) task.get().get(0);
          symbol = (String) task.get().get(1);
          startDate = (LocalDate) task.get().get(2);
        } catch (ExecutionException e) {
          throw new StockQuoteServiceException(e.getMessage());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
  
        double totalNumOfYears = (double) startDate.until(endDate, DAYS) / 365.24;
        double buyPrice = quotes.get(0).getOpen();
        double sellPrice = quotes.get(quotes.size() - 1).getClose();
  
        Double totalReturns = (sellPrice - buyPrice) / buyPrice;
        Double annualizedReturn = Math.pow((1 + totalReturns), (1 / totalNumOfYears)) - 1;
  
        annualizedReturns.add(new AnnualizedReturn(symbol, annualizedReturn, totalReturns));
      }
  
      Collections.sort(annualizedReturns, this.getComparator());
  
      threadPool.shutdown();
      try {
        if (!threadPool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
          threadPool.shutdownNow();
        }
      } catch (InterruptedException e) {
        threadPool.shutdownNow();
      }
  
      return annualizedReturns;
}


  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
