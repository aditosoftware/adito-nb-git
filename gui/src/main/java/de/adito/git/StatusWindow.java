package de.adito.git;

import de.adito.git.api.IFileStatus;

import javax.swing.*;
import java.awt.*;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusWindow extends JPanel {

    private IFileStatus status;

    public StatusWindow(IFileStatus pStatus){
        status = pStatus;
        _initGui();
    }

    private void _initGui(){
        setLayout(new BorderLayout());
        JTable statusTable = new JTable(new StatusTableModel(status));
        statusTable.getColumnModel().getColumn(0).setMinWidth(150);
        statusTable.getColumnModel().getColumn(1).setMinWidth(250);
        statusTable.getColumnModel().getColumn(2).setMinWidth(50);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        add(statusTable, BorderLayout.CENTER);
    }

}
