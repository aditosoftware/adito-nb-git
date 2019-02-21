package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.dialogs.panels.basediffpanel.MergePanel;
import de.adito.git.gui.icon.IIconLoader;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;

import static de.adito.git.gui.Constants.ACCEPT_CHANGE_THEIRS_ICON;
import static de.adito.git.gui.Constants.ACCEPT_CHANGE_YOURS_ICON;
import static de.adito.git.gui.Constants.DISCARD_CHANGE_ICON;

/**
 * Dialog/Panel for displaying the merge-conflicts
 * Also offers the possibility of accepting changes from both the
 * merge-base/current side and the side that is being merged
 *
 * @author m.kaspera 22.10.2018
 */
class MergeConflictResolutionDialog extends AditoBaseDialog<Object>
{

  private final IMergeDiff mergeDiff;
  private final MergePanel mergePanel;

  @Inject
  MergeConflictResolutionDialog(IIconLoader pIconLoader, IEditorKitProvider pEditorKitProvider, @Assisted IMergeDiff pMergeDiff)
  {
    mergeDiff = pMergeDiff;
    ImageIcon acceptYoursIcon = pIconLoader.getIcon(ACCEPT_CHANGE_YOURS_ICON);
    ImageIcon acceptTheirsIcon = pIconLoader.getIcon(ACCEPT_CHANGE_THEIRS_ICON);
    ImageIcon discardIcon = pIconLoader.getIcon(DISCARD_CHANGE_ICON);
    mergePanel = new MergePanel(mergeDiff, acceptYoursIcon, acceptTheirsIcon, discardIcon, pEditorKitProvider);
    _initGui();
  }

  private void _initGui()
  {
    // create a panel in a scrollPane for each of the textPanes
    setLayout(new BorderLayout());

    JPanel diffPanel = new JPanel(new BorderLayout());
    diffPanel.add(mergePanel, BorderLayout.CENTER);
    diffPanel.add(_initAcceptAllPanel(), BorderLayout.NORTH);
    diffPanel.setPreferredSize(new Dimension(1600, 900));
    add(diffPanel, BorderLayout.CENTER);
  }

  private JPanel _initAcceptAllPanel()
  {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(_initAcceptAllButtonPanel("accept remaining YOURS changes",
                                           "discard all remaining YOURS changes",
                                           IMergeDiff.CONFLICT_SIDE.YOURS), BorderLayout.WEST);
    topPanel.add(_initAcceptAllButtonPanel("accept remaining THEIRS changes",
                                           "discard all remaining THEIRS changes",
                                           IMergeDiff.CONFLICT_SIDE.THEIRS), BorderLayout.EAST);
    return topPanel;
  }

  private JPanel _initAcceptAllButtonPanel(String pAcceptButtonText, String pDiscardButtonText, IMergeDiff.CONFLICT_SIDE pConflictSide)
  {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
    JButton acceptChangesButton = new JButton(pAcceptButtonText);
    JButton discardChangesButton = new JButton(pDiscardButtonText);
    acceptChangesButton.addActionListener(e -> _acceptAllChanges(pConflictSide));
    discardChangesButton.addActionListener(e -> _discardAllChanges(pConflictSide));
    buttonPanel.add(acceptChangesButton);
    buttonPanel.add(discardChangesButton);
    return buttonPanel;
  }

  /**
   * @param pConflictSide CONFLICT_SIDE whose changes should be accepted
   */
  private void _acceptAllChanges(IMergeDiff.CONFLICT_SIDE pConflictSide)
  {
    for (IFileChangeChunk changeChunk : mergeDiff.getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue())
    {
      if (changeChunk.getChangeType() != EChangeType.SAME)
      {
        mergeDiff.acceptChunk(changeChunk, pConflictSide);
      }
    }
  }

  /**
   * @param pConflictSide CONFLICT_SIDE whose changes should be discarded
   */
  private void _discardAllChanges(IMergeDiff.CONFLICT_SIDE pConflictSide)
  {
    for (IFileChangeChunk changeChunk : mergeDiff.getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue())
    {
      if (changeChunk.getChangeType() != EChangeType.SAME)
      {
        mergeDiff.discardChange(changeChunk, pConflictSide);
      }
    }
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Object getInformation()
  {
    return null;
  }
}
