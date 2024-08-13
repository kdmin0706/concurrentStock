package sample.concurrent.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.concurrent.api.aop.DistributedLock;
import sample.concurrent.domain.Stock;
import sample.concurrent.domain.StockRepository;

@Service
@RequiredArgsConstructor
public class DistributeLockStockService {

  private final StockRepository stockRepository;

  @Transactional  //기존
  public void decrease(Long id, Long quantity) {
    Stock stock = stockRepository.findById(id).orElseThrow();

    stock.decrease(quantity);
  }

  @DistributedLock(key = "#lockName")
  public void decrease(String lockName, Long id, Long quantity) {
    Stock stock = stockRepository.findById(id).orElseThrow();

    stock.decrease(quantity);
  }
}
