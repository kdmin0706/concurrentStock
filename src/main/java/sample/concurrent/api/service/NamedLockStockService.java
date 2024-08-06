package sample.concurrent.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sample.concurrent.domain.Stock;
import sample.concurrent.domain.StockRepository;

@Service
@RequiredArgsConstructor
public class NamedLockStockService {

  private final StockRepository stockRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void decrease(Long id, Long quantity) {
    Stock stock = stockRepository.findById(id).orElseThrow();

    stock.decrease(quantity);

    stockRepository.saveAndFlush(stock);
  }
}
