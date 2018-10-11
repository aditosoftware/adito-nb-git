package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.tableModels.StatusTableModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author m.kaspera 11.10.2018
 */
public class ExcludeAction extends AbstractTableAction {

    private JTable statusTable;
    private IRepository repository;
    private IFileStatus status;

    public ExcludeAction(JTable pStatusTable, IRepository pRepository, IFileStatus pStatus) {
        super("Exclude");
        statusTable = pStatusTable;
        repository = pRepository;
        status = pStatus;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<File> selectedFiles = new ArrayList<>();
        for (int rowNum : rows) {
            selectedFiles.add(status.getUncommitted().get(rowNum).getFile());
        }
        try {
            repository.exclude(selectedFiles);
            // refresh table view
            status = repository.status();
            ((StatusTableModel) statusTable.getModel()).statusChanged(status);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Only enabled if all selected files are not in the index yet, i.e. have status
     * NEW, MODIFY or MISSING
     */
    @Override
    protected boolean filter(int[] rows) {
        return Arrays.stream(rows)
                .allMatch(row ->
                        statusTable.getValueAt(row, 2).equals(EChangeType.NEW)
                                || statusTable.getValueAt(row, 2).equals(EChangeType.MODIFY)
                                || statusTable.getValueAt(row, 2).equals(EChangeType.MISSING));
    }
}
