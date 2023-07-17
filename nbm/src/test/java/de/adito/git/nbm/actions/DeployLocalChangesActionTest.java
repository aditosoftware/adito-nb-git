package de.adito.git.nbm.actions;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.common.IProjectQuery;
import de.adito.git.api.data.IFileStatus;
import lombok.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.MockedStatic;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;

import java.util.*;
import java.util.regex.Matcher;
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
   * check if the icon resource is not null
   */
  @Test
  void getIconResource()
  {
    assertNotNull(deployLocalChangesAction.iconResource());
  }

  /**
   * check, if the name returns the default value
   */
  @Test
  void getName()
  {
    assertNotNull(deployLocalChangesAction.getName());
  }

  /**
   * check, if the help returns the default value
   */
  @Test
  void getHelpCtx()
  {
    assertNull(deployLocalChangesAction.getHelpCtx());
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
      Node nodeMock1 = mock(Node.class);
      Node nodeMock2 = mock(Node.class);
      doReturn("project1").when(nodeMock1).getDisplayName();
      doReturn("project2").when(nodeMock2).getDisplayName();

      return Stream.of(
          Arguments.of(new Node[]{nodeMock1}, true),
          Arguments.of(new Node[]{nodeMock1, nodeMock2}, false)
      );
    }

    /**
     * parameterized test to check that the action is only available if one project is selected
     *
     * @param nodes
     * @param expectedResult the expected result
     */
    @ParameterizedTest
    @MethodSource("provideNodeArrays")
    void shouldReturnExpectedResult(Node[] nodes, boolean expectedResult)
    {
      DeployLocalChangesAction deploySpy = spy(DeployLocalChangesAction.class);
      doReturn((nodes.length)).when(deploySpy).getCountOfSelectedProjects(nodes);
      boolean result = deploySpy.enable(nodes);

      assertEquals(expectedResult, result);
    }
  }

  /**
   * Checks the method {@link DeployLocalChangesAction#getCountOfSelectedProjects}
   */
  @Nested
  class GetCountOfSelectedProjects
  {

    /**
     * Checks if the given nodes returns the expected amount of projects
     */
    @Test
    void shouldReturnCountOfSelectedNodes()
    {
      try (var mockedStat = mockStatic(IProjectQuery.class))
      {
        Node nodeMock1 = mock(Node.class);
        Project project1 = mock(Project.class);
        IProjectQuery projectQuery = mock(IProjectQuery.class);

        when(IProjectQuery.getInstance()).thenReturn(projectQuery);
        doReturn(project1).when(projectQuery).findProjects(nodeMock1, IProjectQuery.ReturnType.MULTIPLE_TO_NULL);

        int result = deployLocalChangesAction.getCountOfSelectedProjects(new Node[]{nodeMock1});

        assertEquals(1, result);
      }
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

      Optional optional = mock(Optional.class);
      IFileStatus fileStatus = mock(IFileStatus.class);
      Optional<IFileStatus> optionalIFileStatus = Optional.of(fileStatus);
      doReturn(Set.<String>of("/myTest/myTestEntity/myProcess/process.js")).when(fileStatus).getUncommittedChanges();

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
  @Nested
  class GetSourcesToDeploy
  {
    /**
     * checks if an empty list is returned when parameter is empty
     */
    @Test
    void shouldReturnEmptyList()
    {
      assertEquals(new ArrayList<>(), deployLocalChangesAction.getSourcesToDeploy(new HashSet<>()));
    }

    /**
     * checks if the empty provided list will not trigger the matcher-function
     */
    @Test
    void shouldReturnEmptyListIfUncommitedChangesAreEmpty()
    {
      try (MockedStatic<Matcher> matcherMockedStatic = mockStatic(Matcher.class))
      {
        Matcher matcher = mock(Matcher.class);

        assertAll(
            () -> assertEquals(new ArrayList<>(), deployLocalChangesAction.getSourcesToDeploy(new HashSet<>())),
            () -> verify(matcher, never()).group(anyInt())
        );
      }
    }

    /**
     * checks if the function with the Regex returns the expected list
     */
    @Test
    void shouldFindMatcherAndAddToList()
    {
      List<String> expected = List.of("myEntity");

      Set<String> set = Set.of("entity/myEntity/myEntity.aod");

      assertEquals(expected, deployLocalChangesAction.getSourcesToDeploy(set));
    }
  }
}