package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.dialogs.panels.basediffpanel.MergePanel;
import de.adito.git.gui.icon.IIconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

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
class MergeConflictResolutionDialog extends AditoBaseDialog<Object> implements IDiscardable
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
    _initGui(pIconLoader);
  }

  private void _initGui(IIconLoader pIconLoader)
  {
    // create a panel in a scrollPane for each of the textPanes
    setLayout(new BorderLayout());

    JPanel diffPanel = new JPanel(new BorderLayout());
    diffPanel.add(mergePanel, BorderLayout.CENTER);
    diffPanel.add(_initToolbar(pIconLoader), BorderLayout.NORTH);
    diffPanel.setPreferredSize(new Dimension(1600, 900));
    add(diffPanel, BorderLayout.CENTER);
  }

  @NotNull
  private JToolBar _initToolbar(IIconLoader pIconLoader)
  {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // Merge-Panel-Actions (Next | Previous)
    mergePanel.getActions().stream()
        .map(JButton::new)
        .forEach(toolbar::add);

    for (Component component : toolbar.getComponents())
    {
      component.setFocusable(false);
    }

    toolbar.addSeparator();

    // Our Actions
    toolbar.add(new JButton(new _AcceptAllActionImpl(IMergeDiff.CONFLICT_SIDE.YOURS, pIconLoader)));
    toolbar.add(new JButton(new _AcceptNonConflictingChangesAction(pIconLoader)));
    toolbar.add(new JButton(new _AcceptAllActionImpl(IMergeDiff.CONFLICT_SIDE.THEIRS, pIconLoader)));

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

  @Override
  public void discard()
  {
    mergePanel.discard();
  }

  /**
   * AcceptAll-ActionImpl
   */
  private class _AcceptAllActionImpl extends AbstractAction
  {
    private final IMergeDiff.CONFLICT_SIDE conflictSide;

    _AcceptAllActionImpl(@NotNull IMergeDiff.CONFLICT_SIDE pConflictSide, IIconLoader pIconLoader)
    {
      super("", pIconLoader.getIcon(pConflictSide == IMergeDiff.CONFLICT_SIDE.YOURS ? Constants.ACCEPT_ALL_LEFT : Constants.ACCEPT_ALL_RIGHT));
      putValue(SHORT_DESCRIPTION, "accept remaining " + pConflictSide.name() + " changes");
      conflictSide = pConflictSide;
    }

    @Override
    public void actionPerformed(ActionEvent pEvent)
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

  /**
   * Action that accepts all non-conflicting changes of both sides
   */
  private class _AcceptNonConflictingChangesAction extends AbstractAction
  {

    _AcceptNonConflictingChangesAction(IIconLoader pIconLoader)
    {
      super("", pIconLoader.getIcon(Constants.ACCEPT_ALL_NON_CONFLICTING));
      putValue(SHORT_DESCRIPTION, "accept remaining non-conflicting changes");
    }

    @Override
    public void actionPerformed(ActionEvent pEvent)
    {
      _acceptSide(IMergeDiff.CONFLICT_SIDE.YOURS);
      _acceptSide(IMergeDiff.CONFLICT_SIDE.THEIRS);
    }

    @Override
    public boolean isEnabled()
    {
      return _containsNonConflicting(IMergeDiff.CONFLICT_SIDE.YOURS) || _containsNonConflicting(IMergeDiff.CONFLICT_SIDE.THEIRS);
    }

    /**
     * accepts any as-of-yet unaccepted non-conflicting changes on the given conflict side
     *
     * @param pConflictSide CONFLICT_SIDE
     */
    private void _acceptSide(IMergeDiff.CONFLICT_SIDE pConflictSide)
    {
      IMergeDiff.CONFLICT_SIDE theirSide = pConflictSide == IMergeDiff.CONFLICT_SIDE.YOURS ? IMergeDiff.CONFLICT_SIDE.THEIRS : IMergeDiff.CONFLICT_SIDE.YOURS;
      List<IFileChangeChunk> theirChanges = mergeDiff.getDiff(theirSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue();
      for (IFileChangeChunk changeChunk : mergeDiff.getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue())
      {
        boolean isConflicting = IMergeDiff.affectedChunkIndices(changeChunk, theirChanges).stream()
            .map(theirChanges::get)
            .anyMatch(pChange -> pChange.getChangeType() != EChangeType.SAME);
        if (changeChunk.getChangeType() != EChangeType.SAME && !isConflicting)
        {
          mergeDiff.acceptChunk(changeChunk, pConflictSide);
        }
      }
    }

    /**
     * checks if there are any as-of-yet unaccepted non-conflicting changes on the given conflict side
     *
     * @param pConflictSide CONFLICT_SIDE to check
     * @return true if there are any non-conflicting changes that are not accepted, false otherwise
     */
    private boolean _containsNonConflicting(IMergeDiff.CONFLICT_SIDE pConflictSide)
    {
      IMergeDiff.CONFLICT_SIDE theirSide = pConflictSide == IMergeDiff.CONFLICT_SIDE.YOURS ? IMergeDiff.CONFLICT_SIDE.THEIRS : IMergeDiff.CONFLICT_SIDE.YOURS;
      List<IFileChangeChunk> theirChanges = mergeDiff.getDiff(theirSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue();
      for (IFileChangeChunk changeChunk : mergeDiff.getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue())
      {
        boolean isConflicting = IMergeDiff.affectedChunkIndices(changeChunk, theirChanges).stream()
            .map(theirChanges::get)
            .anyMatch(pChange -> pChange.getChangeType() != EChangeType.SAME);
        if (changeChunk.getChangeType() != EChangeType.SAME && !isConflicting)
        {
          // one changeChunk that can be accepted and that is not conflicting exists -> Action is enabled
          return true;
        }
      }
      return false;
    }
  }
}
