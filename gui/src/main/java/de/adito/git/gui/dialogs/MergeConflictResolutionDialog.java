package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.*;
import de.adito.git.gui.*;
import de.adito.git.gui.dialogs.panels.basediffpanel.MergePanel;
import de.adito.git.gui.icon.IIconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static de.adito.git.gui.Constants.*;

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
    mergePanel = new MergePanel(pIconLoader, mergeDiff, acceptYoursIcon, acceptTheirsIcon, discardIcon, pEditorKitProvider);
    _initGui();
  }

  private void _initGui()
  {
    // create a panel in a scrollPane for each of the textPanes
    setLayout(new BorderLayout());

    JPanel diffPanel = new JPanel(new BorderLayout());
    diffPanel.add(mergePanel, BorderLayout.CENTER);
    diffPanel.add(_initToolbar(), BorderLayout.NORTH);
    diffPanel.setPreferredSize(new Dimension(1600, 900));
    add(diffPanel, BorderLayout.CENTER);
  }

  @NotNull
  private JToolBar _initToolbar()
  {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // Merge-Panel-Actions (Next | Previous)
    mergePanel.getActions().stream()
        .map(JButton::new)
        .forEach(toolbar::add);

    toolbar.addSeparator();

    // Our Actions
    toolbar.add(new JButton(new _AcceptAllActionImpl(IMergeDiff.CONFLICT_SIDE.YOURS)));
    toolbar.add(new JButton(new _AcceptAllActionImpl(IMergeDiff.CONFLICT_SIDE.THEIRS)));

    return toolbar;
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

  /**
   * AcceptAll-ActionImpl
   */
  private class _AcceptAllActionImpl extends AbstractAction
  {
    private final IMergeDiff.CONFLICT_SIDE conflictSide;

    public _AcceptAllActionImpl(@NotNull IMergeDiff.CONFLICT_SIDE pConflictSide)
    {
      super("", new ImageIcon(_AcceptAllActionImpl.class.getResource(pConflictSide == IMergeDiff.CONFLICT_SIDE.YOURS ?
                                                                         Constants.ACCEPT_ALL_LEFT : Constants.ACCEPT_ALL_RIGHT)));
      putValue(SHORT_DESCRIPTION, "accept remaining " + pConflictSide.name() + " changes");
      conflictSide = pConflictSide;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      for (IFileChangeChunk changeChunk : mergeDiff.getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue())
      {
        if (changeChunk.getChangeType() != EChangeType.SAME)
        {
          mergeDiff.acceptChunk(changeChunk, conflictSide);
        }
      }
    }
  }
}
