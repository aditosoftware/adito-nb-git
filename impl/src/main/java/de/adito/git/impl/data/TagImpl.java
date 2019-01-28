package de.adito.git.impl.data;

import de.adito.git.api.data.ITag;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

/**
 * @author m.kaspera, 28.01.2019
 */
public class TagImpl implements ITag
{

  private static final String REF_TAG_START = "refs/tags/";
  private final Ref tagRef;
  private final String name;

  /**
   * @param pTagRef Ref that describes the tag
   */
  public TagImpl(Ref pTagRef)
  {
    tagRef = pTagRef;
    String rawName = tagRef.getName();
    if (rawName.startsWith(REF_TAG_START))
      rawName = rawName.substring(REF_TAG_START.length());
    name = rawName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId()
  {
    return ObjectId.toString(tagRef.getObjectId());
  }
}
