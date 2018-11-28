package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.ColorPicker;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TableLayoutUtil;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.popup.PopupWindow;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Creates the content for the branch menu.
 *
 * @author a.arnold, 08.11.2018
 */
public class StatusLineWindowContent extends JPanel implements IDiscardable {
    private final IActionProvider actionProvider;
    private final Observable<Optional<IRepository>> repository;
    private final Observable<Optional<List<IBranch>>> branchObservable;
    private List<JList<IBranch>> branchLists = new ArrayList<>();
    private Disposable disposable;
    private PopupWindow popupWindow;

    @Inject
    public StatusLineWindowContent(IActionProvider pProvider, @Assisted Observable<Optional<IRepository>> pRepository) {
        actionProvider = pProvider;
        repository = pRepository;
        branchObservable = repository.flatMap(pRepo -> pRepo.orElseThrow(() -> new RuntimeException("no valid repository found")).getBranches());
        _initGUI();
    }

    /**
     * A method to initialize the GUI
     */
    private void _initGUI() {
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
        tlu.add(0, 2, _createLabel("Local Branch"));
        tlu.add(0, 3, _createListBranches(EBranchType.LOCAL));
        tlu.add(0, 5, _createLabel("Remote Branch"));
        tlu.add(0, 6, _createListBranches(EBranchType.REMOTE));
    }

    private JLabel _createLabel(String pString){
        JLabel jLabel = new JLabel(pString);
        jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
        return  jLabel;
    }

    /**
     * a click-able label to create a new Branch at the actual repository
     * @return return a label for the new branch action
     */
    private JLabel _createNewBranch() {
        JLabel label = new JLabel("+ New Branch...");
        label.addMouseMotionListener(new _HoverMouseListener());
        label.addMouseListener(new _HoverMouseListener());
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Action newBranchAction = actionProvider.getNewBranchAction(repository);
                newBranchAction.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, ""));
                popupWindow.dispose();
            }
        });
        return label;
    }

    /**
     * Create and fill a list of branches
     *
     * @param type the {@link EBranchType} of the branches
     * @return a branchList
     */
    private JComponent _createListBranches(EBranchType type) {
        _HoverMouseListener hoverMouseListener = new _HoverMouseListener();
        JList<IBranch> branchList = new JList<>();
        branchList.setSelectionModel(new ObservableListSelectionModel(branchList.getSelectionModel()));
        branchList.setCellRenderer(new BranchCellRenderer());
        branchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        branchList.addMouseListener(new _BranchMouseListener());
        branchList.addMouseListener(hoverMouseListener);
        branchList.addMouseMotionListener(hoverMouseListener);
        branchLists.add(branchList);

        disposable = branchObservable.subscribe(pBranches -> branchList.setListData(
                pBranches.orElse(Collections.emptyList())
                .stream()
                .filter(pBranch -> pBranch.getType().equals(type))
                .toArray(IBranch[]::new)));
        return branchList;
    }

    public void setParentWindow(PopupWindow pPopupWindow) {
        popupWindow = pPopupWindow;
    }

    @Override
    public void discard() {
        if (disposable != null)
            disposable.dispose();
    }

    /**
     * add a {@link MouseAdapter} to the second popup menu at the clicked branch.
     */
    private class _BranchMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            JList branchList = (JList) e.getSource();
            int selectedIndex = branchList.getSelectedIndex();

            //clear the last selection
            branchLists.forEach(listComponent -> {
                        if (!listComponent.equals(branchList)) {
                            listComponent.clearSelection();
                        }
                    }
            );

            //get selected branch as optional
            Observable<Optional<IBranch>> selectedBranch = branchObservable.map(pBranches -> {
                if (selectedIndex == -1) {
                    return Optional.empty();
                } else {
                    return Optional.of(pBranches.orElseThrow().get(selectedIndex));
                }
            });

            Action checkoutAction = actionProvider.getCheckoutAction(repository, selectedBranch);
            Action showAllCommitsAction = actionProvider.getShowAllCommitsForBranchAction(repository, branchObservable.map(pBranch ->
                    pBranch.map(iBranches -> Collections.singletonList(iBranches.get(selectedIndex)))));
            Action mergeAction = actionProvider.getMergeAction(repository, selectedBranch);

            JPopupMenu innerPopup = new JPopupMenu();
            innerPopup.add(new _DisposeAction(checkoutAction));
            innerPopup.add(new _DisposeAction(showAllCommitsAction));
            innerPopup.add(new _DisposeAction(mergeAction));

            Point location = _calculateInnerPopupPosition(branchList);
            innerPopup.show(branchList, location.x - innerPopup.getPreferredSize().width, location.y);
        }

        private Point _calculateInnerPopupPosition(JList pBranchList) {
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
    private class _HoverMouseListener extends MouseAdapter {
        Color hoverColor;

        _HoverMouseListener() {
            hoverColor = ColorPicker.LIST_SELECTION_BACKGROUND;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Object source = e.getSource();
            if (source instanceof JList) {
                JList list = (JList) source;
                list.clearSelection();
            }
            if (source instanceof JLabel) {
                JLabel label = (JLabel) source;
                label.setOpaque(false);
                label.repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Object source = e.getSource();
            if (source instanceof JList) {
                JList list = (JList) source;
                list.clearSelection();
                int i = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(i);
            }
            if (source instanceof JLabel) {
                JLabel label = (JLabel) source;
                label.setOpaque(true);
                label.setBackground(hoverColor);
                label.repaint();
            }
        }
    }

    private class _DisposeAction extends AbstractAction {
        private final Action outerAction;

        _DisposeAction(Action pOuterAction) {
            super(pOuterAction.getValue(Action.NAME).toString());
            outerAction = pOuterAction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            popupWindow.dispose();
            outerAction.actionPerformed(e);
        }
    }
}
