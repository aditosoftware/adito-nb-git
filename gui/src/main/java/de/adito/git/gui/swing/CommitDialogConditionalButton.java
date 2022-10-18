package de.adito.git.gui.swing;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IBeforeCommitAction;
import de.adito.git.gui.DelayedSupplier;
import de.adito.git.gui.dialogs.EButtons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 16.05.2022
 */
public class CommitDialogConditionalButton extends ConditionalDialogButton
{

  private final DelayedSupplier<List<IBeforeCommitAction>> beforeCommitActions;
  private final DelayedSupplier<List<File>> selectedFiles;
  private EButtons pressedButton;

  public CommitDialogConditionalButton(@NotNull EButtons pButton, @NotNull DelayedSupplier<List<IBeforeCommitAction>> pBeforeCommitActions,
                                       @NotNull DelayedSupplier<List<File>> pSelectedFiles)
  {
    super(pButton.toString());
    pressedButton = pButton;
    beforeCommitActions = pBeforeCommitActions;
    selectedFiles = pSelectedFiles;
  }

  @Override
  @NotNull
  ActionListener getCustomListener()
  {
    return new ActionListenerFilter();
  }

  @Override
  @NotNull
  public EButtons getPressedButton()
  {
    return pressedButton;
  }

  private class ActionListenerFilter implements ActionListener
  {

    @Override
    public void actionPerformed(ActionEvent e)
    {
      executorService.submit(() -> {

        for (IBeforeCommitAction beforeCommitAction : Optional.ofNullable(beforeCommitActions.get()).orElse(List.of()))
        {
          if (!beforeCommitAction.performAction(Optional.ofNullable(selectedFiles.get()).orElse(List.of())))
          {
            pressedButton = EButtons.CANCEL;
            break;
          }
        }
        SwingUtilities.invokeLater(() -> actionListeners.forEach(pActionListener -> pActionListener.actionPerformed(e)));
      });
    }
  }
}
