package de.adito.git.nbm.Guice;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IRepositoryProvider {
    Observable<IRepository> getRepositoryImpl();
}
