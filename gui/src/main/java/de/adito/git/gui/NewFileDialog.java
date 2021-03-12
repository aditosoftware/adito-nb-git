package de.adito.git.gui;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.dialogs.AditoBaseDialog;
import de.adito.git.gui.dialogs.filechooser.FileChooserPanel;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;

import javax.annotation.Nullable;
import javax.swing.filechooser.FileFilter;

/**
 * @author m.kaspera, 12.03.2021
 */
public class NewFileDialog extends AditoBaseDialog<Object>
{

  private final FileChooserPanel fileChooserPanel;

  @Inject
  NewFileDialog(@Assisted FileChooserProvider.FileSelectionMode pFileSelectionMode, @Assisted @Nullable FileFilter pFileFilter, @Assisted @Nullable String pFileName)
  {
    fileChooserPanel = FileChooserProvider.createNewFileChooserPanel(pFileSelectionMode, pFileFilter, pFileName);
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
