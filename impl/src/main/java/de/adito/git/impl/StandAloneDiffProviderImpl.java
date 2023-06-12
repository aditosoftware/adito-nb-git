package de.adito.git.impl;

import com.google.inject.Inject;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.IStandAloneDiffProvider;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.impl.data.diff.*;
import lombok.NonNull;
import org.eclipse.jgit.diff.*;

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
  public IFileDiff diffOffline(@NonNull String pVersion1, @NonNull String pVersion2)
  {
    return diffOffline(pVersion1.getBytes(), pVersion2.getBytes());
  }

  @Override
  public IFileDiff diffOffline(@NonNull byte[] pVersion1, @NonNull byte[] pVersion2)
  {
    RawText fileContents = new RawText(pVersion1);
    RawText currentFileContents = new RawText(pVersion2);

    EditList linesChanged = new HistogramDiff().diff(RawTextComparator.WS_IGNORE_TRAILING, fileContents, currentFileContents);
    String fictionalFile = new File(new File(""), SOME_TEMP_FILE_NAME).getPath();
    return new FileDiffImpl(new FileDiffHeaderImpl(new DiffPathInfoImpl(null, fictionalFile, fictionalFile),
                                                   new DiffDetailsImpl("1", "2", EChangeType.MODIFY, EFileType.FILE, EFileType.FILE)),
                            linesChanged,
                            new FileContentInfoImpl(() -> pVersion1, fileSystemUtil),
                            new FileContentInfoImpl(() -> pVersion2, fileSystemUtil));
  }

  /**
   * Diffs two strings and finds the differences
   *
   * @param pVersion1 String to be compared to pVersion2
   * @param pVersion2 String to be compared to pVersion1
   * @return EditList with changed lines between version 1 and 2
   */
  @NonNull
  public static EditList getChangedLines(@NonNull String pVersion1, @NonNull String pVersion2)
  {
    return getChangedLines(pVersion1.getBytes(), pVersion2.getBytes());
  }

  /**
   * Diffs the contents of two byte arrays and finds the differences
   *
   * @param pVersion1 String to be compared to pVersion2
   * @param pVersion2 String to be compared to pVersion1
   * @return EditList with changed lines between version 1 and 2
   */
  @NonNull
  public static EditList getChangedLines(@NonNull byte[] pVersion1, @NonNull byte[] pVersion2)
  {
    RawText fileContents = new RawText(pVersion1);
    RawText currentFileContents = new RawText(pVersion2);

    return new HistogramDiff().diff(RawTextComparator.WS_IGNORE_TRAILING, fileContents, currentFileContents);
  }

}
