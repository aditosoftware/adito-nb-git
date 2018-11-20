package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.TableLayoutUtil;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.popup.PopupWindow;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Creates the content of the branch menu.
 *
 * @author a.arnold, 08.11.2018
 */
public class StatusLineWindowContent extends JPanel {
    private final IActionProvider actionProvider;
    private final Observable<IRepository> repository;
    private final Observable<List<IBranch>> branchObservable;
    private List<JList<IBranch>> branchLists = new ArrayList<>();
    private PopupWindow popupWindow;

    @Inject
    public StatusLineWindowContent(IActionProvider pProvider, @Assisted Observable<IRepository> pRepository) {
        actionProvider = pProvider;
        repository = pRepository;
        branchObservable = repository.flatMap(IRepository::getBranches);
        _initGUI();
    }

    /**
     * A method to initialize the GUI
     */
    private void _initGUI() {
        double pref = TableLayout.PREFERRED;
        JLabel labelLocalBranch = new JLabel("Local Branches");
        JLabel labelRemoteBranch = new JLabel("Remote Branches");

        //boldFont for headings
        Font font = labelLocalBranch.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        labelLocalBranch.setFont(boldFont);
        labelRemoteBranch.setFont(boldFont);

        //room between the components
        final double gap = 12;

        double[] cols = {16, pref};
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
        tlu.add(0, 0, 1, 0, _createNewBranch());
        tlu.add(0, 2, 1, 2, labelLocalBranch);
        tlu.add(1, 3, _createListLocalBranches());
        tlu.add(0, 5, 1, 5, labelRemoteBranch);
        tlu.add(1, 6, _createListRemoteBranches());

    }

    private JComponent _createNewBranch() {
        JLabel label = new JLabel("+ New Branch...");
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
     * Create and fill a list of local branches
     *
     * @return the list of local branches
     */
    private JComponent _createListLocalBranches() {
        JList<IBranch> localBranches = new JList<>();
        localBranches.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        localBranches.addMouseListener(new _BranchMouseListener(localBranches));
        localBranches.setSelectionModel(new ObservableListSelectionModel(localBranches.getSelectionModel()));
        branchLists.add(localBranches);

        branchObservable.subscribe(pBranches -> localBranches.setListData(pBranches
                .stream()
                .filter(pBranch -> pBranch.getType().equals(EBranchType.LOCAL))
                .toArray(IBranch[]::new)));
        return localBranches;
    }

    /**
     * Create and fill a list of remote Branches
     *
     * @return the list of remote branches
     */
    private JComponent _createListRemoteBranches() {
        JList<IBranch> remoteBranches = new JList<>();
        remoteBranches.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        remoteBranches.addMouseListener(new _BranchMouseListener(remoteBranches));
        remoteBranches.setSelectionModel(new ObservableListSelectionModel(remoteBranches.getSelectionModel()));
        branchLists.add(remoteBranches);

        branchObservable.subscribe(pBranches -> remoteBranches.setListData(pBranches
                .stream()
                .filter(pBranch -> pBranch.getType().equals(EBranchType.REMOTE))
                .toArray(IBranch[]::new)));
        return remoteBranches;
    }

    public void setParentWindow(PopupWindow pPopupWindow) {
        popupWindow = pPopupWindow;
    }

    private class _BranchMouseListener extends MouseAdapter {
        private final JList<IBranch> actualBranches;

        _BranchMouseListener(JList<IBranch> pActualBranches) {
            actualBranches = pActualBranches;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            //clear the last selection
            branchLists.forEach(listComponent -> {
                        if (!listComponent.equals(actualBranches)) {
                            listComponent.clearSelection();
                        }
                    }
            );

            Supplier<Integer> selectionIndexSupplier = () -> ((JList) e.getSource()).getSelectedIndex();

            // TODO: 19.11.2018 get y axis for cell positioning
//            Integer integer = selectionIndexSupplier.get();
//            integer = actualBranches.getComponent(integer).getParent().getY() + actualBranches.getComponent(integer).getY();

            Observable<Optional<IBranch>> selectedBranch = branchObservable.map(pBranches -> {
                if (pBranches == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(pBranches.get(selectionIndexSupplier.get()));
                }
            });


            Action checkoutAction = actionProvider.getCheckoutAction(repository, selectedBranch);
            Action showAllCommitsAction = actionProvider.getShowAllCommitsAction(repository, branchObservable.map(pBranch -> Collections.singletonList(pBranch.get(selectionIndexSupplier.get()))));
//            actionProvider.getMergeAction(repository, selectedBranch, repository.blockingFirst().getCurrentBranch())
            JPopupMenu innerPopup = new JPopupMenu();
            innerPopup.add(new _DisposeAction(checkoutAction));
            innerPopup.add(new _DisposeAction(showAllCommitsAction));
            innerPopup.show(actualBranches, -100, e.getY() + 15);
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
