package de.adito.git.gui.window;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An interface provider class for the windows
 *
 * @author a.arnold, 31.10.2018
 */
public interface IWindowProvider
{

  void showBranchListWindow(@NotNull Observable<Optional<IRepository>> pRepository);

  void showCommitHistoryWindow(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull ICommitFilter pCommitFilter);

  void showStatusWindow(@NotNull Observable<Optional<IRepository>> pRepository);

}
