package de.adito.git.gui;

import de.adito.git.api.data.IRepositoryDescription;

/**
 * Mock repository implementation for testing purposes
 *
 * @author m.kaspera 27.09.2018
 */
public class MockRepositoryDescriptionImpl implements IRepositoryDescription {

    private String path;

    public MockRepositoryDescriptionImpl(String pPath){
        path = pPath;
    }

    @Override
    public String getPath() {
        //return "C:\\Entwicklungszweige\\adito-nb-git\\.git";
//        return ResourceBundle.getBundle(this.getClass().getPackage().getName() + ".Bundle").getString("VCS_Directory");
        //return System.getProperty("vcsDirectory");
        return path;
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
