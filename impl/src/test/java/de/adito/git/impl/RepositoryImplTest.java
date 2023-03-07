package de.adito.git.impl;

import de.adito.git.api.*;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.IRepositoryDescription;
import de.adito.git.impl.data.FileStatusImpl;
import de.adito.git.impl.data.IDataFactory;
import de.adito.git.impl.ssh.ISshProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link RepositoryImpl}.
 *
 * @author r.hartinger
 */
class RepositoryImplTest
{

  /**
   * Tests if an index file exists. In this case a temporary file is created, so there will be always a temporary file.
   * After the test run, this temporary file is deleted.
   *
   * @throws IOException error while creating or deleting the file
   */
  @Test
  void checkForLockedIndexFileExisitingFile() throws IOException
  {
    // creates a temporary file, so a file exisits for checking
    Path created = Files.createTempFile("index", ".lock");
    try
    {
      RepositoryImpl repository = mock(RepositoryImpl.class);
      Mockito.doCallRealMethod().when(repository).checkForLockedIndexFile();
      Mockito.doReturn(created.toFile()).when(repository)._getIndexLockFile();

      assertTrue(repository.checkForLockedIndexFile());
    }
    finally
    {
      // in every case delete the temporary file
      Files.delete(created);
    }
  }

  /**
   * Tests if an index file does not exist. In this case a non-existent file is passed in the {@link RepositoryImpl#_getIndexLockFile()}.
   */
  @Test
  void checkForLockedIndexFileNotExisitingFile()
  {
    RepositoryImpl repository = mock(RepositoryImpl.class);
    Mockito.doCallRealMethod().when(repository).checkForLockedIndexFile();
    Mockito.doReturn(new File("not_existing" + System.currentTimeMillis())).when(repository)._getIndexLockFile();

    assertFalse(repository.checkForLockedIndexFile());
  }

  /**
   * Tests if an exisiting file is correctly deleted.
   *
   * @throws IOException error while deleting the file
   */
  @Test
  void deleteLockedIndexFile() throws IOException
  {
    Path created = Files.createTempFile("index", ".lock");

    RepositoryImpl repository = mock(RepositoryImpl.class);
    Mockito.doCallRealMethod().when(repository).deleteLockedIndexFile();
    Mockito.doReturn(created.toFile()).when(repository)._getIndexLockFile();

    assertTrue(created.toFile().exists(), "file exists before delete");

    repository.deleteLockedIndexFile();

    assertFalse(created.toFile().exists(), "file does not exists after delete");
  }

  /**
   * Tests, if the status calls of the observable that monitors the status are executed in the correct thread
   *
   * @throws IOException          if the RepositoryImpl cannot be properly created
   * @throws InterruptedException if the sleep is interrupted while waiting for the fileSystemChange to propagate
   */
  @Test
  void isStatusCallExecutedInStatusThread() throws InterruptedException, IOException
  {
    List<IFileSystemChangeListener> listeners = new ArrayList<>();
    List<String> threadNames = new ArrayList<>();

    IFileSystemObserver fileSystemObserver = new FileSystemObserverDummy(listeners);
    IFileSystemObserverProvider fileSystemObserverProvider = mock(IFileSystemObserverProvider.class);
    when(fileSystemObserverProvider.getFileSystemObserver(Mockito.any(), Mockito.any())).thenReturn(fileSystemObserver);

    AtomicReference<MockedStatic<RepositoryImplHelper>> statusThreadHelperMock = new AtomicReference<>();
    try (MockedStatic<RepositoryImplHelper> repositoryImplHelperMockedStatic = mockStatic(RepositoryImplHelper.class);
         MockedStatic<GitAttributesChecker> gitAttributesCheckerStatic = mockStatic(GitAttributesChecker.class))
    {
      mockStatusCall(repositoryImplHelperMockedStatic, threadNames);
      gitAttributesCheckerStatic.when(() -> GitAttributesChecker.compareToDefault(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenAnswer((Answer<Void>) invocation -> null);
      RepositoryImpl repository = new RepositoryImpl(fileSystemObserverProvider, mock(IUserInputPrompt.class), mock(IFileSystemUtil.class), mock(ISshProvider.class),
                                                     mock(IDataFactory.class), mock(IStandAloneDiffProvider.class), mock(IRepositoryDescription.class));

      // apply the static mock in the Git-status-computation thread, otherwise the test will not work
      repository.getGitStatusScheduler().scheduleDirect(() -> {
        MockedStatic<RepositoryImplHelper> gitStatusThreadMock = mockStatic(RepositoryImplHelper.class);
        mockStatusCall(gitStatusThreadMock, threadNames);
        statusThreadHelperMock.set(gitStatusThreadMock);
      });

      // set up a subscription on the status observable
      repository.getStatus().subscribe();

      // trigger a change
      listeners.forEach(IFileSystemChangeListener::fileSystemChange);

      // wait for a bit so the fileSystemChange can propagate to the observable
      Thread.sleep(1000);

      // the status calls are done in main and Git-status computation, because the first status call happens on the startWith part of the Observable, which is
      // calculated synchronously when the observable is created on the first call to getStatus()
      assertEquals(List.of("main", "Git-status-computation"), threadNames);
      // clean up the static mock in the Git-status-computation thread
      statusThreadHelperMock.get().close();
    }
  }

  /**
   * Mocks the status calls in the RepositoryImplHelper such that they return a mocked FileStatusImpl object and log the name of the thread the method was invoked in
   *
   * @param pRepoHelperMock MockedStatic of the RepositoryImplHelper
   * @param pThreadNames    List with the thread names in which the status call was executed
   */
  private static void mockStatusCall(@NotNull MockedStatic<RepositoryImplHelper> pRepoHelperMock, @NotNull List<String> pThreadNames)
  {
    pRepoHelperMock.when(() -> RepositoryImplHelper.status(Mockito.any())).thenAnswer((Answer<IFileStatus>) invocation -> {
      pThreadNames.add(Thread.currentThread().getName());
      return mock(FileStatusImpl.class);
    });
  }

  /**
   * Dummy that only adds the listener to the passed list if addListener is invoked. Does nothing else otherwise
   */
  private static class FileSystemObserverDummy implements IFileSystemObserver
  {

    @NotNull
    private final List<IFileSystemChangeListener> changeListeners;

    public FileSystemObserverDummy(@NotNull List<IFileSystemChangeListener> pChangeListeners)
    {
      changeListeners = pChangeListeners;
    }

    @Override
    public void discard()
    {

    }

    @Override
    public void addListener(IFileSystemChangeListener pChangeListener)
    {
      changeListeners.add(pChangeListener);
    }

    @Override
    public void removeListener(IFileSystemChangeListener pToRemove)
    {

    }

    @Override
    public void fireChange()
    {

    }
  }
}