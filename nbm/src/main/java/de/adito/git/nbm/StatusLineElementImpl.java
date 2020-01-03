package de.adito.git.nbm;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.gui.popup.PopupWindow;
import de.adito.git.gui.window.content.IWindowContentProvider;
import de.adito.git.nbm.observables.ActiveProjectObservable;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Create a popup for all branches in one observableOptRepo in the status line.
 *
 * @author a.arnold, 05.11.2018
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class StatusLineElementImpl implements StatusLineElementProvider, IDiscardable
{
  private IWindowContentProvider windowContentProvider = IGitConstants.INJECTOR.getInstance(IWindowContentProvider.class);
  private JLabel label = new JLabel(NbBundle.getMessage(StatusLineElementImpl.class, "Invalid.Initialized"));
  private PopupWindow popupWindow;
  private Disposable disposable;

  public StatusLineElementImpl()
  {
    _initPopup();
    label.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent pE)
      {
        popupWindow.setVisible(true);
        popupWindow.setLocation(pE.getLocationOnScreen().x - popupWindow.getWidth(), pE.getLocationOnScreen().y - popupWindow.getHeight());
      }
    });
    EventQueue.invokeLater(this::_setStatusLineName);
  }

  /**
   * Set the name in the status line to the actual branch name
   */
  private void _setStatusLineName()
  {
    //noinspection StatusLineElement only exists once
    disposable = RepositoryUtility.getRepositoryObservable()
        .switchMap(pRepo -> pRepo.isPresent() ? pRepo.get().getRepositoryState() : Observable.just(Optional.<IRepositoryState>empty()))
        .subscribe(pRepoState -> label.setText(pRepoState.map(pState -> pState.getCurrentBranch().getSimpleName()
            + (pState.getState() != IRepository.State.SAFE ? " | " + pState.getState() : ""))
                                                   .orElse("<no branch>")));
  }

  private void _initPopup()
  {
    JComponent statusLineWindowContent = windowContentProvider.createStatusLineWindowContent(RepositoryUtility.getRepositoryObservable());
    popupWindow = new PopupWindow(WindowManager.getDefault().getMainWindow(), "Git Branches", statusLineWindowContent);
    statusLineWindowContent.putClientProperty("parent", popupWindow);
  }

  @Override
  public Component getStatusLineElement()
  {
    return label;
  }

  @Override
  public void discard()
  {
    if (disposable != null && !disposable.isDisposed())
    {
      disposable.dispose();
      disposable = null;
    }
    ActiveProjectObservable.dispose();
  }
}
