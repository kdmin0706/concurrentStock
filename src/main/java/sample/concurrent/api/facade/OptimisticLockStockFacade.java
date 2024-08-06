package sample.concurrent.api.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sample.concurrent.api.service.OptimisticLockStockService;

@Component
@RequiredArgsConstructor
public class OptimisticLockStockFacade {

  private final OptimisticLockStockService optimisticLockStockService;

  public void decrease(Long id, Long quantity) throws InterruptedException {
    while (true) {
      try {
        optimisticLockStockService.decrease(id, quantity);

        break;
      } catch (Exception e) {
        Thread.sleep(50);
      }
    }
  }

}
