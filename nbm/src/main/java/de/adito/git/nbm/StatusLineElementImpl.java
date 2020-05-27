package de.adito.git.nbm;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.gui.window.content.IWindowContentProvider;
import de.adito.git.nbm.observables.ActiveProjectObservable;
import de.adito.git.nbm.util.RepositoryUtility;
import de.adito.swing.popup.PopupMouseAdapter;
import de.adito.swing.popup.PopupWindow;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;

/**
 * Create a popup for all branches in one observableOptRepo in the status line.
 *
 * @author a.arnold, 05.11.2018
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 10000)
public class StatusLineElementImpl implements StatusLineElementProvider, IDiscardable
{

  private final JPanel statusLinePanel;
  private JComponent branchWindowContent;
  private final IWindowContentProvider windowContentProvider = IGitConstants.INJECTOR.getInstance(IWindowContentProvider.class);
  private final JLabel label = new JLabel(NbBundle.getMessage(StatusLineElementImpl.class, "Invalid.Initialized"));
  private PopupWindow popupWindow;
  private Disposable disposable;

  public StatusLineElementImpl()
  {
    _initPopup();
    JSeparator separator = new JSeparator(SwingConstants.VERTICAL)
    {
      @Override
      public Dimension getPreferredSize()
      {
        return new Dimension(3, 3); // Y-unimportant -> gridlayout will stretch it
      }
    };
    separator.setBorder(new EmptyBorder(1, 1, 1, 1));

    label.setBorder(new EmptyBorder(0, 10, 0, 10));
    label.addMouseListener(new PopupMouseAdapter(popupWindow, label, branchWindowContent));
    statusLinePanel = new JPanel(new BorderLayout());
    statusLinePanel.add(separator, BorderLayout.WEST);
    statusLinePanel.add(label, BorderLayout.CENTER);
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
        .subscribe(pRepoState -> label.setText(pRepoState.map(this::_getDisplayValue).orElse("<no branch>")));
  }

  /**
   * Creates the displayed value for the current branch. If the current state is anything other than SAFE, the state is appended to the name of the branch
   *
   * @param pRepositoryState RepositoryState for which to create the display string
   * @return String with name of the branch and state of the repo, if the state is not equal to SAFE
   */
  private String _getDisplayValue(IRepositoryState pRepositoryState)
  {
    return pRepositoryState.getCurrentBranch().getSimpleName() + (pRepositoryState.getState() != IRepository.State.SAFE ? " - " + pRepositoryState.getState() : "");
  }

  private void _initPopup()
  {
    branchWindowContent = windowContentProvider.createBranchWindowContent(RepositoryUtility.getRepositoryObservable());
    JScrollPane contentScrollPane = new JScrollPane(branchWindowContent);
    contentScrollPane.setBorder(null);
    popupWindow = new PopupWindow(WindowManager.getDefault().getMainWindow(), "Git Branches", contentScrollPane);
    branchWindowContent.putClientProperty("parent", popupWindow);
  }

  @Override
  public Component getStatusLineElement()
  {
    return statusLinePanel;
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
