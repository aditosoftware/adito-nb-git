package de.adito.git.api.data;

/**
 * Holds information about a Tag, such as the name of the tag and the location it points to
 *
 * @author m.kaspera, 28.01.2019
 */
public interface ITag
{

  /**
   * @return the name of the Tag
   */
  String getName();

  /**
   * @return the identifier of the commit the tag points to
   */
  String getId();

}
