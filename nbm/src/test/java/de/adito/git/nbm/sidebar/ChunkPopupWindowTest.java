package de.adito.git.nbm.sidebar;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.impl.data.diff.FileContentInfoImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mockito;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * Test class for {@link ChunkPopupWindow}.
 *
 * @author r.hartinger
 */
class ChunkPopupWindowTest
{

  private static Stream<Arguments> testPerformRollback()
  {
    return Stream.of(

        // content was removed, rollback will add this content back
        Arguments.of(new ChunkPopupWindow._RollbackInformation(112, 0, "    var x = 10;\n"),
                     "import { db,  result, vars } from \"@aditosoftware/jdito-types\";\n" +
                         "\n" +
                         "\n" +
                         "if(!vars.get(\"$param.IgnoreOrderBy_param\"))\n" +
                         "{\n" +
                         "    result.object({\n" +
                         "        \"ORGANISATION.NAME\": db.ASCENDING\n" +
                         "        });\n" +
                         "}"),


        // content was changed, rollback will reset the given content
        Arguments.of(new ChunkPopupWindow._RollbackInformation(66, 41, "if(!vars.get(\"$param.IgnoreOrderBy_param\"))")
            , "import { db,  result, vars } from \"@aditosoftware/jdito-types\";\n" +
                         "\n" +
                         "\n" +
                         "if(!vars.get(\"$param.AttributeId_param\"))\n" +
                         "{\n" +
                         "    var x = 10;\n" +
                         "    result.object({\n" +
                         "        \"ORGANISATION.NAME\": db.ASCENDING\n" +
                         "        });\n" +
                         "}"
        ),

        // content was added, rollback will remove the content
        Arguments.of(new ChunkPopupWindow._RollbackInformation(128, 19, ""), "import { db,  result, vars } from \"@aditosoftware/jdito-types\";\n" +
            "\n" +
            "\n" +
            "if(!vars.get(\"$param.IgnoreOrderBy_param\"))\n" +
            "{\n" +
            "    var x = 10;\n" +
            "    var y = x + 2;\n" +
            "    result.object({\n" +
            "        \"ORGANISATION.NAME\": db.ASCENDING\n" +
            "        });\n" +
            "}")
    );
  }


  /**
   * Tests the actual rollback of a given text. The expected is the same in all the test cases.
   *
   * @param pRollbackInformation the rollback information. There are three cases in this test: added content, changed content and removed content
   * @param pCurrentEditorText   the current text of the editor
   * @see ChunkPopupWindow#_performRollback(ChunkPopupWindow._RollbackInformation, JTextComponent)
   */
  @ParameterizedTest
  @MethodSource
  void testPerformRollback(ChunkPopupWindow._RollbackInformation pRollbackInformation, String pCurrentEditorText)
  {
    String expected = "import { db,  result, vars } from \"@aditosoftware/jdito-types\";\n" +
        "\n" +
        "\n" +
        "if(!vars.get(\"$param.IgnoreOrderBy_param\"))\n" +
        "{\n" +
        "    var x = 10;\n" +
        "    result.object({\n" +
        "        \"ORGANISATION.NAME\": db.ASCENDING\n" +
        "        });\n" +
        "}";

    JTextComponent jTextComponent = new JEditorPane("text/plain", pCurrentEditorText);

    ChunkPopupWindow chunkPopupWindow = Mockito.mock(ChunkPopupWindow.class);
    Mockito.doCallRealMethod().when(chunkPopupWindow)._performRollback(any(), any());

    chunkPopupWindow._performRollback(pRollbackInformation, jTextComponent);

    assertEquals(expected, jTextComponent.getText());
  }


  private static Stream<Arguments> testGetAffectedContents()
  {
    String expectedNormal = "second line.\nthird line.\n";
    String expectedModify = "second line.\nthird line.";

    String windowsLines = _createLines(ELineEnding.WINDOWS.getLineEnding());
    String unixLines = _createLines(ELineEnding.UNIX.getLineEnding());
    String macLines = _createLines(ELineEnding.MAC.getLineEnding());

    // building the arguments. Each EChangeType will result in three Arguments with the content for windows, unix and mac file ending.
    List<Arguments> arguments = new ArrayList<>();
    Arrays.stream(EChangeType.values()).forEach(pEChangeType -> {
      // expected for ChangeType modify is different from all the other ChangeTypes
      String expected = pEChangeType.equals(EChangeType.MODIFY) ? expectedModify : expectedNormal;

      arguments.add(Arguments.of(expected, pEChangeType, windowsLines));
      arguments.add(Arguments.of(expected, pEChangeType, unixLines));
      arguments.add(Arguments.of(expected, pEChangeType, macLines));
    });

    return arguments.stream();
  }

  /**
   * Tests the finding of the affected content.
   *
   * @param pExpected    the expected text that the method returns
   * @param pEChangeType the ChangeType that is in EChangeType
   * @param pContents    the whole contents of the file
   * @see ChunkPopupWindow#_getAffectedContents(String, IChangeDelta)
   */
  @ParameterizedTest
  @MethodSource
  void testGetAffectedContents(String pExpected, EChangeType pEChangeType, String pContents)
  {
    IChangeDelta changeDelta = Mockito.mock(IChangeDelta.class);
    Mockito.doReturn(1).when(changeDelta).getStartLine(EChangeSide.OLD);
    Mockito.doReturn(3).when(changeDelta).getEndLine(EChangeSide.OLD);
    Mockito.doReturn(pEChangeType).when(changeDelta).getChangeType();

    ChunkPopupWindow chunkPopupWindow = Mockito.mock(ChunkPopupWindow.class);
    Mockito.doCallRealMethod().when(chunkPopupWindow)._getAffectedContents(any(), any());

    assertEquals(pExpected, chunkPopupWindow._getAffectedContents(pContents, changeDelta));

    Mockito.verify(changeDelta).getStartLine(EChangeSide.OLD);
    Mockito.verify(changeDelta, Mockito.times(3)).getEndLine(EChangeSide.OLD);
    Mockito.verify(changeDelta).getChangeType();
    Mockito.verifyNoMoreInteractions(changeDelta);
  }

  /**
   * Utility method for creating five example lines. The line ending is depending on the given line ending
   * <p>
   * These lines look as the following: <br/>
   * <tt>
   * first line.<br/>
   * second line.<br/>
   * third line.<br/>
   * forth line.<br/>
   * fifth line.<br/>
   * </tt>
   *
   * @param pLineEnding the line ending that each of the lines should have
   * @return five example lines
   */
  private static String _createLines(String pLineEnding)
  {
    StringBuilder lines = new StringBuilder();
    lines.append("first line.").append(pLineEnding);
    lines.append("second line.").append(pLineEnding);
    lines.append("third line.").append(pLineEnding);
    lines.append("forth line.").append(pLineEnding);
    lines.append("fifth line.").append(pLineEnding);
    return lines.toString();
  }

  /**
   * Tests if the runtime exception is thrown if there is an inner AditoGitException
   *
   * @see ChunkPopupWindow#_calculateRollbackInfo(IRepository, IChangeDelta, File)
   */
  @Test
  void testCalculateRollbackInfoAditoGitException() throws AditoGitException
  {
    IRepository repository = Mockito.mock(IRepository.class);
    Mockito.doThrow(new AditoGitException("junit")).when(repository).getCommit(null);

    IChangeDelta changeDelta = Mockito.mock(IChangeDelta.class);

    File file = new File("");

    ChunkPopupWindow chunkPopupWindow = Mockito.mock(ChunkPopupWindow.class);
    Mockito.doCallRealMethod().when(chunkPopupWindow)._calculateRollbackInfo(any(), any(), any());

    RuntimeException actual = assertThrows(RuntimeException.class, () -> chunkPopupWindow._calculateRollbackInfo(repository, changeDelta, file), "method call should throw an runtime exception");

    // check cause if it is an AditoGitException
    Throwable cause = actual.getCause();
    assertEquals(AditoGitException.class, cause.getClass(), "cause should be the thrown AditoGitException");
    assertEquals("junit", cause.getMessage(), "message should be junit");
  }

  /**
   * Tests if the runtime exception is thrown if there is an inner IOException
   *
   * @see ChunkPopupWindow#_calculateRollbackInfo(IRepository, IChangeDelta, File)
   */
  @Test
  void testCalculateRollbackInfoIOException() throws AditoGitException, IOException
  {
    ICommit commit = Mockito.mock(ICommit.class);

    IRepository repository = Mockito.mock(IRepository.class);
    Mockito.doReturn(commit).when(repository).getCommit(null);
    Mockito.doReturn(new File("")).when(repository).getTopLevelDirectory();
    Mockito.doThrow(new IOException("junit")).when(repository).getFileVersion(any(), any());

    IChangeDelta changeDelta = Mockito.mock(IChangeDelta.class);

    File file = new File("");

    ChunkPopupWindow chunkPopupWindow = Mockito.mock(ChunkPopupWindow.class);
    Mockito.doCallRealMethod().when(chunkPopupWindow)._calculateRollbackInfo(any(), any(), any());

    RuntimeException actual = assertThrows(RuntimeException.class, () -> chunkPopupWindow._calculateRollbackInfo(repository, changeDelta, file), "method call should throw an runtime exception");

    // check cause if it is an IOException
    Throwable cause = actual.getCause();
    assertEquals(IOException.class, cause.getClass(), "cause should be the thrown IOException");
    assertEquals("junit", cause.getMessage(), "message should be junit");
  }

  private static Stream<Arguments> testCalculateRollbackInfo()
  {
    return Stream.of(

        // added content
        Arguments.of(new ChunkPopupWindow._RollbackInformation(12, 36, "secne."),
                     _createChangeDelta(1, 2, 12, 49, EChangeType.MODIFY),
                     "first line.\nsecne.\nfifth line."),

        // removed content
        Arguments.of(new ChunkPopupWindow._RollbackInformation(12, 6, "second line.\nthird line.\nforth line."),
                     _createChangeDelta(1, 4, 12, 19, EChangeType.MODIFY),
                     "first line.\nsecond line.\nthird line.\nforth line.\nfifth line."),

        // changed content
        Arguments.of(new ChunkPopupWindow._RollbackInformation(12, 36, "second line.\nthird line.\nforth line."),
                     _createChangeDelta(1, 4, 12, 49, EChangeType.MODIFY),
                     "first line.\nsecond line.\nthird line.\nforth line.\nfifth line."),

        // added a new line
        Arguments.of(new ChunkPopupWindow._RollbackInformation(19, 1, ""),
                     _createChangeDelta(2, 2, 19, 20, EChangeType.ADD),
                     "first line.\nsecne.\nfifth line."),

        // deleted a line
        Arguments.of(new ChunkPopupWindow._RollbackInformation(12, 0, "secne.\n"),
                     _createChangeDelta(1, 2, 12, 12, EChangeType.DELETE),
                     "first line.\nsecne.\nfifth line.")

    );
  }

  /**
   * Tests if the rollback information is calculated correctly.
   *
   * @param pExpected    the expected rollback information
   * @param pChangeDelta the change delta
   * @param pContent     the content of the supplier of {@link IFileContentInfo}
   * @see ChunkPopupWindow#_calculateRollbackInfo(IRepository, IChangeDelta, File)
   */
  @ParameterizedTest
  @MethodSource
  void testCalculateRollbackInfo(ChunkPopupWindow._RollbackInformation pExpected, IChangeDelta pChangeDelta, String pContent) throws AditoGitException, IOException
  {
    IFileContentInfo fileContentInfo = new FileContentInfoImpl(() -> pContent, () -> StandardCharsets.UTF_8);

    ICommit commit = Mockito.mock(ICommit.class);

    IRepository repository = Mockito.mock(IRepository.class);
    Mockito.doReturn(commit).when(repository).getCommit(null);
    Mockito.doReturn(new File("")).when(repository).getTopLevelDirectory();
    Mockito.doReturn("").when(repository).getFileVersion(any(), any());

    Mockito.doReturn(fileContentInfo).when(repository).getFileContents(any());

    File file = new File("");

    ChunkPopupWindow chunkPopupWindow = Mockito.mock(ChunkPopupWindow.class);
    Mockito.doCallRealMethod().when(chunkPopupWindow)._calculateRollbackInfo(any(), any(), any());
    Mockito.doCallRealMethod().when(chunkPopupWindow)._getAffectedContents(any(), any());

    ChunkPopupWindow._RollbackInformation actual = chunkPopupWindow._calculateRollbackInfo(repository, pChangeDelta, file);

    assertEquals(pExpected, actual);
  }

  /**
   * Utility method to create a simple {@link IChangeDelta}. This created object will be a Mock. Just the five methods mentioned below have return values,
   * all the other values have the default Mock behaviour.
   *
   * @param startLineOld      the value that should be returned when calling {@code getStartLine(EChangeSide.OLD)}
   * @param endLineOld        the value that should be returned when calling {@code getEndLine(EChangeSide.OLD)}
   * @param startTextIndexNew the value that should be returned when calling {@code getStartTextIndex(EChangeSide.NEW)}
   * @param endTextIndexNew   the value that should be returned when calling {@code getEndTextIndex(EChangeSide.NEW)}
   * @param changeType        the value that should be returned when calling {@code getChangeType()}
   * @return the created mock object
   */
  private static IChangeDelta _createChangeDelta(int startLineOld, int endLineOld, int startTextIndexNew, int endTextIndexNew, EChangeType changeType)
  {
    IChangeDelta changeDelta = Mockito.mock(IChangeDelta.class);
    Mockito.doReturn(startLineOld).when(changeDelta).getStartLine(EChangeSide.OLD);
    Mockito.doReturn(endLineOld).when(changeDelta).getEndLine(EChangeSide.OLD);
    Mockito.doReturn(startTextIndexNew).when(changeDelta).getStartTextIndex(EChangeSide.NEW);
    Mockito.doReturn(endTextIndexNew).when(changeDelta).getEndTextIndex(EChangeSide.NEW);
    Mockito.doReturn(changeType).when(changeDelta).getChangeType();
    return changeDelta;
  }


}