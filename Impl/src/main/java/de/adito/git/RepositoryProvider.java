package de.adito.git;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Central Object from which to get the repository from
 *
 * @author m.kaspera 24.09.2018
 */
public class RepositoryProvider {

    private static Repository repository;

    /**
     *
     * @param pLocation Location of the local git repository
     * @return Repository of the provided location
     * @throws IOException the repository could not be accessed to configure the rest of
     *             the builder's parameters.
     */
    public static Repository get(String pLocation) throws IOException {
        if(repository == null || (repository.getDirectory() != null && !pLocation.equals(repository.getDirectory().getAbsolutePath()))){
            repository = FileRepositoryBuilder.create(new File(pLocation));
        }
        return repository;
    }

    /**
     *
     * @return Repository that was already set or is retrieved from the location set in the netbeans options
     */
    public static Repository get() {
        //TODO: if repository is not set get the information about the loocation of the repository file from the netbeans options
        return repository;
    }


}
