package de.adito.git.gui.dialogs;

/**
 * @author m.kaspera, 22.11.2019
 */
public interface IDialogResult<S, T>
{
  S getSource();

  String getMessage();

  T getInformation();

  EButtons getSelectedButton();
}
