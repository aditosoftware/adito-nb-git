package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

/**
 * Dialog with one textField for manually entering a file and a button that opens a fileChooser, if selection of the fileChooser is accepted
 * the path to the selected file is stored in the textField
 *
 * @author m.kaspera, 06.03.2019
 */
public class FileSelectionDialog extends AditoBaseDialog<Object>
{

  private static final int PATH_NUM_CHARS = 60;
  private final JTextField textField;

  @Inject
  public FileSelectionDialog()
  {
    textField = new JTextField(PATH_NUM_CHARS);
    _initGui();
  }

  private void _initGui()
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, fill, gap, pref, gap};
    double[] rows = {gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    JButton chooseFileButton = new JButton("Browse");
    chooseFileButton.addActionListener(e -> {
      JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnValue = fc.showSaveDialog(null);
      if (returnValue == JFileChooser.APPROVE_OPTION)

      {
        textField.setText(fc.getSelectedFile().getAbsolutePath());
      }
    });
    tlu.add(1, 1, textField);
    tlu.add(3, 1, chooseFileButton);
  }

  @Override
  public String getMessage()
  {
    return textField.getText();
  }

  @Override
  public Object getInformation()
  {
    return null;
  }
}
