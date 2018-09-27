package de.adito.git;

import de.adito.git.api.data.IRepositoryDescription;

/**
 * Mock repository implementation for testing purposes
 *
 * @author m.kaspera 27.09.2018
 */
public class MockRepositoryDescriptionImpl implements IRepositoryDescription {

    public MockRepositoryDescriptionImpl(){

    }

    @Override
    public String getPath() {
        return "C:\\Entwicklungszweige\\adito-nb-git\\.git";
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getSSHKeyLocation() {
        return null;
    }

    @Override
    public String getPassphrase() {
        return null;
    }
}
