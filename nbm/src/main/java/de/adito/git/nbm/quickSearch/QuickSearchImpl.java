package de.adito.git.nbm.quickSearch;

import de.adito.git.api.IQuickSearch;
import org.openide.awt.QuickSearch;

/**
 * @author m.kaspera, 06.02.2019
 */
class QuickSearchImpl implements IQuickSearch
{


  private final QuickSearch referee;

  QuickSearchImpl(QuickSearch pReferee)
  {
    referee = pReferee;
  }

  @Override
  public void setEnabled(boolean pEnabled)
  {
    referee.setEnabled(pEnabled);
  }

  @Override
  public boolean isEnabled()
  {
    return referee.isEnabled();
  }

  @Override
  public void setAlwaysShown(boolean pIsAlwaysShown)
  {
    referee.setAlwaysShown(pIsAlwaysShown);
  }

  @Override
  public boolean isAlwaysShown()
  {
    return referee.isAlwaysShown();
  }

  @Override
  public void detach()
  {
    referee.detach();
  }
}
