package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Paths;
import java.util.*;

/**
 * Panel for the NewBranchAction
 *
 * @author A.Arnold 17.10.2018
 */

class NewBranchDialog extends AditoBaseDialog<Boolean>
{

  private final @NotNull List<IBranch> branchList;
  private final JTextField textField = new JTextField();
  private final JCheckBox checkbox = new JCheckBox();
  private final HashSet<String> branchMap = new HashSet<>();
  private IDialogDisplayer.IDescriptor isValidDescriptor;

  /**
   * @param pRepository The repository where the new branch has to be
   */
  @Inject
  public NewBranchDialog(@Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                         @Assisted Observable<Optional<IRepository>> pRepository)
  {
    isValidDescriptor = pIsValidDescriptor;
    isValidDescriptor.setValid(false);
    branchList = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"))
        .getBranches().blockingFirst().orElse(Collections.emptyList());
    checkbox.setSelected(true);
    textField.setPreferredSize(new Dimension(200, 24));
    _initGui();
  }

  /**
   * initialise GUI elements
   */
  private void _initGui()
  {
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

    labelAlreadyExists.setForeground(Color.red);
    labelAlreadyExists.setVisible(false);
    setVisible(true);
    for (IBranch branch : branchList)
    {
      branchMap.add(branch.getSimpleName());
      if (Paths.get(branch.getSimpleName()).getParent() != null)
        branchMap.add(Paths.get(branch.getSimpleName()).getFileName().toString());
    }
    textField.getDocument().addDocumentListener(new _DocumentListener(branchMap, labelAlreadyExists));
  }

  @Override
  public String getMessage()
  {
    return textField.getText();
  }

  @Override
  public Boolean getInformation()
  {
    return checkbox.isSelected();
  }

  /**
   * Listen to document changes.
   * Disable the OK button if the name is equal to an other branch name.
   * Disable the OK button if the name is empty.
   */
  private class _DocumentListener implements DocumentListener
  {
    private HashSet<String> branchMap;
    private JLabel alreadyExistsLabel;

    private _DocumentListener(HashSet<String> pBranchMap, JLabel pAlreadyExists)
    {
      branchMap = pBranchMap;
      alreadyExistsLabel = pAlreadyExists;
    }

    @Override
    public void insertUpdate(DocumentEvent pEvent)
    {
      _checkingUpdate(pEvent, branchMap, alreadyExistsLabel);
    }

    @Override
    public void removeUpdate(DocumentEvent pEvent)
    {
      _checkingUpdate(pEvent, branchMap, alreadyExistsLabel);
    }

    @Override
    public void changedUpdate(DocumentEvent pEvent)
    {
      _checkingUpdate(pEvent, branchMap, alreadyExistsLabel);

    }

    private void _checkingUpdate(DocumentEvent pEvent, HashSet<String> pBranchMap, JLabel pAlreadyExists)
    {
      boolean alreadyExists;
      try
      {
        alreadyExists = pBranchMap.contains(pEvent.getDocument().getText(0, pEvent.getDocument().getLength()));
        pAlreadyExists.setVisible(alreadyExists);
        isValidDescriptor.setValid(!alreadyExists && pEvent.getDocument().getLength() > 0);
      }
      catch (BadLocationException pE)
      {
        isValidDescriptor.setValid(false);
      }
    }
  }

}
