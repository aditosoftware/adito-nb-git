package de.adito.git.gui.dialogs;

import java.util.Collection;

/**
 * Wrapper for a part of the NetBeans Lookup
 *
 * @author m.kaspera, 16.05.2022
 */
public interface LookupProvider
{

  <TYPE> TYPE lookup(Class<TYPE> pType);

  <TYPE> Collection<TYPE> lookupAll(Class<TYPE> pType);

}
