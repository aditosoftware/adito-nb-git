package de.adito.git.gui.window.content;

import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.exception.AditoGitException;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Optional;

/**
 * Label logic for the abort action in the branch menu, if the repository is in a safe state the contained label is set to show nothing,
 * otherwise this class tries to display text that is suitable for the current state (e.g. "Abort merge" if the repo is in an unsafe merging state)
 *
 * The MouseListeners that execute the actual abort are also already included
 *
 * @author m.kaspera, 07.01.2020
 */
class AbortLabelController extends ObservingLabelController<IRepositoryState>
{
  private final BranchWindowContent branchWindowContent;
  private final Observable<Optional<IRepository>> repositoryObservable;
  private final INotifyUtil notifyUtil;
  private MouseListener mouseListener = voidListener;

  public AbortLabelController(@NotNull BranchWindowContent pBranchWindowContent, @NotNull Observable<Optional<IRepositoryState>> pRepoStateObservable,
                              @NotNull Observable<Optional<IRepository>> pRepositoryObservable, @NotNull INotifyUtil pNotifyUtil)
  {
    super("", pRepoStateObservable);
    branchWindowContent = pBranchWindowContent;
    repositoryObservable = pRepositoryObservable;
    notifyUtil = pNotifyUtil;
    label.addMouseListener(new BranchWindowContent._HoverMouseListener());
    label.addMouseMotionListener(new BranchWindowContent._HoverMouseListener());
  }

  @Override
  protected void updateLabel(@Nullable IRepositoryState pRepositoryState)
  {
    label.removeMouseListener(mouseListener);
    if (pRepositoryState != null)
    {
      if (pRepositoryState.getState() == IRepository.State.MERGING || pRepositoryState.getState() == IRepository.State.MERGING_RESOLVED)
      {
        label.setText("+ Abort Merge");
        label.addMouseListener(new ResetMouseAdapter());
        return;
      }
      else if (pRepositoryState.getState() == IRepository.State.CHERRY_PICKING || pRepositoryState.getState() == IRepository.State.CHERRY_PICKING_RESOLVED)
      {
        label.setText("+ Abort Cherry Pick");
        label.addMouseListener(new ResetMouseAdapter());
        return;
      }
      else if (pRepositoryState.getState() == IRepository.State.REBASING_MERGE || pRepositoryState.getState() == IRepository.State.REBASING
          || pRepositoryState.getState() == IRepository.State.REBASING_REBASING)
      {
        label.setText("+ Abort Rebase/Pull");
        label.addMouseListener(new AbortPullMouseAdapter());
        return;
      }
    }
    // if the repositoryState is null or neither of the above states fit display nothing (no active repo or safe state)
    label.setText("");
    label.addMouseListener(voidListener);
  }

  /**
   * MouseListener that executes a hard reset if the mouse is pressed
   */
  private class ResetMouseAdapter extends MouseAdapter
  {
    @Override
    public void mousePressed(MouseEvent e)
    {
      IRepository repository = repositoryObservable.blockingFirst(Optional.empty()).orElse(null);
      if (repository != null)
      {
        try
        {
          repository.reset(repository.getRepositoryState().blockingFirst().orElseThrow().getCurrentBranch().getId(), EResetType.HARD);
        }
        catch (AditoGitException pE)
        {
          notifyUtil.notify(pE, "Error while resetting HEAD", false);
        }
      }
      branchWindowContent.closeWindow();
    }
  }

  /**
   * MouseListener that executes an abort of a pull if the mouse is pressed
   */
  private class AbortPullMouseAdapter extends MouseAdapter
  {
    @Override
    public void mousePressed(MouseEvent e)
    {
      IRepository repository = repositoryObservable.blockingFirst(Optional.empty()).orElse(null);
      if (repository != null)
      {
        try
        {
          repository.pull(true);
        }
        catch (AditoGitException pE)
        {
          notifyUtil.notify(pE, "Error while aborting the pull", false);
        }
      }
      branchWindowContent.closeWindow();
    }
  }
}
