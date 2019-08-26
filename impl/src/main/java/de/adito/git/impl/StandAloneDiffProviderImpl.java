package de.adito.git.impl;

import com.google.inject.Inject;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.IStandAloneDiffProvider;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.impl.data.FileContentInfoImpl;
import de.adito.git.impl.data.FileDiffHeaderImpl;
import de.adito.git.impl.data.FileDiffImpl;
import org.eclipse.jgit.diff.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author m.kaspera, 23.08.2019
 */
public class StandAloneDiffProviderImpl implements IStandAloneDiffProvider
{

  private static final String SOME_TEMP_FILE_NAME = "/tmp/file";
  private final IFileSystemUtil fileSystemUtil;

  @Inject
  public StandAloneDiffProviderImpl(IFileSystemUtil pFileSystemUtil)
  {
    fileSystemUtil = pFileSystemUtil;
  }

  @Override
  public IFileDiff diffOffline(@NotNull String pVersion1, @NotNull String pVersion2)
  {
    return diffOffline(pVersion1.getBytes(), pVersion2.getBytes());
  }

  @Override
  public IFileDiff diffOffline(@NotNull byte[] pVersion1, @NotNull byte[] pVersion2)
  {
    RawText fileContents = new RawText(pVersion1);
    RawText currentFileContents = new RawText(pVersion2);

    EditList linesChanged = new HistogramDiff().diff(RawTextComparator.WS_IGNORE_TRAILING, fileContents, currentFileContents);
    String fictionalFile = new File(new File(""), SOME_TEMP_FILE_NAME).getPath();
    return new FileDiffImpl(new FileDiffHeaderImpl(null, "1", "2", EChangeType.MODIFY, EFileType.FILE, EFileType.FILE, fictionalFile, fictionalFile), linesChanged,
                            new FileContentInfoImpl(() -> pVersion1, fileSystemUtil),
                            new FileContentInfoImpl(() -> pVersion2, fileSystemUtil));
  }
}
