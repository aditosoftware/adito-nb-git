package de.adito.git.gui.dialogs.panels.basediffpanel;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChangesEvent;
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

  private final Observable<IFileChangesEvent> fileChangesObservable;
  private final EChangeSide changeSide;
  private Consumer<IFileChangeChunk> doOnAccept;
  private Consumer<IFileChangeChunk> doOnDiscard;

  /**
   * @param pFileChangesObservable Observable of the IFileChangesEvents of the IFileDiff this model is for
   * @param pChangeSide Which side of an IFileChangeChunk this model should utilize
   */
  DiffPanelModel(Observable<IFileChangesEvent> pFileChangesObservable, EChangeSide pChangeSide)
  {
    fileChangesObservable = pFileChangesObservable;
    changeSide = pChangeSide;
  }

  public Observable<IFileChangesEvent> getFileChangesObservable()
  {
    return fileChangesObservable;
  }

  /**
   * @return Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "accept changes" icon
   */
  public Consumer<IFileChangeChunk> getDoOnDiscard()
  {
    return doOnDiscard;
  }

  /**
   * @return Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "discard changes" icon
   */
  @Nullable
  public Consumer<IFileChangeChunk> getDoOnAccept()
  {
    return doOnAccept;
  }

  /**
   * @param pDoOnAccept Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "accept changes" icon
   * @return this Object for fluent calls
   */
  public DiffPanelModel setDoOnAccept(@NotNull Consumer<IFileChangeChunk> pDoOnAccept)
  {
    doOnAccept = pDoOnAccept;
    return this;
  }

  /**
   * @param pDoOnDiscard Consumer that determines what happens with the IFileChangeChunk if the user clicks on the "discard changes" icon
   * @return this Object for fluent calls
   */
  public DiffPanelModel setDoOnDiscard(@Nullable Consumer<IFileChangeChunk> pDoOnDiscard)
  {
    doOnDiscard = pDoOnDiscard;
    return this;
  }

  public EChangeSide getChangeSide()
  {
    return changeSide;
  }
}
