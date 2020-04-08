package de.adito.git.api.data;

import de.adito.git.api.data.diff.IFileChangeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for data structure that provides meta-information about a diff
 *
 * @author m.kaspera, 14.05.2019
 */
public interface IDiffInfo
{

  /**
   * Commit that was used as base of the diff
   *
   * @return ICommit that describes the commit that was used as the base for the diff
   */
  @NotNull
  ICommit getBaseCommit();

  /**
   * parent commit of the diff, in case the base commit was a merge commit or several commits were selected
   *
   * @return ICommit that describes the parent commit for this diff, or the oldest commit if several commits were selected
   */
  @NotNull
  ICommit getParentCommit();

  /**
   * List of changes files, and how they changed
   *
   * @return List with the changed files
   */
  @NotNull
  List<IFileChangeType> getChangedFiles();

}
