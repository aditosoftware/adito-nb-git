package de.adito.git.nbm;

import de.adito.git.api.IIgnoreFacade;
import de.adito.git.nbm.repo.ProjectRepositoryDescription;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * Tests for validating the function of the {@link FileSystemObserverImpl} class
 *
 * @author m.kaspera, 22.02.2023
 */
class FileSystemObserverImplTest
{
  private static Path tempDirectory;
  private static ProjectRepositoryDescription repositoryDescription;

  /**
   * Sets up the directory and RepositoryDescription that is used for the tests
   *
   * @throws IOException if the temporary directory used for the tests cannot be created
   */
  @BeforeAll
  static void beforeAll() throws IOException
  {
    tempDirectory = Files.createTempDirectory("fileSystemObserverImplTest");
    FileObject projectDir = FileUtil.toFileObject(tempDirectory.toFile());
    repositoryDescription = new ProjectRepositoryDescription(projectDir);
  }

  /**
   * Tests, if the correct listeners are added when creating a FileSystemObserver
   */
  @Test
  void isAddCorrectListeners()
  {
    try (MockedStatic<FileUtil> fileUtilMockedStatic = mockStatic(FileUtil.class))
    {
      ProjectRepositoryDescription repositoryDescription = initFileSystemObserver(fileUtilMockedStatic, false);

      fileUtilMockedStatic.verify(() -> FileUtil.addFileChangeListener(Mockito.any(), eq(new File(repositoryDescription.getPath(), ".git"))));
      fileUtilMockedStatic.verify(() -> FileUtil.addFileChangeListener(Mockito.any()));
    }
  }

  /**
   * Tests, if the listeners are discarded when the FileSystemObserver is discarded
   */
  @Test
  void isListenersRemovedOnDiscard()
  {
    try (MockedStatic<FileUtil> fileUtilMockedStatic = mockStatic(FileUtil.class))
    {
      ProjectRepositoryDescription repositoryDescription = initFileSystemObserver(fileUtilMockedStatic, true);

      fileUtilMockedStatic.verify(() -> FileUtil.removeFileChangeListener(Mockito.any(), eq(new File(repositoryDescription.getPath(), ".git"))));
      fileUtilMockedStatic.verify(() -> FileUtil.removeFileChangeListener(Mockito.any()));
    }
  }

  /**
   * Set up the FileSystemObserverImpl for the tests
   *
   * @param fileUtilMockedStatic MockedStatic of the FileUtil class
   * @param isDiscard            true if the FileSystemObserverImpl should be discarded after creation, false otherwise
   * @return ProjectRepositoryDescription that has the information about the project folder
   */
  @NotNull
  private static ProjectRepositoryDescription initFileSystemObserver(@NotNull MockedStatic<FileUtil> fileUtilMockedStatic, boolean isDiscard)
  {
    IIgnoreFacade ignoreFacade = mock(IIgnoreFacade.class);
    when(ignoreFacade.isIgnored(Mockito.any())).thenReturn(false);

    fileUtilMockedStatic.when(() -> FileUtil.toFileObject(tempDirectory.toFile())).thenReturn(mock(FileObject.class));

    FileSystemObserverImpl fileSystemObserver = new FileSystemObserverImpl(repositoryDescription, ignoreFacade);
    if (isDiscard)
      fileSystemObserver.discard();
    return repositoryDescription;
  }

}