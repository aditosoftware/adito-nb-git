package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.openide.windows.TopComponent;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testclass for {@link DeployLocalChangesAction}.
 *
 * @author F.Adler, 03.07.2023
 */
class DeployLocalChangesActionTest
{
  private DeployLocalChangesAction deploy;

  /**
   * initialize the needed object
   */
  @BeforeEach
  void init()
  {
    deploy = new DeployLocalChangesAction();
  }

  /**
   * check, if the name returns null
   */
  @Test
  void getName()
  {
    assertNotNull(deploy.getName());
  }

  /**
   * check, if the help returns null
   */
  @Test
  void getHelpCtx()
  {
    assertNull(deploy.getHelpCtx());
  }

  /**
   * Checks the method {@link DeployLocalChangesAction#actionPerformed}
   */
  @Nested
  class ActionPerformed
  {
    /**
     * checks, if no project and repository are available and therefore should return a log
     */
    @Test
    void shouldNotThrowIfNoProjectsOrRepositoryAvailableAndShouldReturnLog()
    {
      try (MockedStatic<Logger> loggerMockedStatic = mockStatic(Logger.class))
      {
        Logger mockedLogger = mock(Logger.class);
        loggerMockedStatic.when(() -> Logger.getLogger(any())).thenReturn(mockedLogger);


        Observable test2 = spy(Observable.class);
        System.out.println(test2.blockingFirst().getClass());

        assertAll(
            () -> assertDoesNotThrow(() -> deploy.actionPerformed(mock(ActionEvent.class))),
            () -> verify(mockedLogger).log(Level.INFO, "No Repository was found")
        );
      }
    }

    @Test
    void shouldReturnLogWhenStatusIsEmptyOrNotPresent()
    {

      TopComponent y = new TopComponent();
      var stuff = mock(TopComponent.Registry.class);
      var test = ProjectUtility.findProjectFromActives(stuff);

      try (MockedStatic<Logger> loggerMockedStatic = mockStatic(Logger.class);
           MockedStatic<RepositoryCache> cache = mockStatic(RepositoryCache.class);
      )
      {
        RepositoryCache cache2 = mock(RepositoryCache.class);
        Observable<Optional<IRepository>> test2 = mock(Observable.class);
        IRepository test3 = mock(IRepository.class);
        doReturn(test2).when(cache2).findRepository(test.get());
        doReturn(test2.blockingFirst()).when(test2).blockingFirst().isPresent();
        //when(test2.blockingFirst()).thenReturn(test2.blockingFirst().get().getStatus().blockingFirst().get().getUncommitted().get().getFile().);

        Logger mockedLogger = mock(Logger.class);
        loggerMockedStatic.when(() -> Logger.getLogger(any())).thenReturn(mockedLogger);

        assertAll(
            () -> assertDoesNotThrow(() -> deploy.actionPerformed(mock(ActionEvent.class))),
            () -> verify(mockedLogger).log(Level.WARNING, "a")
        );
      }

    }
    
  }
}