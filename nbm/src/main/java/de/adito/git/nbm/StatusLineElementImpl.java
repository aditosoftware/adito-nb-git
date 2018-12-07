package de.adito.git.nbm;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.popup.PopupWindow;
import de.adito.git.gui.window.content.IWindowContentProvider;
import de.adito.git.nbm.util.EditorObservable;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Create a popup for all branches in one observableOptRepo in the status line.
 *
 * @author a.arnold, 05.11.2018
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class StatusLineElementImpl implements StatusLineElementProvider, IDiscardable {
    private IWindowContentProvider windowContentProvider = IGitConstants.INJECTOR.getInstance(IWindowContentProvider.class);
    private JLabel label = new JLabel("not initialized...");
    private PopupWindow popupWindow;
    private Disposable disposable;

    public StatusLineElementImpl() {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                _initPopup();
                popupWindow.setVisible(true);
                popupWindow.setLocation(e.getLocationOnScreen().x - popupWindow.getWidth(), e.getLocationOnScreen().y - popupWindow.getHeight());
            }
        });
        EventQueue.invokeLater(this::_setStatusLineName);
    }

    /**
     * Set the name in the status line to the actual branch name
     */
    private void _setStatusLineName() {
        //noinspection ResultOfMethodCallIgnored StatusLineElement only exists once
        disposable = _observeRepository().flatMap(pRepo -> pRepo.isPresent() ? pRepo.get().getCurrentBranch() : Observable.just(Optional.<IBranch>empty()))
                .subscribe(pBranch -> label.setText(pBranch.map(IBranch::getSimpleName).orElse("<no branch>")));
    }

    private Observable<Optional<IRepository>> _observeRepository() {
        return EditorObservable.create()
                .flatMap(pEditorOpt -> pEditorOpt
                        .map(pTopComponent -> RepositoryUtility.findOneRepositoryFromNode(pTopComponent.getActivatedNodes()))
                        .orElse(Observable.just(Optional.empty())));
    }

    private void _initPopup() {
        JComponent statusLineWindowContent = windowContentProvider.createStatusLineWindowContent(_observeRepository());
        popupWindow = new PopupWindow(WindowManager.getDefault().getMainWindow(), "Git Branches", statusLineWindowContent);
        statusLineWindowContent.putClientProperty("parent", popupWindow);
    }

    @Override
    public Component getStatusLineElement() {
        return label;
    }

    @Override
    public void discard() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }
}