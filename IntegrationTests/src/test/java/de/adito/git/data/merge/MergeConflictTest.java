package de.adito.git.data.merge;

import de.adito.aditoweb.nbm.nbaditointerfaceimpl.javascript.JsParserUtilityImpl;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.IJsParserUtility;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.data.diff.ImportResolveOption;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.guice.dummies.SimpleNotifyUtil;
import de.adito.git.gui.progress.SimpleAsyncProgressFacade;
import de.adito.git.gui.sequences.MergeConflictSequence;
import de.adito.git.impl.StandAloneDiffProviderImpl;
import de.adito.git.impl.data.diff.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openide.util.Lookup;

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
 * Simulates different merges that can or cannot be resolved by the auto-resolve. Each test case is its owm folder with up to 4 specific files.
 * For adding a test case, just add another folder to the resources, set up the files to simulate the wanted merge case and add the folder name to the
 * stream in getConflictFiles
 *
 * @author m.kaspera, 08.05.2020
 */
public class MergeConflictTest
{

  public static Stream<Arguments> getConflictFiles()
  {
    return Stream.of(
            createFullFileNames("AddRemoveLines", ""),
            createFullFileNames("EqualLineConflict", ""),
            createFullFileNames("NotResolvableConflict", ""),
            createFullFileNames("NoConflicts", ""),
            createFullFileNames("SameLineConflict", ""),
            createFullFileNames("MoreChangesInOneLine", ""),
            //createFullFileNames("MultilineResolvable"),
            createFullFileNames("ImportJsUpgradeConflict", ".js"),
            createFullFileNames("ImportsConflict", ".js"),
            createFullFileNames("MultipleConflicts", ""),
            createFullFileNames("LiquibaseSimpleResolve", ".xml"),
            createFullFileNames("LiquibaseNonResolvable", ".xml"),
            createFullFileNames("LanguageDifferentKeysResolvable", ".aod"),
            createFullFileNames("LanguageDifferentMultiKeysResolvable", ".aod"),
            createFullFileNames("LanguageSameKeyValueResolvable", ".aod"),
            createFullFileNames("LanguageMissingValueTagNonResolvable", ".aod"),
            createFullFileNames("LanguageSameKeyNonResolvable", ".aod"))
        .map(pFileList -> Arguments.of(pFileList.get(0),
                                       pFileList.get(1),
                                       readResourceFile(pFileList.get(1)),
                                       readResourceFile(pFileList.get(2)),
                                       readResourceFile(pFileList.get(3)),
                                       readResourceFile(pFileList.get(4))));
  }

  /**
   * reads the contents of a file, returns null if any exception occurred during the read instead of throwing an exception
   *
   * @param pFileName name of the file to read
   * @return contents of the file as string, or null if any exception occurred while trying to read the file
   */
  @Nullable
  private static String readResourceFile(@NotNull String pFileName)
  {
    try (InputStream resourceAsStream = MergeConflictTest.class.getResourceAsStream(pFileName))
    {
      if (resourceAsStream != null)
        return new String(resourceAsStream.readAllBytes());
    }
    catch (IOException ignored)
    {
    }
    return null;
  }

  /**
   * create a list with paths to the 4 files used for setting up a conflict for testing. These files are:
   * Original: contains the "Base" of the conflict, i.e. the file before the two branches diverged
   * VersionA: contains the file as it is in Branch A
   * VersionB: contains the file as it is in Branch B
   * Expected: Contains the expected outcome of the autoresolve merge. If the autoresolve should not be able to resolve the conflict, this file has to be missing
   *
   * @param pCoreFileName name of the folder that contains the files, should be relevant to what happens inside the test since this name is displayed as the name of the test
   * @return List with paths to the relevant files for setting up a test merge
   */
  private static List<String> createFullFileNames(@NotNull String pCoreFileName, @NotNull String pFileExtension)
  {
    return List.of(pCoreFileName, pCoreFileName + "/Original" + pFileExtension, pCoreFileName + "/VersionA" + pFileExtension,
                   pCoreFileName + "/VersionB" + pFileExtension, pCoreFileName + "/Expected" + pFileExtension);
  }

  @SuppressWarnings("unused") // pName wird zur besseren Lesbarkeit des Testnamens verwendet, siehe name in der ParameterizedTest Annotation
  @ParameterizedTest(name = "{index} : {0}")
  @MethodSource("getConflictFiles")
  void performAutoResolve(@NotNull String pName, @NotNull String pFileName, @NotNull String pOriginalContents, @NotNull String pVersionAContents,
                          @NotNull String pVersionBContents, @Nullable String pExpectedResult)
      throws AditoGitException, IOException
  {
    SimpleAsyncProgressFacade asyncProgressFacade = new SimpleAsyncProgressFacade();
    SimpleNotifyUtil notifyUtil = new SimpleNotifyUtil();
    IRepository repository = Mockito.mock(IRepository.class);
    doNothing().when(repository).add(Mockito.anyList());
    Path tempDirectory = Files.createTempDirectory("mergeConflictSequenceTest");
    Lookup lookupMock = Mockito.mock(Lookup.class);
    when(lookupMock.lookup(IJsParserUtility.class)).thenReturn(new JsParserUtilityImpl());
    try (MockedStatic<Lookup> lookupMockedStatic = Mockito.mockStatic(Lookup.class))
    {
      lookupMockedStatic.when(Lookup::getDefault).thenReturn(lookupMock);
      when(repository.getTopLevelDirectory()).thenReturn(tempDirectory.toFile());

      IDiffPathInfo diffPathInfo = new DiffPathInfoImpl(tempDirectory.toFile(), pFileName, pFileName);
      IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE);
      FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo, diffDetails);
      IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> pOriginalContents, () -> StandardCharsets.UTF_8);
      IFileContentInfo newFileContent = new FileContentInfoImpl(() -> pVersionAContents, () -> StandardCharsets.UTF_8);
      IFileContentInfo secondNewFileContent = new FileContentInfoImpl(() -> pVersionBContents, () -> StandardCharsets.UTF_8);
      IFileDiff yourFileDiff = new FileDiffImpl(fileDiffHeader, StandAloneDiffProviderImpl.getChangedLines(pOriginalContents, pVersionAContents), oldFileContent, newFileContent);
      IFileDiff theirDiff = new FileDiffImpl(fileDiffHeader, StandAloneDiffProviderImpl.getChangedLines(pOriginalContents, pVersionBContents), oldFileContent, secondNewFileContent);
      IMergeData mergeData = new MergeDataImpl(yourFileDiff, theirDiff);

      IDialogProvider dialogProvider = Mockito.mock(IDialogProvider.class);

      List<IMergeData> mergeDataList = new ArrayList<>();
      mergeDataList.add(mergeData);
      MergeConflictSequence.performAutoResolve(mergeDataList, repository, asyncProgressFacade, notifyUtil, () -> List.of(new SameResolveOption(),
                                                                                                                         new EnclosedResolveOption(),
                                                                                                                         new WordBasedResolveOption(),
                                                                                                                         new ImportResolveOption(),
                                                                                                                         new LiquibaseResolveOption(),
                                                                                                                         new LanguageFileResolveOption()),
                                               dialogProvider);

      if (pExpectedResult != null)
      {
        assertEquals(pExpectedResult, new String(Files.readAllBytes(new File(tempDirectory.toFile(), pFileName).toPath())));
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