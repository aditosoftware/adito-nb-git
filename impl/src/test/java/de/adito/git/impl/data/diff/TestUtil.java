package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileContentInfo;
import de.adito.git.api.data.diff.IFileDiff;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * @author m.kaspera, 03.03.2020
 */
class TestUtil
{

  /**
   * Convenience method to shorten the amount of code for setting up a diff for a test
   *
   * @param pEditList   List with edits describing the changed lines
   * @param pOldVersion Text before changes
   * @param pNewVersion Text after changes
   * @return IFileDiff with some default parameters and the passed arguments
   */
  static IFileDiff _createFileDiff(@NotNull EditList pEditList, @NotNull String pOldVersion, @NotNull String pNewVersion)
  {
    IDiffPathInfo diffPathInfo = new DiffPathInfoImpl(null, "filea", "fileb");
    IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE);
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo, diffDetails);
    IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> pOldVersion, () -> StandardCharsets.UTF_8);
    IFileContentInfo newFileContent = new FileContentInfoImpl(() -> pNewVersion, () -> StandardCharsets.UTF_8);
    return new FileDiffImpl(fileDiffHeader, pEditList, oldFileContent, newFileContent);
  }

}
