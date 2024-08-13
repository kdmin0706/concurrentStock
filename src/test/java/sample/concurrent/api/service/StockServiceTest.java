package sample.concurrent.api.service;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sample.concurrent.domain.Stock;
import sample.concurrent.domain.StockRepository;

@SpringBootTest
class StockServiceTest {

  @Autowired
  private StockService stockService;

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
  @DisplayName("상품의 재고를 감소시킵니다.")
  void decreaseStock() {
    // given // when
    stockService.decrease(stock.getId(), 1L);

    // then
    Stock persistStock = stockRepository.findById(stock.getId()).orElseThrow();
    assertThat(99L).isEqualTo(persistStock.getQuantity());
  }

  @Test
  @Disabled
  @DisplayName("동시에 100개의 요청이 들어온다.")
  void sameTime_100_request() throws InterruptedException {
    // given
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          stockService.decrease(stock.getId(), 1L);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    // then
    Stock persistStock = stockRepository.findById(stock.getId()).orElseThrow();
    assertThat(persistStock.getQuantity()).isZero();
  }

}