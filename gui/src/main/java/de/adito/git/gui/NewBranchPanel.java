package de.adito.git.gui;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.HashSet;
import java.util.List;

/**
 * the Panel for the NewBranchAction
 * @author A.Arnold 17.10.2018
 */
public class NewBranchPanel extends JPanel {

    private IDialogDisplayer dialogDisplayer;
    private @NotNull List<IBranch> branchList;
    private JTextField textField = new JTextField();
    private JCheckBox checkbox = new JCheckBox();
    private HashSet<String> branchMap = new HashSet<>();

    /**
     * @param pRepository      The repository where the new branch has to be
     * @param pDialogDisplayer the displayer where to provide functionality of giving an overlying framework
     */
    @Inject
    public NewBranchPanel(Observable<IRepository> pRepository, IDialogDisplayer pDialogDisplayer) throws Exception {
        branchList = pRepository.blockingFirst().getBranches().blockingFirst();
        dialogDisplayer = pDialogDisplayer;
        checkbox.setSelected(true);
        textField.setPreferredSize(new Dimension(200, 24));
        _initGui();
    }

    /**
     * initialise GUI elements
     */
    private void _initGui() {
        double fill = TableLayout.FILL;
        double pref = TableLayout.PREFERRED;
        JLabel labelCheckout = new JLabel("Checkout Branch:");
        JLabel labelBranchName = new JLabel("New Branch Name:");
        JLabel labelAlreadyExists = new JLabel("Branch already exists!");
        //room between the components
        final double gap = 8;

        double[] cols = {gap, pref, gap, pref, gap};
        double[] rows = {gap,
                pref,
                gap,
                pref,
                gap,
                pref,
                fill};

        setLayout(new TableLayout(cols, rows));
        TableLayoutUtil tlu = new TableLayoutUtil(this);

        tlu.add(1, 1, labelBranchName);
        tlu.add(3, 1, textField);
        tlu.add(1, 3, labelCheckout);
        tlu.add(3, 3, checkbox);
        tlu.add(1, 5, labelAlreadyExists);
        //setPreferredSize(new Dimension(500, 300));

        labelAlreadyExists.setForeground(Color.red);
        labelAlreadyExists.setVisible(false);
        setVisible(true);
        for (IBranch branch : branchList) {
            branchMap.add(branch.getSimpleName(branch));
        }
        textField.getDocument().addDocumentListener(new _DocumentListener(branchMap, labelAlreadyExists));
    }

    /**
     * Listen to document changes.
     * Disable the OK button if the name is equal to an other branch name.
     * Disable the OK button if the name is empty.
     */
    private class _DocumentListener implements DocumentListener {
        private HashSet<String> branchMap;
        private JLabel alreadyExists;

        private _DocumentListener(HashSet<String> pBranchMap, JLabel pAlreadyExists) {
            branchMap = pBranchMap;
            alreadyExists = pAlreadyExists;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            try {
                _checkingUpdate(e, branchMap, alreadyExists);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            try {
                _checkingUpdate(e, branchMap, alreadyExists);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            try {
                _checkingUpdate(e, branchMap, alreadyExists);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void _checkingUpdate(DocumentEvent e, HashSet<String> branchMap, JLabel alreadyExists) throws BadLocationException {
        if (branchMap.contains(e.getDocument().getText(0, e.getDocument().getLength()))) {
            dialogDisplayer.disableOKButton();
            alreadyExists.setVisible(true);
        } else if (e.getDocument().getLength() == 0) {
            dialogDisplayer.disableOKButton();
            alreadyExists.setVisible(false);
        } else {
            dialogDisplayer.enableOKButton();
            alreadyExists.setVisible(false);
        }
    }

    public String getBranchName() {
        return textField.getText();
    }

    public boolean getCheckoutValid() {
        return checkbox.isSelected();
    }

}
