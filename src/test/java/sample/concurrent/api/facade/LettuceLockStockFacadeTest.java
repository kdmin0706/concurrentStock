package sample.concurrent.api.facade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sample.concurrent.domain.Stock;
import sample.concurrent.domain.StockRepository;

@SpringBootTest
class LettuceLockStockFacadeTest {

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private LettuceLockStockFacade lettuceLockStockFacade;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock(1L, 100L);
    stockRepository.save(stock);
  }

  @AfterEach
  void tearDown() {
    stockRepository.deleteAll();
  }


  @Test
  public void 동시에_100개의_요청() throws InterruptedException {
    int threadCount = 100;  //100개의 요청을 보냄
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          lettuceLockStockFacade.decrease(stock.getId(), 1L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    Stock persistStock = stockRepository.findById(stock.getId()).orElseThrow();
    assertThat(persistStock.getQuantity()).isZero();
  }
}