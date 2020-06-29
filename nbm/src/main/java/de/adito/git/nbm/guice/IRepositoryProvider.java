package de.adito.git.nbm.guice;

import de.adito.git.api.IRepository;
import io.reactivex.rxjava3.core.Observable;

import java.util.Optional;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IRepositoryProvider
{
  Observable<Optional<IRepository>> getRepositoryImpl();
}
