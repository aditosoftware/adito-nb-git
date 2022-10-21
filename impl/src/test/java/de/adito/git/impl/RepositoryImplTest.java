package de.adito.git.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

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
      RepositoryImpl repository = Mockito.mock(RepositoryImpl.class);
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
    RepositoryImpl repository = Mockito.mock(RepositoryImpl.class);
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

    RepositoryImpl repository = Mockito.mock(RepositoryImpl.class);
    Mockito.doCallRealMethod().when(repository).deleteLockedIndexFile();
    Mockito.doReturn(created.toFile()).when(repository)._getIndexLockFile();

    assertTrue(created.toFile().exists(), "file exists before delete");

    repository.deleteLockedIndexFile();

    assertFalse(created.toFile().exists(), "file does not exists after delete");
  }
}