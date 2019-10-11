package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.dialogs.filechooser.FileChooserPanel;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;

import javax.annotation.Nullable;
import javax.swing.filechooser.FileFilter;

/**
 * Dialog with one textField for manually entering a file and a button that opens a fileChooser, if selection of the fileChooser is accepted
 * the path to the selected file is stored in the textField
 *
 * @author m.kaspera, 06.03.2019
 */
class FileSelectionDialog extends AditoBaseDialog<Object>
{

  private final FileChooserPanel fileChooserPanel;

  @Inject
  FileSelectionDialog(@Assisted String pLabel, @Assisted FileChooserProvider.FileSelectionMode pFileSelectionMode, @Assisted @Nullable FileFilter pFileFilter)
  {
    if (pFileFilter == null)
      fileChooserPanel = FileChooserProvider.createFileChooserPanel(pLabel, pFileSelectionMode);
    else
      fileChooserPanel = FileChooserProvider.createFileChooserPanel(pLabel, pFileSelectionMode, pFileFilter);
    add(fileChooserPanel);
  }

  @Override
  public String getMessage()
  {
    return fileChooserPanel.getSelectedFile();
  }

  @Override
  public Object getInformation()
  {
    return null;
  }
}
