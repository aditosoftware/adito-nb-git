package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

  @Override
  public void acceptDelta(@NotNull IChangeDelta acceptedDelta, @NotNull EConflictSide conflictSide)
  {
    if (acceptedDelta.getChangeStatus().getChangeStatus() == EChangeStatus.UNDEFINED)
    {
      throw new IllegalArgumentException("Cannot accept a delta of state UNDEFINED");
    }
    List<IDeltaTextChangeEvent> deltaTextChangeEvents;
    if (conflictSide == EConflictSide.YOURS)
    {
      deltaTextChangeEvents = yourSideDiff.acceptDelta(acceptedDelta);
      deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> theirSideDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                            pDeltaTextChangeEvent.getLength(),
                                                                                            pDeltaTextChangeEvent.getText(), EChangeSide.OLD));
    }
    else
    {
      deltaTextChangeEvents = theirSideDiff.acceptDelta(acceptedDelta);
      deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> yourSideDiff.processTextEvent(pDeltaTextChangeEvent.getOffset(),
                                                                                           pDeltaTextChangeEvent.getLength(),
                                                                                           pDeltaTextChangeEvent.getText(), EChangeSide.OLD));
    }
  }

  @Override
  public void discardChange(@NotNull IChangeDelta discardedDelta, @NotNull EConflictSide conflictSide)
  {
    if (conflictSide == EConflictSide.YOURS)
    {
      yourSideDiff.discardDelta(discardedDelta);
      theirSideDiff.processTextEvent(0, 0, null, EChangeSide.OLD);
    }
    else
    {
      theirSideDiff.discardDelta(discardedDelta);
      yourSideDiff.processTextEvent(0, 0, null, EChangeSide.OLD);
    }
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
      yourSideDiff.processTextEvent(offset, length, null, EChangeSide.OLD);
      theirSideDiff.processTextEvent(offset, length, null, EChangeSide.OLD);
    }
    else
    {
      yourSideDiff.processTextEvent(offset, length, text, EChangeSide.OLD);
      theirSideDiff.processTextEvent(offset, length, text, EChangeSide.OLD);
    }
  }

  @Override
  public void markConflicting()
  {
    theirSideDiff.markConflicting(yourSideDiff);
    yourSideDiff.markConflicting(theirSideDiff);
  }
}
