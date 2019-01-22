package de.adito.git.nbm;

import com.google.inject.Singleton;
import de.adito.git.api.IUserPreferences;

/**
 * @author m.kaspera 23.11.2018
 */
@Singleton
public class UserPreferencesNBImpl implements IUserPreferences
{

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumLoadAdditionalCHEntries()
  {
    return 1000;
  }
}
