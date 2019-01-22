package de.adito.git.nbm.wizard;

import de.adito.git.api.data.IBranch;
import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author a.arnold, 09.01.2019
 */
public class CloneWizardVisualPanel2 extends JPanel
{
  private final JList<IBranch> branchList;

  CloneWizardVisualPanel2()
  {
    JLabel checkoutLabel = new JLabel(AditoRepositoryCloneWizard.getMessage(this, "Label.BranchCheckout"));
    JLabel checkoutHintLabel = new JLabel(AditoRepositoryCloneWizard.getMessage(this, "Label.BranchCheckoutHint"));
    branchList = new JList<>();
    branchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, pref, gap};
    double[] rows = {gap,
                     pref,
                     gap,
                     pref,
                     gap,
                     pref,
                     gap};

    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, checkoutLabel);
    tlu.add(1, 3, checkoutHintLabel);
    tlu.add(1, 5, _chooseSingleBranch());
  }

  private JScrollPane _chooseSingleBranch()
  {
    return new JScrollPane(branchList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  @Override
  public String getName()
  {
    return AditoRepositoryCloneWizard.getMessage(this, "Title.Name.Panel2");
  }

  void setBranchArray(@Nullable IBranch[] pBranchArray)
  {
    branchList.setListData(pBranchArray);
  }

  @Nullable
  IBranch getSelectedBranch()
  {
    if (branchList.getSelectedValue() != null)
      return branchList.getSelectedValue();
    return null;
  }
}
