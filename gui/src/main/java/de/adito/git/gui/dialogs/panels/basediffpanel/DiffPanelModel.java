package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.IChangeDelta;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Model for the Panels in the DiffPanel/MergePanel
 *
 * @author m.kaspera 13.12.2018
 */
public class DiffPanelModel
{

  private final Observable<IDeltaTextChangeEvent> fileChangesObservable;
  private final EChangeSide changeSide;
  private Consumer<IChangeDelta> doOnAccept;
  private Consumer<IChangeDelta> doOnDiscard;

  /**
   * @param pFileChangesObservable Observable of the IFileChangesEvents of the IFileDiff this model is for
   * @param pChangeSide            Which side of an IFileChangeChunk this model should utilize
   */
  DiffPanelModel(Observable<IDeltaTextChangeEvent> pFileChangesObservable, EChangeSide pChangeSide)
  {
    fileChangesObservable = pFileChangesObservable;
    changeSide = pChangeSide;
  }

  public Observable<IDeltaTextChangeEvent> getFileChangesObservable()
  {
    return fileChangesObservable;
  }

  /**
   * @return Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "accept changes" icon
   */
  public Consumer<IChangeDelta> getDoOnDiscard()
  {
    return doOnDiscard;
  }

  /**
   * @return Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "discard changes" icon
   */
  @Nullable
  public Consumer<IChangeDelta> getDoOnAccept()
  {
    return doOnAccept;
  }

  /**
   * @param pDoOnAccept Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "accept changes" icon
   * @return this Object for fluent calls
   */
  public DiffPanelModel setDoOnAccept(@NotNull Consumer<IChangeDelta> pDoOnAccept)
  {
    doOnAccept = pDoOnAccept;
    return this;
  }

  /**
   * @param pDoOnDiscard Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "discard changes" icon
   * @return this Object for fluent calls
   */
  public DiffPanelModel setDoOnDiscard(@Nullable Consumer<IChangeDelta> pDoOnDiscard)
  {
    doOnDiscard = pDoOnDiscard;
    return this;
  }

  public EChangeSide getChangeSide()
  {
    return changeSide;
  }
}
