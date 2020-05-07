package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.diff.*;
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

  private final IMergeData mergeDiff;
  private final MergePanel mergePanel;

  @Inject
  MergeConflictResolutionDialog(IIconLoader pIconLoader, IEditorKitProvider pEditorKitProvider, @Assisted IMergeData pMergeDiff)
  {
    mergeDiff = pMergeDiff;
    ImageIcon acceptYoursIcon = pIconLoader.getIcon(ACCEPT_CHANGE_YOURS_ICON);
    ImageIcon acceptTheirsIcon = pIconLoader.getIcon(ACCEPT_CHANGE_THEIRS_ICON);
    ImageIcon discardIcon = pIconLoader.getIcon(DISCARD_CHANGE_ICON);
    mergeDiff.markConflicting();
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
    toolbar.add(new JButton(new _AcceptAllActionImpl(EConflictSide.YOURS, pIconLoader)));
    toolbar.add(new JButton(new _AcceptNonConflictingChangesAction(pIconLoader)));
    toolbar.add(new JButton(new _AcceptAllActionImpl(EConflictSide.THEIRS, pIconLoader)));

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
   * Accepts all non-conflicting changes of a specified side
   *
   * @param pConflictSide EConflictSide that should be accepted
   */
  private void _acceptNonConflictingDeltas(EConflictSide pConflictSide)
  {
    for (IChangeDelta changeDelta : mergeDiff.getDiff(pConflictSide).getChangeDeltas())
    {
      if (changeDelta.getChangeStatus() == EChangeStatus.PENDING && changeDelta.getChangeType() != EChangeType.CONFLICTING)
      {
        mergeDiff.acceptDelta(changeDelta, pConflictSide);
      }
    }
  }

  /**
   * AcceptAll-ActionImpl
   */
  private class _AcceptAllActionImpl extends AbstractAction
  {
    private final EConflictSide conflictSide;

    _AcceptAllActionImpl(@NotNull EConflictSide pConflictSide, IIconLoader pIconLoader)
    {
      super("", pIconLoader.getIcon(pConflictSide == EConflictSide.YOURS ? Constants.ACCEPT_ALL_LEFT : Constants.ACCEPT_ALL_RIGHT));
      putValue(SHORT_DESCRIPTION, "accept remaining " + pConflictSide.name() + " changes");
      conflictSide = pConflictSide;
    }

    @Override
    public void actionPerformed(ActionEvent pEvent)
    {
      _acceptNonConflictingDeltas(conflictSide);
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
      _acceptSide(EConflictSide.YOURS);
      _acceptSide(EConflictSide.THEIRS);
    }

    // TODO find a way to re-evaluate isEnabled if a delta is accepted without too much of a performance drain if e.g. all non-conflicting changes are accepted
    @Override
    public boolean isEnabled()
    {
      return _containsNonConflicting(EConflictSide.YOURS) || _containsNonConflicting(EConflictSide.THEIRS);
    }

    /**
     * accepts any as-of-yet unaccepted non-conflicting changes on the given conflict side
     *
     * @param pConflictSide CONFLICT_SIDE
     */
    private void _acceptSide(EConflictSide pConflictSide)
    {
      _acceptNonConflictingDeltas(pConflictSide);
    }

    /**
     * checks if there are any as-of-yet unaccepted non-conflicting changes on the given conflict side
     *
     * @param pConflictSide CONFLICT_SIDE to check
     * @return true if there are any non-conflicting changes that are not accepted, false otherwise
     */
    private boolean _containsNonConflicting(EConflictSide pConflictSide)
    {
      for (IChangeDelta changeDelta : mergeDiff.getDiff(pConflictSide).getChangeDeltas())
      {
        if (changeDelta.getChangeType() != EChangeType.CONFLICTING && changeDelta.getChangeStatus() == EChangeStatus.PENDING)
        {
          // one changeDelta that can be accepted and that is not conflicting exists -> Action is enabled
          return true;
        }
      }
      return false;
    }
  }
}
