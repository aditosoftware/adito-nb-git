package de.adito.git.api.data;

import de.adito.git.api.data.diff.IMergeData;
import lombok.NonNull;

import java.util.List;

/**
 * Represents information about a merge, such as a list of conflicts and a description of where the conflicting versions originated
 *
 * @author m.kaspera, 04.11.2020
 */
public interface IMergeDetails
{

  /**
   * @return List with the conflicts of the merge/rebase/cherryPick
   */
  @NonNull
  List<IMergeData> getMergeConflicts();

  /**
   * Branch/Commit or general description of where the YOURS side of the conflict originates
   *
   * @return String
   */
  @NonNull
  String getYoursOrigin();

  /**
   * Branch/Commit or general description of where the THEIRS side of the conflict originates
   *
   * @return String
   */
  @NonNull
  String getTheirsOrigin();

}
