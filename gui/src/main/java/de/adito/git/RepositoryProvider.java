package de.adito.git;

import com.google.inject.Inject;
import de.adito.git.Guice.IRepositoryFactory;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRepositoryDescription;

/**
 * @author m.kaspera 27.09.2018
 */
class RepositoryProvider {

    private IRepository git;

    @Inject
    RepositoryProvider(IRepositoryFactory pGitFactory, IRepositoryDescription description){
        git = pGitFactory.create(description.getPath());
    }

    IRepository getGit(){
        return git;
    }

}
