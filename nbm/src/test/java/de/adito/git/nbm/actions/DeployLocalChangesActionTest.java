package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.openide.nodes.Node;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testclass for {@link DeployLocalChangesAction}.
 *
 * @author F.Adler, 03.07.2023
 */
class DeployLocalChangesActionTest
{
  private DeployLocalChangesAction deployLocalChangesAction;


  /**
   * initialize the needed object
   */
  @BeforeEach
  void init()
  {
    deployLocalChangesAction = new DeployLocalChangesAction();
  }

  /**
   * Check if the icon resource is not null
   */
  @Test
  void getIconResource()
  {
    assertNotNull(deployLocalChangesAction.iconResource());
  }

  /**
   * Check, if the name returns the default value
   */
  @Test
  void getName()
  {
    assertNotNull(deployLocalChangesAction.getName());
  }

  /**
   * Checks if the getIsEnabledObservable method returns the right enabled state
   */
  @Test
  void shouldCallIsEnabled()
  {
    DeployLocalChangesAction deploySpy = spy(DeployLocalChangesAction.class);
    var repository = mock(IRepository.class);

    doReturn(true).when(deploySpy).isEnabled(repository);

    var isEnabledObservable = deploySpy.getIsEnabledObservable(Observable.just(Optional.of(repository)));
    var actual = (isEnabledObservable.blockingFirst());

    assertAll(
        () -> verify(deploySpy).isEnabled(any()),
        () -> assertEquals(Optional.of(true), actual)
    );
  }

  /**
   * Checks the method {@link DeployLocalChangesAction#enable}
   */
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  class Enable
  {
    /**
     * Parameterized Method for the enabled method
     *
     * @return Array of nodes and a corresponding expected result as an argument
     */
    @NonNull
    private Stream<Arguments> provideNodeArrays()
    {
      IRepository repository = spy(IRepository.class);
      IRepository repository2 = spy(IRepository.class);

      IFileStatus fileStatus = spy(IFileStatus.class);
      IFileStatus fileStatus2 = spy(IFileStatus.class);

      //pretend if there are uncommitted changes
      doReturn(Observable.just(Optional.of(fileStatus))).when(repository).getStatus();
      doReturn(true).when(fileStatus).hasUncommittedChanges();

      //pretend there are no uncommitted changes
      doReturn(Observable.just(Optional.of(fileStatus2))).when(repository2).getStatus();
      doReturn(false).when(fileStatus2).hasUncommittedChanges();

      return Stream.of(
          Arguments.of(repository, true),
          Arguments.of(repository2, false),
          Arguments.of(null, false)
      );
    }

    /**
     * Parameterized test to check that the action is only available if one project is selected
     *
     * @param pRepository    the repository that will be checked
     * @param expectedResult the expected result
     */
    @ParameterizedTest
    @MethodSource("provideNodeArrays")
    void shouldReturnExpectedResult(@Nullable IRepository pRepository, boolean expectedResult)
    {
      DeployLocalChangesAction deploySpy = spy(DeployLocalChangesAction.class);

      assertEquals(expectedResult, deploySpy.isEnabled(pRepository));
    }
  }

  /**
   * Checks the method {@link DeployLocalChangesAction#getAllUncommittedChanges}
   */
  @Nested
  class GetAllUncommittedChanges
  {
    /**
     * Checks if the expected value is returned when an uncommitted change exists
     */
    @Test
    void shouldReturnUncommittedChange()
    {
      Set<String> expected = Set.of("/myTest/myTestEntity/myProcess/process.js");

      IFileStatus fileStatus = mock(IFileStatus.class);
      Optional<IFileStatus> optionalIFileStatus = Optional.of(fileStatus);
      doReturn(Set.of("/myTest/myTestEntity/myProcess/process.js")).when(fileStatus).getUncommittedChanges();

      Set<String> result = deployLocalChangesAction.getAllUncommittedChanges(optionalIFileStatus);

      assertAll(
          () -> verify(fileStatus).getUncommittedChanges(),
          () -> verify(fileStatus).getUntracked(),
          () -> assertEquals(expected, result)
      );
    }

    /**
     * Checks that nothing is returned if nothing to deploy was found
     */
    @Test
    void shouldReturnEmptyHashSet()
    {
      Set<String> result = deployLocalChangesAction.getAllUncommittedChanges(Optional.empty());

      assertEquals(new HashSet<>(), result);
    }
  }

  /**
   * Checks the method {@link DeployLocalChangesAction#actionPerformed}
   */
  @Nested
  class ActionPerformed
  {
    /**
     * Checks if no error is thrown when method is called
     */
    @Test
    void shouldNotThrowIfActionPerformedIsCalled()
    {
      assertDoesNotThrow(() -> deployLocalChangesAction.performAction(new Node[]{}));
    }

    /**
     * Checks if the "deploy"-method is called and do nothing
     */
    @Test
    void shouldCallDeploy()
    {
      List<String> uncommittedFiles = List.of("test");

      DeployLocalChangesAction deploySpy = spy(DeployLocalChangesAction.class);

      doNothing().when(deploySpy).deploy(any());
      doReturn(uncommittedFiles).when(deploySpy).getSourcesToDeploy(new HashSet<>());

      deploySpy.performAction(any());

      verify(deploySpy).deploy(uncommittedFiles);
    }
  }

  /**
   * Checks the method {@link DeployLocalChangesAction#getSourcesToDeploy}
   */
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  class GetSourcesToDeploy
  {
    /**
     * Parameterized Method for the getSourcesToDeploy method
     *
     * @return Array of nodes and a corresponding expected result as an argument
     */
    @NonNull
    private Stream<Arguments> provideSetsOfArguments()
    {
      return Stream.of(
          Arguments.of(List.of(), Set.of()),
          Arguments.of(List.of("myEntity"), Set.of("entity/myEntity/myEntity.aod")),
          Arguments.of(List.of("test", "myEntity"), Set.of("entity/myEntity/myEntity.aod", "test/test/test/test/test.js"))
      );
    }


    /**
     * Check if the regex returns the expected result
     *
     * @param pExpected expected result
     * @param pToTest   set of relative file paths as strings
     */
    @ParameterizedTest
    @MethodSource("provideSetsOfArguments")
    void shouldReturnExpectedResults(@NonNull List<String> pExpected, @NonNull Set<String> pToTest)
    {
      assertEquals(pExpected, deployLocalChangesAction.getSourcesToDeploy(pToTest));
    }
  }
}