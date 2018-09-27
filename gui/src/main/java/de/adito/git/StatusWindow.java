package de.adito.git;

import de.adito.git.api.IFileStatus;

import javax.swing.*;

/**
 * @author m.kaspera 27.09.2018
 */
public class StatusWindow extends JPanel {

    private IFileStatus status;

    public StatusWindow(IFileStatus pStatus){
        status = pStatus;
        _initGui();
    }

    private void _initGui(){
        add(new JTable(new StatusTableModel(status)));
    }

}
