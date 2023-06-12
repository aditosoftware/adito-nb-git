package de.adito.git.gui.swing;

import de.adito.git.api.data.diff.EChangeStatus;
import de.adito.git.api.data.diff.EConflictSide;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.gui.dialogs.EButtons;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.panels.NotificationPanel;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import de.adito.git.impl.Util;
import de.adito.git.impl.data.diff.EConflictType;
import lombok.NonNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * This button acts as an in-between that can halt sending a "button pressed" action
 * If all conflicts and changes of the merge are resolved, it acts as a normal button. If there are still unresolved conflicts or changes,
 * pressing the button invokes an additional dialog with options. Depending on the options chosen, this button fires the "button pressed" event or hides the event
 * if the user wants to stay in the original dialog.
 *
 * @author m.kaspera, 01.07.2021
 */
public class MergeConflictConditionalButton extends ConditionalDialogButton
{

  private final IMergeData mergeData;
  private final IDialogProvider dialogProvider;
  private EButtons pressedButton = EButtons.ACCEPT_CHANGES;

  public MergeConflictConditionalButton(@NonNull IMergeData pMergeData, @NonNull IDialogProvider pDialogProvider)
  {
    super(EButtons.ACCEPT_CHANGES.toString());
    mergeData = pMergeData;
    dialogProvider = pDialogProvider;
  }

  private class ActionListenerFilter implements ActionListener
  {

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if ((mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().stream().anyMatch(pChangeDelta -> pChangeDelta.getConflictType() == EConflictType.CONFLICTING
          && pChangeDelta.getChangeStatus() == EChangeStatus.PENDING))
          || (mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().stream().anyMatch(pChangeDelta -> pChangeDelta.getConflictType() == EConflictType.CONFLICTING
          && pChangeDelta.getChangeStatus() == EChangeStatus.PENDING)))
      {
        executorService.execute(() -> _unresolvedConflicts(e));
      }
      else if (mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().stream().anyMatch(pChangeDelta -> pChangeDelta.getChangeStatus() == EChangeStatus.PENDING)
          || mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().stream().anyMatch(pChangeDelta -> pChangeDelta.getChangeStatus() == EChangeStatus.PENDING))
      {
        executorService.execute(() -> _unresolvedChanged(e));
      }
      else
      {
        actionListeners.forEach(pActionListener -> pActionListener.actionPerformed(e));
      }
    }

    private void _unresolvedConflicts(ActionEvent e)
    {
      IUserPromptDialogResult<NotificationPanel, Object> dialogResult = dialogProvider
          .showMessageDialog("Unresolved conflicting changes", Util.getResource(MergeConflictConditionalButton.class, "TEXTUnresolvedConflicts"),
                             List.of(EButtons.CONTTINUE_MERGE, EButtons.EXIT_ANYWAY),
                             List.of(EButtons.CONTTINUE_MERGE));
      if (dialogResult.getSelectedButton() == EButtons.EXIT_ANYWAY)
      {
        pressedButton = EButtons.EXIT_ANYWAY;
        SwingUtilities.invokeLater(() -> actionListeners.forEach(pActionListener -> pActionListener.actionPerformed(e)));
      }
    }

    private void _unresolvedChanged(ActionEvent e)
    {
      IUserPromptDialogResult<NotificationPanel, Object> dialogResult = dialogProvider
          .showMessageDialog("Unresolved changes", Util.getResource(MergeConflictConditionalButton.class, "TEXTUnresolvedChanges"),
                             List.of(EButtons.CONTTINUE_MERGE, EButtons.ACCEPT_REMAINING, EButtons.ACCEPT_AS_IS),
                             List.of(EButtons.CONTTINUE_MERGE));
      if (dialogResult.getSelectedButton() == EButtons.ACCEPT_AS_IS)
      {
        pressedButton = EButtons.ACCEPT_AS_IS;
        SwingUtilities.invokeLater(() -> actionListeners.forEach(pActionListener -> pActionListener.actionPerformed(e)));
      }
      else if (dialogResult.getSelectedButton() == EButtons.ACCEPT_REMAINING)
      {
        pressedButton = EButtons.ACCEPT_REMAINING;
        SwingUtilities.invokeLater(() -> actionListeners.forEach(pActionListener -> pActionListener.actionPerformed(e)));
      }
      else
      {
        pressedButton = EButtons.CONTTINUE_MERGE;
      }
    }
  }

  @Override
  @NonNull ActionListener getCustomListener()
  {
    return new ActionListenerFilter();
  }

  @Override
  @NonNull
  public EButtons getPressedButton()
  {
    return pressedButton;
  }
}
