package de.adito.git.gui.dialogs.filechooser;

import de.adito.git.gui.swing.TextFieldWithPlaceholder;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Dimension;

/**
 * JPanel consisting of a label, textfield and a FileChooser whose selection is displayed in the textfield
 *
 * @author m.kaspera, 10.09.2019
 */
public class FileChooserPanel extends JPanel
{

  final double gap = 15;
  final TextFieldWithPlaceholder targetPath;
  final JFileChooser fc;
  final double fill = TableLayout.FILL;
  final double pref = TableLayout.PREFERRED;
  TableLayoutUtil tlu;

  FileChooserPanel(@NotNull FileChooserProvider.FileSelectionMode pFileSelectionMode, @Nullable FileFilter pFileFilter)
  {
    fc = FileChooserProvider.getFileChooser();
    fc.setFileFilter(pFileFilter);
    targetPath = new TextFieldWithPlaceholder(fc.getCurrentDirectory().getAbsolutePath(), fc.getCurrentDirectory().getAbsolutePath());
    _initComponents(pFileSelectionMode);
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
    return targetPath.getText();
  }

  private void _initComponents(@NotNull FileChooserProvider.FileSelectionMode pFileSelectionMode)
  {


    double[] cols = getColumns();
    double[] rows = getRows();
    setLayout(new TableLayout(cols, rows));
    tlu = new TableLayoutUtil(this);
    tlu.add(0, 1, new JLabel(getFileChooserLabel()));
    tlu.add(2, 1, targetPath);
    tlu.add(4, 1, _createFileChooserButton(pFileSelectionMode));
    setPreferredSize(new Dimension(550, 45));
  }

  double[] getRows()
  {
    return new double[]{gap,
                        pref,
                        gap};
  }

  double[] getColumns()
  {
    return new double[]{pref, gap, fill, gap, pref};
  }

  String getFileChooserLabel()
  {
    return "Path:";
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
    JButton locationBrowseButton = new JButton("Browse");
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
