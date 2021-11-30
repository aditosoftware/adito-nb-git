package de.adito.git.impl.ssh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author m.kaspera, 30.11.2021
 */
class ClearHttpCacheHandlerTest
{

  /**
   * Stellt sicher, dass die Reflections passen
   */
  @Test
  void doReflectionsWork()
  {
    Assertions.assertDoesNotThrow(() -> ClearHttpCacheHandler.clearCache("https://gitlab.adito.de"));
  }
}