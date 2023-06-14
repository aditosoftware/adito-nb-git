package de.adito.git.gui.window;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import java.util.Optional;

/**
 * An interface provider class for the windows
 *
 * @author a.arnold, 31.10.2018
 */
public interface IWindowProvider
{

  void showCommitHistoryWindow(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull ICommitFilter pCommitFilter);

  void showStatusWindow(@NonNull Observable<Optional<IRepository>> pRepository);

}
