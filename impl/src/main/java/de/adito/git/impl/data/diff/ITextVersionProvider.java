package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeSide;

/**
 * Provides a version of the text depending on the passed side
 *
 * @author m.kaspera, 27.02.2020
 */
interface ITextVersionProvider
{

  /**
   * @param pChangeSide which version should be provided
   * @return text of the passed version
   */
  String getVersion(EChangeSide pChangeSide);

}
