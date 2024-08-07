package sample.concurrent.api.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sample.concurrent.api.service.StockService;
import sample.concurrent.domain.RedisLockRepository;

@Component
@RequiredArgsConstructor
public class LettuceLockStockFacade {

  private final RedisLockRepository redisLockRepository;
  private final StockService stockService;

  public void decrease(Long id, Long quantity) throws InterruptedException {
    while (!redisLockRepository.lock(id)) {
      Thread.sleep(100);
    }

    try {
      stockService.decrease(id, quantity);
    } finally {
      redisLockRepository.unlock(id);
    }

  }

}
