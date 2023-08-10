package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.data.diff.IMergeData;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the MergeAction class
 *
 * @author m.kaspera, 09.08.2023
 */
class MergeActionTest
{

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  class CreateSuggestedCommitMessage
  {

    /**
     * @param pCurrentBranch Name of the current branch
     * @param pMergeBranch   Name of the branch that is merged into the current one
     * @param pFilePaths     List with conflicting filePaths
     */
    @ParameterizedTest
    @MethodSource("suggestedCommitMessageSource")
    void doesMessageContainNecessaryInfos(@NonNull String pCurrentBranch, @NonNull String pMergeBranch, @NonNull List<String> pFilePaths)
    {
      IRepository repository = createMockRepo(pCurrentBranch);

      IBranch mergeBranch = mock(IBranch.class);
      when(mergeBranch.getSimpleName()).thenReturn(pMergeBranch);

      List<IMergeData> mergeData = new ArrayList<>();
      pFilePaths.forEach(pFilePath -> {
        IMergeData data = mock(IMergeData.class);
        when(data.getFilePath()).thenReturn(pFilePath);
        mergeData.add(data);
      });

      String suggestedCommitMessage = MergeAction.createSuggestedCommitMessage(repository, mergeBranch, mergeData);

      pFilePaths.forEach(filePath -> assertTrue(suggestedCommitMessage.contains(filePath)));
      assertAll(() -> assertEquals(pFilePaths.isEmpty(), !suggestedCommitMessage.contains("\n")),
                // check if the branch names are mentioned in the message
                () -> assertTrue(suggestedCommitMessage.contains(pCurrentBranch)),
                () -> assertTrue(suggestedCommitMessage.contains(pMergeBranch)));
    }

    /**
     * create a mocked repository
     *
     * @param pCurrentBranch Name of the current branch
     * @return Mocked IRepository that returns a mocked repositoryState that as the current Branch set (mocked again)
     */
    @NonNull
    private IRepository createMockRepo(@NonNull String pCurrentBranch)
    {
      IRepository repository = mock(IRepository.class);
      IRepositoryState repositoryState = mock(IRepositoryState.class);
      IBranch currentBranch = mock(IBranch.class);

      when(repository.getRepositoryState()).thenReturn(Observable.just(Optional.of(repositoryState)));
      when(repositoryState.getCurrentBranch()).thenReturn(currentBranch);
      when(currentBranch.getSimpleName()).thenReturn(pCurrentBranch);
      return repository;
    }

    /**
     * @return Stream of Arguments for the doesMessageContainConflictingFiles test
     */
    public Stream<Arguments> suggestedCommitMessageSource()
    {
      return Stream.of(Arguments.of("main", "dev", List.of()),
                       Arguments.of("main", "dev", List.of("entity/org/process/test.js")),
                       Arguments.of("main", "dev", List.of("package.json", "")),
                       Arguments.of("main", "dev", List.of("package.json", "entity/org/org_entity.aod")));
    }

  }

}