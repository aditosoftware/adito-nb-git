package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

/**
 * Interface that defines the data points a commit filter should have
 *
 * @author m.kaspera, 17.05.2019
 */
public interface ICommitFilter extends Predicate<ICommit>
{

  /**
   * @param pBranch defines the branch all commits should have in common
   * @return this Builder
   */
  ICommitFilter setBranch(IBranch pBranch);

  /**
   * @param pFileList defines the files (Note: an OR condition should be used for the list of files, so if a commit has changed one of the files the condition is
   *                  met and not all files have to be changed by each commit meeting this criterium) that were changed by a commit
   * @return this Builder
   */
  ICommitFilter setFileList(List<File> pFileList);

  /**
   * @param pAuthor defines the author of the commits
   * @return this Builder
   */
  ICommitFilter setAuthor(String pAuthor);

  /**
   * @param pStartDate load commits from this point in time onwards, null if not specified
   * @return this Builder
   */
  ICommitFilter setStartDate(Instant pStartDate);

  /**
   * @param pEndDate load commits up until this point in time, null if not specified
   * @return this Builder
   */
  ICommitFilter setEndDate(Instant pEndDate);

  /**
   * defines the branch all commits should have in common
   *
   * @return the IBranch that all commits should belong to, or null if not specified
   */
  @Nullable IBranch getBranch();

  /**
   * defines the files (Note: an OR condition should be used for the list of files, so if a commit has changed one of the files the condition is met and not all files
   * have to be changed by each commit meeting this criterium) that were changed by a commit
   *
   * @return List of files where each loaded commit has touched at least one of the files in the list. Emtpy if not specified
   */
  @NotNull List<File> getFiles();

  /**
   * defines the author of the commits
   *
   * @return Author that each commit should be from, or null if not specified
   */
  @Nullable String getAuthor();

  /**
   * defines the startpoint of a timeInterval
   *
   * @return load commits from this point in time onwards, null if not specified
   */
  @Nullable Instant getStartDate();

  /**
   * defines the endpoint of a timeInterval
   *
   * @return load commits up until this point in time, null if not specified
   */
  @Nullable Instant getEndDate();

}
