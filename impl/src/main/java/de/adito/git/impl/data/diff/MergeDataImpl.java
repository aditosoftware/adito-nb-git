package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author m.kaspera, 06.03.2020
 */
public class MergeDataImpl implements IMergeData
{

  private final IFileDiff yourSideDiff;
  private final IFileDiff theirSideDiff;

  public MergeDataImpl(IFileDiff pYourSideDiff, IFileDiff pTheirSideDiff)
  {
    yourSideDiff = pYourSideDiff;
    theirSideDiff = pTheirSideDiff;
  }

  @Override
  public String getFilePath()
  {
    if (yourSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME && theirSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME)
      return yourSideDiff.getFileHeader().getFilePath();
    else if (yourSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME)
    {
      return yourSideDiff.getFileHeader().getFilePath();
    }
    else if (theirSideDiff.getFileHeader().getChangeType() != EChangeType.RENAME)
    {
      return theirSideDiff.getFileHeader().getFilePath();
    }
    else
    {
      if (theirSideDiff.getFile(EChangeSide.OLD).equals(yourSideDiff.getFile(EChangeSide.OLD)))
        return theirSideDiff.getFileHeader().getFilePath(EChangeSide.OLD);
      else return theirSideDiff.getFileHeader().getFilePath(EChangeSide.NEW);
    }
  }

  @NotNull
  @Override
  public IFileDiff getDiff(@NotNull EConflictSide conflictSide)
  {
    return conflictSide == EConflictSide.YOURS ? yourSideDiff : theirSideDiff;
  }

  @NotNull
  @Override
  public IDeltaTextChangeEvent acceptDelta(@NotNull IChangeDelta acceptedDelta, @NotNull EConflictSide conflictSide)
  {
    if (acceptedDelta.getChangeStatus().getChangeStatus() == EChangeStatus.UNDEFINED)
    {
      throw new IllegalArgumentException("Cannot accept a delta of state UNDEFINED");
    }
    IDeltaTextChangeEvent deltaTextChangeEvent;
    if (conflictSide == EConflictSide.YOURS)
    {
      deltaTextChangeEvent = yourSideDiff.acceptDelta(acceptedDelta);
      theirSideDiff.processTextEvent(deltaTextChangeEvent.getOffset(), deltaTextChangeEvent.getLength(), deltaTextChangeEvent.getText());
    }
    else
    {
      deltaTextChangeEvent = theirSideDiff.acceptDelta(acceptedDelta);
      yourSideDiff.processTextEvent(deltaTextChangeEvent.getOffset(), deltaTextChangeEvent.getLength(), deltaTextChangeEvent.getText());
    }
    return deltaTextChangeEvent;
  }

  @Override
  public void discardChange(@NotNull IChangeDelta discardedDelta, @NotNull EConflictSide conflictSide)
  {
    if (conflictSide == EConflictSide.YOURS)
      yourSideDiff.discardDelta(discardedDelta);
    else
      theirSideDiff.discardDelta(discardedDelta);
  }

  @Override
  public void reset()
  {
    yourSideDiff.reset();
    theirSideDiff.reset();
  }

  @Override
  public void modifyText(String text, int length, int offset)
  {
    if (text == null)
    {
      yourSideDiff.processTextEvent(offset, length, null);
      theirSideDiff.processTextEvent(offset, length, null);
    }
    else
    {
      yourSideDiff.processTextEvent(offset, length, text);
      theirSideDiff.processTextEvent(offset, length, text);
    }
  }
}
