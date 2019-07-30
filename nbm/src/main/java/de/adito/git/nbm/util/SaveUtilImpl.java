package de.adito.git.nbm.util;

import de.adito.git.api.ISaveUtil;
import org.netbeans.api.actions.Savable;
import org.openide.loaders.DataObject;

import java.io.IOException;

/**
 * Implements ISaveUtil with tools/code from Netbeans
 *
 * @author m.kaspera, 30.07.2019
 */
public class SaveUtilImpl implements ISaveUtil
{
  /**
   * @inheritDoc
   */
  @Override
  public void saveUnsavedFiles()
  {
    try
    {
      for (Savable savable : Savable.REGISTRY.lookupAll(Savable.class))
        savable.save();
      // Old implementations may sometimes only be referenced in 'DataObject.getRegistry()'.
      for (DataObject dataObject : DataObject.getRegistry().getModifiedSet())
        for (Savable savable : dataObject.getLookup().lookupAll(Savable.class))
          savable.save();
    }
    catch (IOException pE)
    {
      // do nothing
    }
  }
}
