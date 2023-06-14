package de.adito.git.gui.dialogs.filechooser;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Class that offers utility methods centered around the JFileChooser
 *
 * @author m.kaspera, 04.10.2019
 */
public class FileChooserProvider
{

  private static JFileChooser lastFileChooser;
  public static final FileFilter DIRECTORIES_FILTER = new FileFilter()
  {
    @Override
    public boolean accept(File f)
    {
      return f.isDirectory();
    }

    @Override
    public String getDescription()
    {
      return "Shows only directories";
    }
  };

  /**
   * FILES_ONLY: only files can be selected by the user
   * DIRECTORIES_ONLY: only directories can be selected
   * FILES_AND_DIRECTORIES: no distinction between files and directories
   */
  public enum FileSelectionMode
  {
    FILES_ONLY,
    DIRECTORIRES_ONLY,
    FILES_AND_DIRECTORIES
  }

  /**
   * Creates a mew FileChooser and sets the currentDirectory
   *
   * @return FileChooser whose currentDirectory matches the one of the FileChooser that was last active
   */
  public static JFileChooser getFileChooser()
  {
    JFileChooser fileChooser = new JFileChooser();
    updateFileChooser(fileChooser);
    return fileChooser;
  }

  /**
   * sets the currentDirectory of the passed FileChooser to the currentDirectory of the FileChooser that was last active
   *
   * @param pFileChooser FileChooser to update
   */
  public static void updateFileChooser(JFileChooser pFileChooser)
  {
    if (lastFileChooser != null)
      pFileChooser.setCurrentDirectory(lastFileChooser.getCurrentDirectory());
    lastFileChooser = pFileChooser;
  }

  /**
   * Creates a JPanel that includes a JFileChooser and a textfield that displays the File that was selected by the FileChooser
   *
   * @param pFileSelectionMode SelectionMode, only files matching the selected mode may be selected in the JFileChooser
   * @param pFileFilter        FileFilter that determines which files are available for the user to select in the JFileChooser
   * @return JPanel consisting of a label, textfield and a JFileChooser that is connected to the textfield
   */
  public static FileChooserPanel createFileChooserPanel(@NonNull FileSelectionMode pFileSelectionMode,
                                                        @Nullable FileFilter pFileFilter)
  {
    return new FileChooserPanel(pFileSelectionMode, pFileFilter);
  }

  /**
   * Creates a JPanel that includes a JFileChooser and a textfield that displays the File that was selected by the FileChooser
   *
   * @param pFileSelectionMode SelectionMode, only files matching the selected mode may be selected in the JFileChooser
   * @param pFileFilter        FileFilter that determines which files are available for the user to select in the JFileChooser
   * @return JPanel consisting of a label, textfield and a JFileChooser that is connected to the textfield
   */
  public static FileChooserPanel createNewFileChooserPanel(@NonNull FileSelectionMode pFileSelectionMode,
                                                           @Nullable FileFilter pFileFilter, @Nullable String pFileName)
  {
    return new NewFileChooserPanel(pFileSelectionMode, pFileFilter, pFileName);
  }

  public static FileFilter createFilter(String pMatchRegex, boolean pAllowFiles)
  {
    return new CustomFilter(pMatchRegex, pAllowFiles);
  }

  private static class CustomFilter extends FileFilter
  {

    private final String matchRegex;
    private final boolean allowFiles;
    private final Pattern compiledPattern;

    CustomFilter(String pMatchRegex, boolean pAllowFiles)
    {
      matchRegex = pMatchRegex;
      allowFiles = pAllowFiles;
      compiledPattern = Pattern.compile(pMatchRegex);
    }

    @Override
    public boolean accept(File f)
    {
      return (f.isDirectory() || allowFiles) && compiledPattern.matcher(f.getAbsolutePath()).matches();
    }

    @Override
    public String getDescription()
    {
      return String.format("Custom filter that shows %s if they match the regex pattern of %s", allowFiles ? "files and directories" : "only directories", matchRegex);
    }
  }

}
