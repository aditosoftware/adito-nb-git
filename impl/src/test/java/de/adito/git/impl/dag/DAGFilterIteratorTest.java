package de.adito.git.impl.dag;

import de.adito.git.api.dag.IDAGObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author m.kaspera, 22.05.2019
 */
class DAGFilterIteratorTest
{

  @Test
  void testDAGFilterIterator()
  {
    List<Integer> objectNums = List.of(22, 8, 9, 12, 3, 10, 3, 2, 5);
    List<List<Integer>> parentsList = List.of(List.of(1), List.of(2, 3), List.of(4), List.of(6), List.of(5), List.of(6), List.of(7), List.of());
    Iterator<_DAGTestObj> iterator = _createDAG(objectNums, parentsList);
    _testFilter(iterator, pDAGTestObj -> pDAGTestObj.getNum() % 2 == 0);
  }

  @Test
  void testDAGFilterIterator2()
  {
    List<Integer> objectNums = List.of(22, 8, 9, 12, 7, 10, 3, 2, 5);
    List<List<Integer>> parentsList = List.of(List.of(1), List.of(2, 3), List.of(4), List.of(6), List.of(5), List.of(6), List.of(7), List.of());
    Iterator<_DAGTestObj> iterator = _createDAG(objectNums, parentsList);
    _testFilter(iterator, pDAGTestObj -> pDAGTestObj.getNum() >= 10);
  }

  @Test
  void testDAGFilterIterator3()
  {
    List<Integer> objectNums = List.of(13, 8, 9, 12, 3, 10, 19, 20, 7);
    List<List<Integer>> parentsList = List.of(List.of(1), List.of(2, 3), List.of(4), List.of(4, 5), List.of(5, 6, 7), List.of(8), List.of(7), List.of(8), List.of());
    Iterator<_DAGTestObj> iterator = _createDAG(objectNums, parentsList);
    _testFilter(iterator, pDAGTestObj -> pDAGTestObj.getNum() >= 10);
  }

  /**
   * This method does the actual testing, the test methods here are only setup
   *
   * @param pIterator  Iterator to create the DAGFilterIterator from
   * @param pPredicate pPredicate used for the DAGFilterIterator and checking if the DAGFilterIterator works
   */
  private void _testFilter(Iterator<_DAGTestObj> pIterator, Predicate<_DAGTestObj> pPredicate)
  {
    DAGFilterIterator<_DAGTestObj> filteredIterator = new DAGFilterIterator<>(pIterator, pPredicate);
    Assertions.assertTrue(filteredIterator.hasNext());
    while (filteredIterator.hasNext())
    {
      _DAGTestObj nextObj = filteredIterator.next();
      // all objects should pass the predicate test
      Assertions.assertTrue(pPredicate.test(nextObj));
      if (nextObj.getParents() != null)
      {
        // the same parent should not occur twice in the list of parents
        Assertions.assertEquals(new HashSet<>(nextObj.getParents()).size(), nextObj.getParents().size(), "duplicate parents in list");
        for (_DAGTestObj parent : nextObj.getParents())
        {
          // parents should also pass the predicate test
          Assertions.assertTrue(pPredicate.test(parent));
        }
      }
    }
  }

  /**
   * Test method to make sure the _createDAG helper method works the way it is supposed to be
   */
  @Test
  void testCreateDAG()
  {
    List<Integer> objectNums = List.of(5, 2, 3, 10, 3, 12, 9, 8, 22);
    List<List<Integer>> parentsList = List.of(List.of(), List.of(0), List.of(1), List.of(2), List.of(3), List.of(2), List.of(4), List.of(5, 6), List.of(7));
    Iterator<_DAGTestObj> iterator = _createDAG(objectNums, parentsList);
    int index = 0;
    while (iterator.hasNext())
    {
      _DAGTestObj nextObj = iterator.next();
      Assertions.assertEquals((int) objectNums.get(index), nextObj.getNum());
      if (nextObj.getParents() != null)
      {
        for (int parentIndex = 0; parentIndex < nextObj.getParents().size(); parentIndex++)
        {
          Assertions.assertEquals((int) objectNums.get(parentsList.get(index).get(parentIndex)), nextObj.getParents().get(parentIndex).getNum());
        }
      }
      index++;
    }
  }

  /**
   * Method that allows creation of a simple DAG without having to write code over and over again in each test setup
   * Which _DAGTestObj will get which number and which parent is determined via the passed lists and their indizes
   *
   * @param pTestObjNumbers Numbers each of the _DAGTestObj will have
   * @param pTestObjParents determine which _DAGTestObj will have which parent.
   * @return Iterator over the created DAG
   */
  private Iterator<_DAGTestObj> _createDAG(List<Integer> pTestObjNumbers, List<List<Integer>> pTestObjParents)
  {
    List<_DAGTestObj> testObjList = new ArrayList<>();
    for (Integer testNum : pTestObjNumbers)
    {
      testObjList.add(new _DAGTestObj(testNum, List.of()));
    }
    for (int index = 0; index < pTestObjParents.size(); index++)
    {
      List<_DAGTestObj> parents = new ArrayList<>();
      for (Integer parentIndex : pTestObjParents.get(index))
      {
        parents.add(testObjList.get(parentIndex));
      }
      testObjList.get(index).setParents(parents);
    }
    return testObjList.iterator();
  }

  /**
   * Simple test object that forms a basic IDAGObject that can easily be filtered
   */
  private static class _DAGTestObj implements IDAGObject<_DAGTestObj>
  {

    private int num;
    private List<_DAGTestObj> parents;

    _DAGTestObj(int pNum, List<_DAGTestObj> pParents)
    {
      num = pNum;
      parents = pParents;
    }

    @Override
    public List<_DAGTestObj> getParents()
    {
      return parents;
    }

    @Override
    public void setParents(List<_DAGTestObj> pParents)
    {
      parents = pParents;
    }

    int getNum()
    {
      return num;
    }
  }

}
