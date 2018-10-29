package de.adito.git.nbm.Guice;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.gui.guice.IRepositoryFactory;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Gets the Repository object injected and forms the central place to get it from
 *
 * @author m.kaspera 27.09.2018
 */
public class RepositoryProvider implements IRepositoryProvider {

    private final Subject<IRepository> git;
    private IRepositoryFactory gitFactory;

    @Inject
    RepositoryProvider(IRepositoryFactory pGitFactory, @Assisted IRepositoryDescription pDescription){
        gitFactory = pGitFactory;
        git = BehaviorSubject.createDefault(pGitFactory.create(pDescription));
    }

    /**
     *
     * @return IRepository implementation of the IRepository interface
     */
    @Override
    public Observable<IRepository> getRepositoryImpl(){
        return git;
    }

    public void setRepositoryDescription(IRepositoryDescription pDescription){
        git.onNext(gitFactory.create(pDescription));
    }

}
