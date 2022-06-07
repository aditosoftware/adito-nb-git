package de.adito.git.gui.sequences;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.guice.dummies.SimpleNotifyUtil;
import de.adito.git.gui.progress.SimpleAsyncProgressFacade;
import de.adito.git.impl.StandAloneDiffProviderImpl;
import de.adito.git.impl.data.diff.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author m.kaspera, 08.05.2020
 */
public class MergeConflictSequenceTest
{

  public static Stream<Arguments> getConflictFiles()
  {
    return Stream.of(
            //createFullFileNames("AddRemoveLines"),
            //createFullFileNames("EqualLineConflict"),
            //createFullFileNames("NotResolvableConflict"),
            //createFullFileNames("NoConflicts"),
            //createFullFileNames("SameLineConflict"),
            //createFullFileNames("MoreChangesInOneLine"),
            createFullFileNames("MultilineResolvable"),
            createFullFileNames("MultipleConflicts"))
        .map(pFileList -> Arguments.of(pFileList.get(0),
                                       readResourceFile(pFileList.get(1)),
                                       readResourceFile(pFileList.get(2)),
                                       readResourceFile(pFileList.get(3)),
                                       readResourceFile(pFileList.get(4))));
  }

  private static String readResourceFile(@NotNull String pFileName)
  {
    try
    {
      InputStream resourceAsStream = MergeConflictSequenceTest.class.getResourceAsStream(pFileName);
      if (resourceAsStream != null)
        return new String(resourceAsStream.readAllBytes());
    }
    catch (IOException ignored)
    {
    }
    return null;
  }

  private static List<String> createFullFileNames(@NotNull String pCoreFileName)
  {
    return List.of(pCoreFileName, pCoreFileName + "/Original", pCoreFileName + "/VersionA", pCoreFileName + "/VersionB", pCoreFileName + "/Expected");
  }

  @Test
  void testAdjustLineEndingsFromUnixToWindows()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.WINDOWS, ELineEnding.WINDOWS);
    String testString = "Hello there\nHow are you?\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\r\nHow are you?\r\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromUnixToMac()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.MAC, ELineEnding.MAC);
    String testString = "Hello there\nHow are you?\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\rHow are you?\r", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromWindowsToUnix()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.UNIX, ELineEnding.UNIX);
    String testString = "Hello there\r\nHow are you?\r\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\nHow are you?\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToUnix()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.UNIX, ELineEnding.UNIX);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\nHow are you?\nThere's another one\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToMac()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.MAC, ELineEnding.MAC);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\rHow are you?\rThere's another one\r", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToWindows()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.WINDOWS, ELineEnding.WINDOWS);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\r\nHow are you?\r\nThere's another one\r\n", adjustLineEndings);
  }

  /**
   * create a IMergeData object with mocked method calls such that when the lineEndings for the two NEW sides are called, the given ELineEndings are returned
   *
   * @param pLineEndingTheirs ELineEnding for the NEW side of the THEIRS conflictSide
   * @param pLineEndingYours  ELineEnding for the NEW side of the YOURS conflictSide
   * @return mocked IMergeData object
   */
  private static IMergeData _createMergeData(ELineEnding pLineEndingTheirs, ELineEnding pLineEndingYours)
  {
    IFileContentInfo fileContentInfoTheirs = Mockito.mock(IFileContentInfo.class);
    when(fileContentInfoTheirs.getLineEnding()).thenReturn(() -> pLineEndingTheirs);
    IFileDiff fileDiffTheirs = Mockito.mock(IFileDiff.class);
    when(fileDiffTheirs.getFileContentInfo(EChangeSide.NEW)).thenReturn(fileContentInfoTheirs);
    IFileContentInfo fileContentInfoYours = Mockito.mock(IFileContentInfo.class);
    when(fileContentInfoYours.getLineEnding()).thenReturn(() -> pLineEndingYours);
    IFileDiff fileDiffYours = Mockito.mock(IFileDiff.class);
    when(fileDiffYours.getFileContentInfo(EChangeSide.NEW)).thenReturn(fileContentInfoYours);
    IMergeData mergeData = Mockito.mock(IMergeData.class);
    when(mergeData.getDiff(EConflictSide.THEIRS)).thenReturn(fileDiffTheirs);
    when(mergeData.getDiff(EConflictSide.YOURS)).thenReturn(fileDiffYours);
    return mergeData;
  }

  @SuppressWarnings("unused") // pName wird zur besseren Lesbarkeit des Testnamens verwendet, siehe name in der ParameterizedTest Annotation
  @ParameterizedTest(name = "{index} : {0}")
  @MethodSource("getConflictFiles")
  void performAutoResolve(@NotNull String pName, @NotNull String pOriginalContents, @NotNull String pVersionAContents, @NotNull String pVersionBContents, @Nullable String pExpectedResult)
      throws AditoGitException, IOException
  {
    SimpleAsyncProgressFacade asyncProgressFacade = new SimpleAsyncProgressFacade();
    SimpleNotifyUtil notifyUtil = new SimpleNotifyUtil();
    IRepository repository = Mockito.mock(IRepository.class);
    doNothing().when(repository).add(Mockito.anyList());
    Path tempDirectory = Files.createTempDirectory("mergeConflictSequenceTest");
    try
    {
      when(repository.getTopLevelDirectory()).thenReturn(tempDirectory.toFile());

      IDiffPathInfo diffPathInfo = new DiffPathInfoImpl(tempDirectory.toFile(), "filea", "filea");
      IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE);
      FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo, diffDetails);
      IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> pOriginalContents, () -> StandardCharsets.UTF_8);
      IFileContentInfo newFileContent = new FileContentInfoImpl(() -> pVersionAContents, () -> StandardCharsets.UTF_8);
      IFileContentInfo secondNewFileContent = new FileContentInfoImpl(() -> pVersionBContents, () -> StandardCharsets.UTF_8);
      IFileDiff yourFileDiff = new FileDiffImpl(fileDiffHeader, StandAloneDiffProviderImpl.getChangedLines(pOriginalContents, pVersionAContents), oldFileContent, newFileContent);
      IFileDiff theirDiff = new FileDiffImpl(fileDiffHeader, StandAloneDiffProviderImpl.getChangedLines(pOriginalContents, pVersionBContents), oldFileContent, secondNewFileContent);
      IMergeData mergeData = new MergeDataImpl(yourFileDiff, theirDiff);

      List<IMergeData> mergeDataList = new ArrayList<>();
      mergeDataList.add(mergeData);
      MergeConflictSequence.performAutoResolve(mergeDataList, repository, asyncProgressFacade, notifyUtil);

      if (pExpectedResult != null)
      {
        assertEquals(pExpectedResult, new String(Files.readAllBytes(new File(tempDirectory.toFile(), "filea").toPath())));
        assertEquals(0, mergeDataList.size());
      }
      else
      {
        // conflict could not be resolved -> file is still in list of conflicts
        assertEquals(1, mergeDataList.size());
      }
    }
    finally
    {
      FileUtils.deleteDirectory(tempDirectory.toFile());
    }
  }
}
