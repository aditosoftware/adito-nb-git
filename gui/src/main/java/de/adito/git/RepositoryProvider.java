package de.adito.git;

import com.google.inject.Inject;
import de.adito.git.Guice.*;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;

/**
 * Gets the Repository object injected and forms the central place to get it from
 *
 * @author m.kaspera 27.09.2018
 */
class RepositoryProvider {

    private IRepository git;

    @Inject
    RepositoryProvider(IRepositoryFactory pGitFactory, IRepositoryDescription description){
        git = pGitFactory.create(description.getPath());
    }

    /**
     *
     * @return IRepository implementation of the IRepository interface
     */
    IRepository getRepositoryImpl(){
        return git;
    }

}
