package de.adito.git.gui.dialogs;

/**
 * @author m.kaspera, 22.11.2019
 */
public interface IDialogResult<SOURCE_TYPE, RESULT_TYPE>
{
  SOURCE_TYPE getSource();

  String getMessage();

  RESULT_TYPE getInformation();

  Object getSelectedButton();
}
