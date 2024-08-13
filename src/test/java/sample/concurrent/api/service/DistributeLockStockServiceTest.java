package sample.concurrent.api.service;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sample.concurrent.domain.Stock;
import sample.concurrent.domain.StockRepository;

@SpringBootTest
class DistributeLockStockServiceTest {

  @Autowired
  private DistributeLockStockService distributeLockStockService;

  @Autowired
  private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock(1L, 100L);
    stockRepository.save(stock);
  }

  @AfterEach
  void tearDown() {
    stockRepository.deleteAllInBatch();
  }


  @Test
  @DisplayName("동시에 100개의 요청이 들어온다.(분산락)")
  void sameTime_100_request_toDistributeLock() throws InterruptedException {
    // given
    int numberOfThreads = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);

    // when
    for (int i = 0; i < numberOfThreads; i++) {
      executorService.submit(() -> {
        try {
          // 분산락 적용 메서드 호출
          distributeLockStockService.decrease("LOCK", stock.getId(), 1L);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // then
    Stock persistStock = stockRepository.findById(stock.getId()).orElseThrow();
    assertThat(persistStock.getQuantity()).isEqualTo(0);

    System.out.println("잔여 재고 개수 : " + persistStock.getQuantity());
  }
}