package de.adito.git.gui.window;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import io.reactivex.Observable;

import java.io.File;

/**
 * An interface provider class for the windows
 *
 * @author a.arnold, 31.10.2018
 */
public interface IWindowProvider {

    void showBranchListWindow(Observable<IRepository> pRepository);

    void showCommitHistoryWindow(Observable<IRepository> pRepository, IBranch pBranch);

    void showCommitHistoryWindow(Observable<IRepository> pRepository, File pFile);

    void showStatusWindow(Observable<IRepository> pRepository);

}
