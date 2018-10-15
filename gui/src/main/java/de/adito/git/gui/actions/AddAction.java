package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Action for adding files to staging
 *
 * @author m.kaspera 11.10.2018
 */
public class AddAction extends AbstractTableAction {

    private JTable statusTable;
    private IRepository repository;

    public AddAction(JTable pStatusTable, IRepository pRepository) {
        super("Add");
        statusTable = pStatusTable;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<File> selectedFiles = new ArrayList<>();
        for (int rowNum : rows) {
            selectedFiles.add(new File((String)statusTable.getValueAt(rowNum, 1)));
        }
        try {
            repository.add(selectedFiles);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Not enabled if file is already in index (i.e. has status
     * CHANGED, ADD or DELETE
     *
     * @param rowNumbers the rows selected by the user
     */
    @Override
    public void setRows(int[] rowNumbers) {
        rows = rowNumbers;
        if (Arrays.stream(rowNumbers)
                .anyMatch(row ->
                        statusTable.getValueAt(row, 2).equals(EChangeType.CHANGED)
                                || statusTable.getValueAt(row, 2).equals(EChangeType.ADD)
                                || statusTable.getValueAt(row, 2).equals(EChangeType.DELETE))) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }

    @Override
    protected boolean filter(int[] rows) {
        return Arrays.stream(rows)
                .anyMatch(row ->
                        statusTable.getValueAt(row, 2).equals(EChangeType.CHANGED)
                                || statusTable.getValueAt(row, 2).equals(EChangeType.ADD)
                                || statusTable.getValueAt(row, 2).equals(EChangeType.DELETE));
    }
}
