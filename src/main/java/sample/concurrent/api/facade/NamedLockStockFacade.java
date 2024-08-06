package sample.concurrent.api.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sample.concurrent.api.service.NamedLockStockService;
import sample.concurrent.domain.LockRepository;

@Component
@RequiredArgsConstructor
public class NamedLockStockFacade {

  private final LockRepository lockRepository;
  private final NamedLockStockService namedLockStockService;

  @Transactional
  public void decrease(Long id, Long quantity) {
    try {
      lockRepository.getLock(id.toString());
      namedLockStockService.decrease(id, quantity);
    } finally {
      lockRepository.releaseLock(id.toString());
    }
  }

}
