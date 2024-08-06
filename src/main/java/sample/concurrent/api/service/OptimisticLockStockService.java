package sample.concurrent.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.concurrent.domain.Stock;
import sample.concurrent.domain.StockRepository;

@Service
@RequiredArgsConstructor
public class OptimisticLockStockService {

  private final StockRepository stockRepository;

  @Transactional
  public void decrease(Long id, Long quantity) {
    Stock stock = stockRepository.findByIdWithOptimisticLock(id);

    stock.decrease(quantity);

    stockRepository.save(stock);
  }
}
