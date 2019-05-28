package de.adito.git.api.dag;

import java.util.List;

/**
 * Interface that each class that wants to use the DAGFilterIterator has to implement
 *
 * @author m.kaspera, 22.05.2019
 */
public interface IDAGObject<S extends IDAGObject>
{

  /**
   * @return get the current parents of this object
   */
  List<S> getParents();

  /**
   * @param pParents the new parents of this object
   */
  void setParents(List<S> pParents);

}
