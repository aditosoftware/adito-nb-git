package de.adito.git.gui.actions;

import de.adito.git.api.*;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;

/**
 * Test class for {@link GitIndexLockUtil}.
 *
 * @author r.hartinger
 */
class GitIndexLockUtilTest
{

  private static final String INDEX_LOCK_TITLE = "Git error: index.lock";
  private static final String INDEX_LOCK_TEXT = "<html>Git error: The index is locked.<br/>This might be due to a concurrent or crashed process. Do you want to delete the locked index file?<br/><b>Warning:</b> If you have any git processes running, please try to exit them first before deleting the index.lock file.</html>";

  /**
   * Test if the file does not exist.
   */
  @Test
  void checkAndHandleLockedIndexFileFileNotExist()
  {

    IRepository repository = Mockito.mock(IRepository.class);
    Mockito.doReturn(false).when(repository).checkForLockedIndexFile();

    IDialogProvider dialogProvider = Mockito.mock(IDialogProvider.class);

    INotifyUtil notifyUtil = Mockito.mock(INotifyUtil.class);

    GitIndexLockUtil.checkAndHandleLockedIndexFile(repository, dialogProvider, notifyUtil);

    // repository just called for checkForLockedIndexFile, and nothing else
    Mockito.verify(repository).checkForLockedIndexFile();
    Mockito.verifyNoMoreInteractions(repository);

    // no interactions with dialogProvider or notifyUtil
    Mockito.verifyNoInteractions(dialogProvider);
    Mockito.verifyNoInteractions(notifyUtil);
  }


  /**
   * Test if the file does exist and the user says no in the dialog.
   */
  @Test
  void checkAndHandleLockedIndexFileFileExistUserNo()
  {
    IRepository repository = Mockito.mock(IRepository.class);
    Mockito.doReturn(true).when(repository).checkForLockedIndexFile();

    IDialogProvider dialogProvider = _createDialogProviderMock(false);

    INotifyUtil notifyUtil = Mockito.mock(INotifyUtil.class);

    GitIndexLockUtil.checkAndHandleLockedIndexFile(repository, dialogProvider, notifyUtil);

    // repository just called for checkForLockedIndexFile, and nothing else
    Mockito.verify(repository).checkForLockedIndexFile();
    Mockito.verifyNoMoreInteractions(repository);

    // dialogProvider has shown its dialog
    Mockito.verify(dialogProvider).showYesNoDialog(INDEX_LOCK_TITLE, INDEX_LOCK_TEXT);
    Mockito.verifyNoMoreInteractions(dialogProvider);

    // no interactions with notifyUtil
    Mockito.verifyNoInteractions(notifyUtil);
  }


  /**
   * Test if the file does exist and the user says yes in the dialog, but deleting throws an error.
   */
  @Test
  void checkAndHandleLockedIndexFileFileExistUserYesError() throws IOException
  {
    IOException ioException = new IOException("junit");

    IRepository repository = Mockito.mock(IRepository.class);
    Mockito.doReturn(true).when(repository).checkForLockedIndexFile();
    Mockito.doThrow(ioException).when(repository).deleteLockedIndexFile();

    IDialogProvider dialogProvider = _createDialogProviderMock(true);

    INotifyUtil notifyUtil = Mockito.mock(INotifyUtil.class);

    GitIndexLockUtil.checkAndHandleLockedIndexFile(repository, dialogProvider, notifyUtil);

    Mockito.verify(repository).checkForLockedIndexFile();
    Mockito.verify(repository).deleteLockedIndexFile();
    Mockito.verifyNoMoreInteractions(repository);

    // dialogProvider has shown its dialog
    Mockito.verify(dialogProvider).showYesNoDialog(INDEX_LOCK_TITLE, INDEX_LOCK_TEXT);
    Mockito.verifyNoMoreInteractions(dialogProvider);

    // notifyUtil shows the error notification
    Mockito.verify(notifyUtil).notify(ioException, "Error deleting the index.lock file", true);
  }

  /**
   * Test if the file does exist and the user says yes in the dialog.
   */
  @Test
  void checkAndHandleLockedIndexFileFileExistUserYes() throws IOException
  {
    IRepository repository = Mockito.mock(IRepository.class);
    Mockito.doReturn(true).when(repository).checkForLockedIndexFile();

    IDialogProvider dialogProvider = _createDialogProviderMock(true);

    INotifyUtil notifyUtil = Mockito.mock(INotifyUtil.class);

    GitIndexLockUtil.checkAndHandleLockedIndexFile(repository, dialogProvider, notifyUtil);

    // repository just called for checkForLockedIndexFile, and nothing else
    Mockito.verify(repository).checkForLockedIndexFile();
    Mockito.verify(repository).deleteLockedIndexFile();
    Mockito.verifyNoMoreInteractions(repository);

    // dialogProvider has shown its dialog
    Mockito.verify(dialogProvider).showYesNoDialog(INDEX_LOCK_TITLE, INDEX_LOCK_TEXT);
    Mockito.verifyNoMoreInteractions(dialogProvider);

    // no interactions with notifyUtil
    Mockito.verifyNoInteractions(notifyUtil);
  }


  private static IDialogProvider _createDialogProviderMock(boolean okay)
  {
    IUserPromptDialogResult<?, ?> indexLockDialog = Mockito.mock(IUserPromptDialogResult.class);
    Mockito.doReturn(okay).when(indexLockDialog).isOkay();

    IDialogProvider dialogProvider = Mockito.mock(IDialogProvider.class);
    Mockito.doReturn(indexLockDialog).when(dialogProvider).showYesNoDialog(INDEX_LOCK_TITLE, INDEX_LOCK_TEXT);
    return dialogProvider;
  }

}