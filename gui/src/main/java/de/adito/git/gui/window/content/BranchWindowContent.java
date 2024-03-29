package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.actions.*;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.icon.SwingIconLoaderImpl;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.swing.TableLayoutUtil;
import de.adito.util.reactive.cache.*;
import info.clearthought.layout.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import lombok.NonNull;
import org.jetbrains.annotations.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.adito.git.gui.Constants.NEW_BRANCH_ICON;

/**
 * Creates the content for the branch menu.
 *
 * @author a.arnold, 08.11.2018
 */
class BranchWindowContent extends JPanel implements Scrollable, IDiscardable
{
  private static final String POPUP_WINDOW_CLIENT_PROPERTY = "parent";
  private final IAsyncProgressFacade progressFacade;
  private final IActionProvider actionProvider;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> observableOptRepo;
  private ObservableListSelectionModel observableListSelectionModel;
  private final List<JList<IBranch>> branchLists = new ArrayList<>();
  private final ObservableCache observableCache = new ObservableCache();
  private final CompositeDisposable disposable = new CompositeDisposable();
  private final IDialogProvider dialogProvider;

  @Inject
  public BranchWindowContent(IAsyncProgressFacade pProgressFacade, IActionProvider pProvider, INotifyUtil pNotifyUtil,
                             @Assisted Observable<Optional<IRepository>> pObservableOptRepo, @NonNull IDialogProvider pDialogProvider)
  {
    progressFacade = pProgressFacade;
    actionProvider = pProvider;
    notifyUtil = pNotifyUtil;
    observableOptRepo = pObservableOptRepo;
    dialogProvider = pDialogProvider;
    disposable.add(new ObservableCacheDisposable(observableCache));
    Observable<Optional<IRepositoryState>> repoStateObservable = observableOptRepo
        .switchMap(pRepository -> pRepository.map(IRepository::getRepositoryState)
            .orElse(Observable.just(Optional.empty())));
    _initGUI(repoStateObservable);
  }

  /**
   * A method to initialize the GUI
   *
   * @param pRepoStateObservable Observable of the RepositoryState
   */
  private void _initGUI(Observable<Optional<IRepositoryState>> pRepoStateObservable)
  {
    //room between the components
    final double gap = 7;
    double pref = TableLayoutConstants.PREFERRED;
    double[] cols = {TableLayoutConstants.FILL};
    double[] rows = {
        pref,
        pref,
        gap,
        pref,
        pref,
        gap,
        pref,
        pref};

    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(0, 0, new AbortLabelController(this, pRepoStateObservable, observableOptRepo, notifyUtil, progressFacade, dialogProvider).getLabel());
    tlu.add(0, 1, new NewBranchLabelController(observableOptRepo).getLabel());
    tlu.add(0, 3, _createLabel("Local Branches"));
    tlu.add(0, 4, _createListBranches(EBranchType.LOCAL));
    tlu.add(0, 6, _createLabel("Remote Branches"));
    tlu.add(0, 7, _createListBranches(EBranchType.REMOTE));
  }

  private JLabel _createLabel(String pString)
  {
    JLabel jLabel = new JLabel(pString);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    return jLabel;
  }

  @NonNull
  private Observable<Optional<List<IBranch>>> _observeBranches()
  {
    return observableCache.calculateParallel("branches", () -> observableOptRepo
        .switchMap(pOptRepo -> pOptRepo
            .map(pRepo -> {
              try
              {
                return pRepo.getBranches();
              }
              catch (Exception e)
              {
                return Observable.just(Optional.<List<IBranch>>empty());
              }
            })
            .orElse(Observable.just(Optional.empty()))));
  }

  /**
   * Create and fill a list of branches
   *
   * @param pType the {@link EBranchType} of the branches
   * @return a branchList
   */
  private JComponent _createListBranches(EBranchType pType)
  {
    _HoverMouseListener hoverMouseListener = new _HoverMouseListener();
    JList<IBranch> branchList = new JList<>();
    ObservingBranchListModel branchListModel = new ObservingBranchListModel(_observeBranches().map(pBranches -> pBranches.orElse(Collections.emptyList())
        .stream()
        .filter(pBranch -> pBranch.getType().equals(pType))
        .collect(Collectors.toList())));
    disposable.add(branchListModel);
    branchList.setModel(branchListModel);
    observableListSelectionModel = new ObservableListSelectionModel(branchList.getSelectionModel());
    branchList.setSelectionModel(observableListSelectionModel);
    branchList.setCellRenderer(new BranchCellRenderer());
    branchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    if (pType == EBranchType.LOCAL)
      branchList.addMouseListener(new _LocalBranchMouseListener());
    else
      branchList.addMouseListener(new _RemoteBranchMouseListener());
    branchList.addMouseListener(hoverMouseListener);
    branchList.addMouseMotionListener(hoverMouseListener);
    branchLists.add(branchList);
    return branchList;
  }

  void closeWindow()
  {
    JWindow parent = (JWindow) getClientProperty(POPUP_WINDOW_CLIENT_PROPERTY);
    if (parent != null)
      parent.dispose();
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    observableListSelectionModel.discard();
  }

  /*
  Implementation of Scrollable, to disable horizontal scrolling -> make sure that the arrows drawn in the branchcellRenderer are always shown on top
   */

  @Override
  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 16;
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 16;
  }

  @Override
  public boolean getScrollableTracksViewportWidth()
  {
    return true;
  }

  @Override
  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }

  /*
  End Scrollable methods
   */

  /**
   * add a {@link MouseAdapter} to the second popup menu at the clicked branch.
   */
  private abstract class _BranchMouseListener extends MouseAdapter
  {
    @Override
    public void mousePressed(MouseEvent pE)
    {
      JList<IBranch> selectedBranchList = (JList<IBranch>) pE.getSource();

      //clear the last selection
      branchLists.forEach(listComponent -> {
                            if (!listComponent.equals(selectedBranchList))
                            {
                              listComponent.clearSelection();
                            }
                          }
      );

      Observable<Optional<List<IBranch>>> selectedBranches = Observable.just(Optional.of(selectedBranchList.getSelectedValuesList()));
      Observable<Optional<IBranch>> selectedBranch = selectedBranches.map(pOpt -> pOpt.map(pList -> {
        if (pList.isEmpty())
          return null;
        return pList.get(0);
      }));

      Action checkoutAction = actionProvider.getCheckoutAction(observableOptRepo, selectedBranch);
      Action showAllCommitsAction = actionProvider.getShowAllCommitsForBranchAction(observableOptRepo, selectedBranches);
      Action mergeAction = getMergeAction(selectedBranch);
      Action deleteBranchAction = actionProvider.getDeleteBranchAction(observableOptRepo, selectedBranch);

      JPopupMenu innerPopup = new JPopupMenu();
      innerPopup.add(checkoutAction);
      innerPopup.add(showAllCommitsAction);
      if (mergeAction != null)
        innerPopup.add(mergeAction);
      innerPopup.addSeparator();
      innerPopup.add(deleteBranchAction);

      Point location = _calculateInnerPopupPosition(selectedBranchList);
      innerPopup.show(selectedBranchList, location.x - innerPopup.getPreferredSize().width, location.y);
    }

    abstract Action getMergeAction(Observable<Optional<IBranch>> pSelectedBranch);

    private Point _calculateInnerPopupPosition(JList<IBranch> pBranchList)
    {
      Point pScr = pBranchList.getLocationOnScreen();
      SwingUtilities.convertPointFromScreen(pScr, pBranchList);
      Point index = pBranchList.indexToLocation(pBranchList.getSelectedIndex());
      pScr.y += index.y;
      JRootPane rp = SwingUtilities.getRootPane(pBranchList);
      Point rpScreen = rp.getLocationOnScreen();
      SwingUtilities.convertPointFromScreen(rpScreen, rp);
      Point point = SwingUtilities.convertPoint(rp, rp.getX(), rp.getY(), pBranchList);
      return new Point(point.x, pScr.y);
    }
  }

  /**
   * Subclass for a mouselistener for remote branches that has menu options specific for remote branches
   */
  private class _RemoteBranchMouseListener extends _BranchMouseListener
  {

    @Override
    Action getMergeAction(Observable<Optional<IBranch>> pSelectedBranch)
    {
      return actionProvider.getMergeRemoteAction(observableOptRepo, pSelectedBranch);
    }
  }

  /**
   * Subclass for a mouselistener for local branches that has menu options specific for local branches
   */
  private class _LocalBranchMouseListener extends _BranchMouseListener
  {

    @Override
    Action getMergeAction(Observable<Optional<IBranch>> pSelectedBranch)
    {
      return actionProvider.getMergeAction(observableOptRepo, pSelectedBranch);
    }
  }

  /**
   * A {@link MouseAdapter} who checks the entry and exit point of a {@link JList}/{@link JLabel} and set at the hover
   * mouse state the look and feel hover color
   */
  static class _HoverMouseListener extends MouseAdapter
  {
    Color hoverColor;
    Color labelSelectedForeground = new JList<>().getSelectionForeground();
    Color labelForeground = new JLabel().getForeground();

    _HoverMouseListener()
    {
      hoverColor = ColorPicker.LIST_SELECTION_BACKGROUND;
    }

    @Override
    public void mouseExited(MouseEvent pE)
    {
      Object source = pE.getSource();
      if (source instanceof JList)
      {
        JList<?> list = (JList<?>) source;
        list.clearSelection();
      }
      if (source instanceof JLabel)
      {
        JLabel label = (JLabel) source;
        label.setOpaque(false);
        label.setForeground(labelForeground);
        label.repaint();
      }
    }

    @Override
    public void mouseMoved(MouseEvent pE)
    {
      Object source = pE.getSource();
      if (source instanceof JList)
      {
        JList<?> list = (JList<?>) source;
        list.clearSelection();
        int i = list.locationToIndex(pE.getPoint());
        list.setSelectedIndex(i);
      }
      if (source instanceof JLabel)
      {
        JLabel label = (JLabel) source;
        label.setOpaque(true);
        label.setBackground(hoverColor);
        label.setForeground(labelSelectedForeground);
        label.repaint();
      }
    }
  }

  /**
   * Model that updates itself via an observable and fires the changes to its listeners if changes occur
   */
  private static class ObservingBranchListModel extends AbstractListModel<IBranch> implements Disposable
  {

    private final Disposable disposable;
    private final List<IBranch> branches = new ArrayList<>();

    public ObservingBranchListModel(Observable<List<IBranch>> pBranchObservable)
    {
      disposable = pBranchObservable.subscribe(pIBranches -> {
        int numElements = branches.size();
        branches.clear();
        fireIntervalRemoved(this, 0, numElements);
        branches.addAll(pIBranches);
        branches.sort(Comparator.comparing(IBranch::getSimpleName));
        fireIntervalAdded(this, 0, branches.size());
      });
    }

    @Override
    public int getSize()
    {
      return branches.size();
    }

    @Override
    public IBranch getElementAt(int index)
    {
      return branches.get(index);
    }

    @Override
    public void dispose()
    {
      disposable.dispose();
    }

    @Override
    public boolean isDisposed()
    {
      return disposable.isDisposed();
    }

  }

  /**
   * Logic for a label whose text is set enabled or disabled according to the state of the Repository contained in the passed Observable
   */
  private class NewBranchLabelController extends ObservingLabelController<IRepository>
  {

    protected NewBranchLabelController(Observable<Optional<IRepository>> pObservable)
    {
      super("New Branch", pObservable);
      label.setIcon(new SwingIconLoaderImpl().getIcon(NEW_BRANCH_ICON));
      label.setBorder(new EmptyBorder(3, 2, 3, 0));
      label.addMouseMotionListener(new _HoverMouseListener());
      label.addMouseListener(new _HoverMouseListener());
      label.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseReleased(MouseEvent pE)
        {
          Action newBranchAction = actionProvider.getNewBranchAction(observableOptRepo);
          newBranchAction.actionPerformed(new ActionEvent(pE.getSource(), ActionEvent.ACTION_PERFORMED, ""));
          closeWindow();
        }
      });
    }

    @Override
    protected void updateLabel(@Nullable IRepository pNewValue)
    {
      SwingUtilities.invokeLater(() -> label.setEnabled(pNewValue != null));
    }
  }

}
