package de.adito.git.nbm;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.popup.PopupWindow;
import de.adito.git.gui.window.content.IWindowContentProvider;
import de.adito.git.gui.window.content.StatusLineWindowContent;
import de.adito.git.nbm.util.EditorObservable;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.Observable;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Create a popup for all branches in one repository in the status line.
 *
 * @author a.arnold, 05.11.2018
 */
@SuppressWarnings("unused") //NetBeans service
@ServiceProvider(service = StatusLineElementProvider.class)
public class StatusLineElementImpl implements StatusLineElementProvider {
    private IWindowContentProvider windowContentProvider = IGitConstants.INJECTOR.getInstance(IWindowContentProvider.class);
    private IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    private JLabel label = new JLabel("not initialized...");
    private Observable<Optional<IRepository>> repository;
    private PopupWindow popupWindow;

    public StatusLineElementImpl() {
        EventQueue.invokeLater(() -> {
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (repository == null) {
                        return;
                    }
                    _initPopup();
                    popupWindow.setVisible(true);
                    popupWindow.setLocation(e.getLocationOnScreen().x - popupWindow.getWidth(), e.getLocationOnScreen().y - popupWindow.getHeight());
                }
            });
            _setStatusLineName();
        });
    }

    /**
     * Set the name in the status line to the actual branch name
     */
    private void _setStatusLineName() {
        //noinspection ResultOfMethodCallIgnored StatusLineElement only exists once
        EditorObservable.create()
                .flatMap(pEditorOpt -> pEditorOpt
                        .map(pTopComponent -> {
                            repository = RepositoryUtility.findOneRepositoryFromNode(pTopComponent.getActivatedNodes());
                            return repository;
                        })
                        .orElse(Observable.just(Optional.empty())))
                .flatMap(pRepo -> (Observable<Optional<IBranch>>) (pRepo.isPresent() ? pRepo.get().getCurrentBranch() : Observable.just(Optional.empty())))
                .subscribe(pBranch -> label.setText(pBranch.isPresent() ? pBranch.get().getSimpleName() : "<no branch>"));
    }

    private void _initPopup() {
        StatusLineWindowContent statusLineWindowContent = new StatusLineWindowContent(actionProvider, repository);
        popupWindow = new PopupWindow(WindowManager.getDefault().getMainWindow(), "Git Branches", statusLineWindowContent);
        statusLineWindowContent.setParentWindow(popupWindow);
    }

    @Override
    public Component getStatusLineElement() {
        _setStatusLineName();
        return label;
    }
}
