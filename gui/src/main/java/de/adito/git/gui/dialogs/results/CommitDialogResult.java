package de.adito.git.gui.dialogs.results;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

/**
 * Stores information about the user actions before pressing the commit/OK button in the
 * commit dialog, such as which files were selected to commit and if the commit should be amended
 *
 * @author m.kaspera 20.09.2018
 */
public class CommitDialogResult
{

  private final Supplier<List<File>> selectedFilesSupplier;
  private final boolean doAmend;

  public CommitDialogResult(Supplier<List<File>> pSelectedFilesSupplier, boolean pDoAmend)
  {
    this.selectedFilesSupplier = pSelectedFilesSupplier;
    this.doAmend = pDoAmend;
  }

  public Supplier<List<File>> getSelectedFilesSupplier()
  {
    return selectedFilesSupplier;
  }

  public boolean isDoAmend()
  {
    return doAmend;
  }
}
