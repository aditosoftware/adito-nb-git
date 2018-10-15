package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.DiffDialog;
import de.adito.git.gui.IDialogDisplayer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author m.kaspera 12.10.2018
 */
public class DiffAction extends AbstractTableAction {

    private JTable statusTable;
    private IRepository repository;
    private IDialogDisplayer dialogDisplayer;

    public DiffAction(IDialogDisplayer pDialogDisplayer, JTable pStatusTable, IRepository pRepository){
        super("Show Diff");
        statusTable = pStatusTable;
        repository = pRepository;
        dialogDisplayer = pDialogDisplayer;
    }

    @Override
    protected boolean filter(int[] rows) {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<File> selectedFiles = new ArrayList<>();
        for (int rowNum : rows) {
            selectedFiles.add(new File((String)statusTable.getValueAt(rowNum, 1)));
        }
        List<IFileDiff> fileDiffs;
        try {
            fileDiffs = repository.diff(selectedFiles);
            DiffDialog diffDialog = new DiffDialog(fileDiffs);
            dialogDisplayer.showDialog(diffDialog, "Diff for files", true);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
