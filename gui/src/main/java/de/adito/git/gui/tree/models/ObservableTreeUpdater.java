package de.adito.git.gui.tree.models;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IDiffInfo;
import de.adito.git.api.data.diff.IFileChangeType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.NonNull;

import java.util.List;

/**
 * Class that serves as a connection between an observable and a TreeModel, telling the TreeModel to update itself each time it gets new data from the observable
 * Can also execute tasks before and after the model is updated (by passing the "after" task on, since it is assumed that the model update is done via SwingWorker - in
 * which case the "after" part would normally be after the SwingWorker completed the non-EDT part. After should be after the EDT part though, so the SwingWorker is
 * responsible to execute the "after" part on its own)
 *
 * @author m.kaspera, 28.08.2019
 */
public class ObservableTreeUpdater<T> implements IDiscardable
{

  private final Observable<List<T>> changeList;
  private final IFileSystemUtil fileSystemUtil;
  private final Runnable[] doAfterJobs;
  private final Runnable[] doBeforeJobs;
  private Disposable disposable;
  private BaseObservingTreeModel<T> treeModel;

  /**
   * @param pChangeList     Observable on whose data the treeModel is based on
   * @param pTreeModel      TreeModel whose data should be in sync with the Observable
   * @param pFileSystemUtil IFileSystemUtil to load the icons
   * @param pDoAfterJobs    Jobs/Actions that should be executed after the model finished updating. NOTE: Will be executed in the EDT
   * @param pDoBeforeJobs   Jobs/Actions that should be executed before the model starts updating. NOTE: Will be executed in an RXJava computation thread
   */
  public ObservableTreeUpdater(@NonNull Observable<List<T>> pChangeList, @NonNull BaseObservingTreeModel<T> pTreeModel,
                               IFileSystemUtil pFileSystemUtil, Runnable[] pDoAfterJobs, Runnable... pDoBeforeJobs)
  {
    changeList = pChangeList;
    treeModel = pTreeModel;
    fileSystemUtil = pFileSystemUtil;
    doAfterJobs = pDoAfterJobs;
    doBeforeJobs = pDoBeforeJobs;
    disposable = pChangeList.subscribe(this::_changeHappened);
  }

  /**
   * Swaps the model for another
   *
   * @param pNewModel new Model, changes in the observable will be routed to this model in the future
   */
  public void swapModel(BaseObservingTreeModel<T> pNewModel)
  {
    treeModel.discard();
    treeModel = pNewModel;
    disposable.dispose();
    disposable = changeList.subscribe(this::_changeHappened);
  }

  private void _changeHappened(List<T> pCurrentElements)
  {
    // assume that the whole list is of type IFileChangeType if the first elemtent is of that type, preLoad the icons if of type IFileChangeType
    if (!pCurrentElements.isEmpty() && fileSystemUtil != null)
    {
      if (pCurrentElements.get(0) instanceof IFileChangeType)
      {
        fileSystemUtil.preLoadIcons((List<IFileChangeType>) pCurrentElements);
      }
      else if (pCurrentElements.get(0) instanceof IDiffInfo)
      {
        pCurrentElements.forEach(pDiffInfo -> fileSystemUtil.preLoadIcons(((IDiffInfo) pDiffInfo).getChangedFiles()));
      }
    }
    for (Runnable doBeforeJob : doBeforeJobs)
    {
      doBeforeJob.run();
    }
    treeModel._treeChanged(pCurrentElements, doAfterJobs);
  }

  @Override
  public void discard()
  {
    treeModel.discard();
    disposable.dispose();
  }
}
