package de.adito.git.nbm.dialogs;

import de.adito.git.gui.dialogs.LookupProvider;
import org.openide.util.Lookup;

import java.util.Collection;

/**
 * Wraps the default NetBeans Lookup
 *
 * @author m.kaspera, 16.05.2022
 */
public class LookupProviderNbImpl implements LookupProvider
{
  @Override
  public <TYPE> TYPE lookup(Class<TYPE> pType)
  {
    return Lookup.getDefault().lookup(pType);
  }

  @Override
  public <TYPE> Collection<TYPE> lookupAll(Class<TYPE> pType)
  {
    //noinspection unchecked
    return (Collection<TYPE>) Lookup.getDefault().lookupAll(pType);
  }
}
