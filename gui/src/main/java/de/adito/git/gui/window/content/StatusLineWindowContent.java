package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.ColorPicker;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.TableLayoutUtil;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Creates the content for the branch menu.
 *
 * @author a.arnold, 08.11.2018
 */
class StatusLineWindowContent extends JPanel implements IDiscardable
{
  private final IActionProvider actionProvider;
  private final Observable<Optional<IRepository>> observableOptRepo;
  private final Observable<Optional<List<IBranch>>> observableBranches;
  private List<JList<IBranch>> branchLists = new ArrayList<>();
  private Disposable disposable;

  @Inject
  public StatusLineWindowContent(IActionProvider pProvider, @Assisted Observable<Optional<IRepository>> pObservableOptRepo)
  {
    actionProvider = pProvider;
    observableOptRepo = pObservableOptRepo;
    observableBranches = observableOptRepo
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
            .orElse(Observable.just(Optional.empty())));
    _initGUI();
  }

  /**
   * A method to initialize the GUI
   */
  private void _initGUI()
  {
    //room between the components
    final double gap = 8;
    double pref = TableLayout.PREFERRED;
    double[] cols = {TableLayout.FILL};
    double[] rows = {
        pref,
        gap,
        pref,
        pref,
        gap,
        pref,
        pref};

    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(0, 0, _createNewBranch());
    tlu.add(0, 2, _createLabel("Local Branches"));
    tlu.add(0, 3, _createListBranches(EBranchType.LOCAL));
    tlu.add(0, 5, _createLabel("Remote Branches"));
    tlu.add(0, 6, _createListBranches(EBranchType.REMOTE));
  }

  private JLabel _createLabel(String pString)
  {
    JLabel jLabel = new JLabel(pString);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    return jLabel;
  }

  /**
   * a click-able label to create a new Branch at the actual observableOptRepo
   *
   * @return return a label for the new branch action
   */
  private JLabel _createNewBranch()
  {
    JLabel label = new JLabel("+ New Branch...");
    if (observableOptRepo.blockingFirst(Optional.empty()).isPresent())
    {
      label.addMouseMotionListener(new _HoverMouseListener());
      label.addMouseListener(new _HoverMouseListener());
      label.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseReleased(MouseEvent pE)
        {
          Action newBranchAction = actionProvider.getNewBranchAction(observableOptRepo);
          newBranchAction.actionPerformed(new ActionEvent(pE.getSource(), ActionEvent.ACTION_PERFORMED, ""));
          JWindow parent = (JWindow) getClientProperty("parent");
          if (parent != null)
            parent.dispose();
        }
      });
    }
    else
    {
      label.setEnabled(false);
    }
    return label;
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
    branchList.setSelectionModel(new ObservableListSelectionModel(branchList.getSelectionModel()));
    branchList.setCellRenderer(new BranchCellRenderer());
    branchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    branchList.addMouseListener(new _BranchMouseListener());
    branchList.addMouseListener(hoverMouseListener);
    branchList.addMouseMotionListener(hoverMouseListener);
    branchLists.add(branchList);

    disposable = observableBranches.subscribe(pBranches -> branchList.setListData(
        pBranches.orElse(Collections.emptyList())
            .stream()
            .filter(pBranch -> pBranch.getType().equals(pType))
            .toArray(IBranch[]::new)));
    return branchList;
  }

  @Override
  public void discard()
  {
    if (disposable != null)
      disposable.dispose();
  }

  /**
   * add a {@link MouseAdapter} to the second popup menu at the clicked branch.
   */
  private class _BranchMouseListener extends MouseAdapter
  {
    @Override
    public void mousePressed(MouseEvent pE)
    {
      JList<IBranch> selectedBranchList = (JList) pE.getSource();

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
      Action mergeAction = actionProvider.getMergeAction(observableOptRepo, selectedBranch);
      Action deleteBranchAction = actionProvider.getDeleteBranchAction(observableOptRepo, selectedBranch);

      JPopupMenu innerPopup = new JPopupMenu();
      innerPopup.add(checkoutAction);
      innerPopup.add(showAllCommitsAction);
      innerPopup.add(mergeAction);
      innerPopup.addSeparator();
      innerPopup.add(deleteBranchAction);

      Point location = _calculateInnerPopupPosition(selectedBranchList);
      innerPopup.show(selectedBranchList, location.x - innerPopup.getPreferredSize().width, location.y);
    }

    private Point _calculateInnerPopupPosition(JList pBranchList)
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
   * A {@link MouseAdapter} who checks the entry and exit point of a {@link JList}/{@link JLabel} and set at the hover
   * mouse state the look and feel hover color
   */
  private class _HoverMouseListener extends MouseAdapter
  {
    Color hoverColor;

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
        JList list = (JList) source;
        list.clearSelection();
      }
      if (source instanceof JLabel)
      {
        JLabel label = (JLabel) source;
        label.setOpaque(false);
        label.repaint();
      }
    }

    @Override
    public void mouseMoved(MouseEvent pE)
    {
      Object source = pE.getSource();
      if (source instanceof JList)
      {
        JList list = (JList) source;
        list.clearSelection();
        int i = list.locationToIndex(pE.getPoint());
        list.setSelectedIndex(i);
      }
      if (source instanceof JLabel)
      {
        JLabel label = (JLabel) source;
        label.setOpaque(true);
        label.setBackground(hoverColor);
        label.repaint();
      }
    }
  }
}
