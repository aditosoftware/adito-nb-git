package de.adito.git.impl.data;

import de.adito.git.api.data.IMergeDetails;
import de.adito.git.api.data.diff.IMergeData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author m.kaspera, 04.11.2020
 */
public class MergeDetailsImpl implements IMergeDetails
{


  private final List<IMergeData> mergeData;
  private final String yoursOrigin;
  private final String theirsOrigin;

  public MergeDetailsImpl(List<IMergeData> pMergeData, String pYoursOrigin, String pTheirsOrigin)
  {
    mergeData = pMergeData;
    yoursOrigin = pYoursOrigin;
    theirsOrigin = pTheirsOrigin;
  }

  @Override
  public @NotNull List<IMergeData> getMergeConflicts()
  {
    return mergeData;
  }

  @Override
  public @NotNull String getYoursOrigin()
  {
    return yoursOrigin;
  }

  @Override
  public @NotNull String getTheirsOrigin()
  {
    return theirsOrigin;
  }
}
