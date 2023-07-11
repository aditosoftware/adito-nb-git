package de.adito.git.nbm.actions;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.regex.Matcher;

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
      assertDoesNotThrow(() -> deployLocalChangesAction.actionPerformed(mock(ActionEvent.class)));
    }

    /**
     * Checks if the "deploy"-method is called and do nothing
     */
    @Test
    void shouldCallDeploy()
    {
      List<String> uncommittedFiles = new ArrayList<>();
      uncommittedFiles.add("test");

      DeployLocalChangesAction deploySpy = spy(DeployLocalChangesAction.class);

      doNothing().when(deploySpy).deploy(any());
      doReturn(uncommittedFiles).when(deploySpy).getSourcesToDeploy(new HashSet<>());

      deploySpy.actionPerformed(mock(ActionEvent.class));

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
      List<String> expected = new ArrayList<>();
      expected.add("myEntity");

      Set<String> set = new HashSet<>();
      set.add("entity/myEntity/myEntity.aod");

      assertEquals(expected, deployLocalChangesAction.getSourcesToDeploy(set));
    }
  }
}