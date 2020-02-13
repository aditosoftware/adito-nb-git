package de.adito.git.gui.dialogs.filechooser;

import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * JPanel consisting of a label, textfield and a FileChooser whose selection is displayed in the textfield
 *
 * @author m.kaspera, 10.09.2019
 */
public class FileChooserPanel extends JPanel
{

  private final JTextField targetPath = new JTextField();
  private final JFileChooser fc;

  FileChooserPanel(@NotNull String pLabel, @NotNull FileChooserProvider.FileSelectionMode pFileSelectionMode)
  {
    this(pLabel, pFileSelectionMode, null);
  }

  FileChooserPanel(@NotNull String pLabel, @NotNull FileChooserProvider.FileSelectionMode pFileSelectionMode, @Nullable FileFilter pFileFilter)
  {
    fc = FileChooserProvider.getFileChooser();
    fc.setFileFilter(pFileFilter);
    targetPath.setText(fc.getCurrentDirectory().getAbsolutePath());
    _initComponents(pLabel, pFileSelectionMode);
  }

  /**
   * @return JTextField that displays the selected file
   */
  @NotNull
  public JTextField getTargetPathField()
  {
    return targetPath;
  }

  /**
   * @return the currently selected file that is displayed in the textfield
   */
  @NotNull
  public String getSelectedFile()
  {
    String currentText = targetPath.getText();
    return currentText == null ? "" : currentText;
  }

  private void _initComponents(String pLabel, FileChooserProvider.FileSelectionMode pFileSelectionMode)
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;

    double[] cols = {pref, gap, fill, gap, pref};
    double[] rows = {gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    JPanel labelTextPanel = new JPanel(new BorderLayout());
    labelTextPanel.add(new JLabel(pLabel), BorderLayout.WEST);
    labelTextPanel.add(targetPath, BorderLayout.CENTER);
    tlu.add(0, 1, new JLabel(pLabel));
    tlu.add(2, 1, targetPath);
    tlu.add(4, 1, _createFileChooserButton(pFileSelectionMode));
  }

  /**
   * Erzeugt einen JButton mit angehängtem JFileChooser, des bei Klick des Button aufgerufen wird. Selektiert der Nutzer ein Directory im FileChooser, so wird
   * der absolute Pfad dieses Ordners in das targetPath Feld eingetragen
   * Creates a JButton with attached JFileChooser that is called when the Button is pressed. If the user selects a file or directory in the FileChooser the path of that
   * file or folder is written to the targetPath field
   *
   * @param pFileSelectionMode FileSelectionMode, determines if only files, only directories or both may be selected
   * @return JButton that shows the JFileChooser when pressed
   */
  private JButton _createFileChooserButton(FileChooserProvider.FileSelectionMode pFileSelectionMode)
  {
    JButton locationBrowseButton = new JButton(". . .");
    locationBrowseButton.setMaximumSize(new Dimension(32, 32));
    locationBrowseButton.addActionListener(e -> {
      fc.setFileSelectionMode(pFileSelectionMode.ordinal());
      int returnValue = fc.showSaveDialog(null);
      if (returnValue == JFileChooser.APPROVE_OPTION)
      {
        targetPath.setText(fc.getSelectedFile().getAbsolutePath());
      }
    });
    return locationBrowseButton;
  }
}
