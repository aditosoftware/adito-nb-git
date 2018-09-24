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

    /**
     *
     * @param pLocation Location of the local git repository
     * @return Repository of the provided location
     * @throws IOException the repository could not be accessed to configure the rest of
     *             the builder's parameters.
     */
    public static Repository get(String pLocation) throws IOException {
        return FileRepositoryBuilder.create(new File(pLocation));
    }

    //TODO: add method where the information about the loocation of the repository file is gotten from the netbeans options

}
