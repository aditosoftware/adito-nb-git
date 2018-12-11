package de.adito.git.gui.window;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import io.reactivex.Observable;

import java.io.File;
import java.util.Optional;

/**
 * An interface provider class for the windows
 *
 * @author a.arnold, 31.10.2018
 */
public interface IWindowProvider
{

  void showBranchListWindow(Observable<Optional<IRepository>> pRepository);

  void showCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, IBranch pBranch);

  void showFileCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, File pFile);

  void showStatusWindow(Observable<Optional<IRepository>> pRepository);

}
