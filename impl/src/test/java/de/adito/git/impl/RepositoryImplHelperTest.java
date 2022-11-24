package de.adito.git.impl;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link RepositoryImplHelper}.
 *
 * @author m.kaspera, 24.11.2022
 */
class RepositoryImplHelperTest
{

  /**
   * Test functionality of the addCommitByTime method
   */
  @Nested
  class addCommitByTime
  {

    /**
     * Test the case of an empty list as commitList. The commit to insert should be the only element in the list afterwards
     */
    @Test
    void emptyList()
    {
      List<RevCommit> commitList = new ArrayList<>();
      RevCommit toInsert = getMockCommit(20);
      RepositoryImplHelper.addCommitByTime(toInsert, commitList);
      assertEquals(1, commitList.size());
      assertEquals(toInsert, commitList.get(0));
    }

    /**
     * Test if a commit, that is older than all entries in the exising list, is added at the end of the list
     */
    @Test
    void addAtEnd()
    {
      List<RevCommit> commitList = new ArrayList<>(List.of(getMockCommit(30), getMockCommit(20), getMockCommit(10)));
      RevCommit toInsert = getMockCommit(5);
      RepositoryImplHelper.addCommitByTime(toInsert, commitList);
      assertEquals(4, commitList.size());
      assertEquals(toInsert, commitList.get(3));
    }

    /**
     * Test if a commit, that sits in the middle of the other commits (commit-time wise), is added in the middle
     */
    @Test
    void addInMiddle()
    {
      List<RevCommit> commitList = new ArrayList<>(List.of(getMockCommit(40), getMockCommit(30), getMockCommit(20), getMockCommit(10)));
      RevCommit toInsert = getMockCommit(25);
      RepositoryImplHelper.addCommitByTime(toInsert, commitList);
      assertEquals(5, commitList.size());
      assertEquals(toInsert, commitList.get(2));
    }

    /**
     * Tests if a commit, that has the latest commit time out of all commits in the list, is added in the very front of the list
     */
    @Test
    void addInFront()
    {
      List<RevCommit> commitList = new ArrayList<>(List.of(getMockCommit(40), getMockCommit(30), getMockCommit(20), getMockCommit(10)));
      RevCommit toInsert = getMockCommit(50);
      RepositoryImplHelper.addCommitByTime(toInsert, commitList);
      assertEquals(5, commitList.size());
      assertEquals(toInsert, commitList.get(0));
    }

    /**
     * Tests that the commit is inserted at the end of the list if all commits have the same commit time
     */
    @Test
    void addAllEqual()
    {
      List<RevCommit> commitList = new ArrayList<>(List.of(getMockCommit(10), getMockCommit(10), getMockCommit(10), getMockCommit(10)));
      RevCommit toInsert = getMockCommit(10);
      RepositoryImplHelper.addCommitByTime(toInsert, commitList);
      assertEquals(5, commitList.size());
      assertEquals(toInsert, commitList.get(4));
    }

  }

  /**
   * create a new mocked commit, with the given parameters mocked
   *
   * @param pCommitTime value that should be returned when the commit time of the commit is asked
   * @return mocked commit
   */
  static RevCommit getMockCommit(int pCommitTime)
  {
    RevCommit mockCommit = mock(RevCommit.class);
    Mockito.when(mockCommit.getCommitTime()).thenReturn(pCommitTime);
    Mockito.when(mockCommit.toString()).thenReturn("Mock commit with commit time " + pCommitTime);
    return mockCommit;
  }
}