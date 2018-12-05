package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Panel for the NewBranchAction
 *
 * @author A.Arnold 17.10.2018
 */

class NewBranchDialog extends JPanel
{

  private final Runnable enableOk;
  private final Runnable disableOk;
  private @NotNull List<IBranch> branchList;
  private JTextField textField = new JTextField();
  private JCheckBox checkbox = new JCheckBox();
  private HashSet<String> branchMap = new HashSet<>();

  /**
   * @param pRepository The repository where the new branch has to be
   */
  @Inject
  public NewBranchDialog(@Assisted Observable<Optional<IRepository>> pRepository,
                         @Assisted("enable") Runnable pEnableOk, @Assisted("disable") Runnable pDisableOk) throws AditoGitException
  {
    branchList = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"))
        .getBranches().blockingFirst().orElse(Collections.emptyList());
    enableOk = pEnableOk;
    disableOk = pDisableOk;
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
    }
    textField.getDocument().addDocumentListener(new _DocumentListener(branchMap, labelAlreadyExists));
  }

  /**
   * Listen to document changes.
   * Disable the OK button if the name is equal to an other branch name.
   * Disable the OK button if the name is empty.
   */
  private class _DocumentListener implements DocumentListener
  {
    private HashSet<String> branchMap;
    private JLabel alreadyExists;

    private _DocumentListener(HashSet<String> pBranchMap, JLabel pAlreadyExists)
    {
      branchMap = pBranchMap;
      alreadyExists = pAlreadyExists;
    }

    @Override
    public void insertUpdate(DocumentEvent pEvent)
    {
      try
      {
        _checkingUpdate(pEvent, branchMap, alreadyExists);
      }
      catch (BadLocationException e1)
      {
        throw new RuntimeException(e1);
      }
    }

    @Override
    public void removeUpdate(DocumentEvent pEvent)
    {
      try
      {
        _checkingUpdate(pEvent, branchMap, alreadyExists);
      }
      catch (BadLocationException e1)
      {
        throw new RuntimeException(e1);
      }
    }

    @Override
    public void changedUpdate(DocumentEvent pEvent)
    {
      try
      {
        _checkingUpdate(pEvent, branchMap, alreadyExists);
      }
      catch (BadLocationException e1)
      {
        throw new RuntimeException(e1);
      }
    }

    private void _checkingUpdate(DocumentEvent pEvent, HashSet<String> pBranchMap, JLabel pAlreadyExists) throws BadLocationException
    {
      if (pBranchMap.contains(pEvent.getDocument().getText(0, pEvent.getDocument().getLength())))
      {
        disableOk.run();
        pAlreadyExists.setVisible(true);
      }
      else if (pEvent.getDocument().getLength() == 0)
      {
        disableOk.run();
        pAlreadyExists.setVisible(false);
      }
      else
      {
        enableOk.run();
        pAlreadyExists.setVisible(false);
      }
    }
  }

  String getBranchName()
  {
    return textField.getText();
  }

  public boolean isCheckoutValid()
  {
    return checkbox.isSelected();
  }

}
