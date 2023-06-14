package de.adito.git.gui.dialogs.filechooser;

import de.adito.git.gui.swing.TextFieldWithPlaceholder;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Dimension;

/**
 * @author m.kaspera, 12.03.2021
 */
public class NewFileChooserPanel extends FileChooserPanel
{

  private final TextFieldWithPlaceholder fileNameField;
  private final String fileName;

  public NewFileChooserPanel(@NonNull FileChooserProvider.FileSelectionMode pFileSelectionMode, @Nullable FileFilter pFileFilter, @Nullable String pFileName)
  {
    super(pFileSelectionMode, pFileFilter);
    fileName = pFileName;
    fileNameField = new TextFieldWithPlaceholder(pFileName);
    _initComponents();
  }

  @Override
  double[] getRows()
  {
    return new double[]{gap,
                        pref,
                        gap,
                        pref,
                        gap};
  }

  @Override
  double[] getColumns()
  {
    return new double[]{pref, gap, fill, gap, pref};
  }

  @Override
  String getFileChooserLabel()
  {
    return "Directory:";
  }

  /**
   * @return the currently selected file that is displayed in the textfield
   */
  @Override
  @NonNull
  public String getSelectedFile()
  {
    String currentText = targetPath.getText();
    if (currentText == null)
      currentText = "";
    String fieldText = fileNameField.getText();
    if (fieldText != null)
    {
      if (fieldText.isEmpty())
      {
        fieldText = fileName;
      }
      if (currentText.endsWith("/"))
      {
        currentText += fieldText;
      }
      else
      {
        currentText += "/" + fieldText;
      }
    }
    return currentText;
  }

  private void _initComponents()
  {
    tlu.add(0, 1, new JLabel("Directory:"));
    tlu.add(0, 3, new JLabel("Filename:"));
    tlu.add(2, 3, 4, 3, fileNameField);
    setPreferredSize(new Dimension(550, 80));
  }
}
