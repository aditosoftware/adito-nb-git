package de.adito.git.nbm.quickSearch;

import de.adito.git.api.IQuickSearch;
import de.adito.git.api.IQuickSearchProvider;
import org.openide.awt.QuickSearch;

import javax.swing.*;

/**
 * @author m.kaspera, 06.02.2019
 */
public class QuickSearchProviderImpl implements IQuickSearchProvider
{
  @Override
  public IQuickSearch attach(JComponent pComponent, Object pConstraints, IQuickSearch.ICallback pCallback)
  {
    return new QuickSearchImpl(QuickSearch.attach(pComponent, pConstraints, new QuickSearchCallbackImpl(pCallback)));
  }

  class QuickSearchCallbackImpl implements QuickSearch.Callback
  {

    private final IQuickSearch.ICallback quickSearchCallback;

    QuickSearchCallbackImpl(IQuickSearch.ICallback pQuickSearchCallback)
    {
      quickSearchCallback = pQuickSearchCallback;
    }

    @Override
    public void quickSearchUpdate(String pSearchText)
    {
      quickSearchCallback.quickSearchUpdate(pSearchText);
    }

    @Override
    public void showNextSelection(boolean pForward)
    {
      quickSearchCallback.showNextSelection(pForward);
    }

    @Override
    public String findMaxPrefix(String pPrefix)
    {
      return quickSearchCallback.findMaxPrefix(pPrefix);
    }

    @Override
    public void quickSearchConfirmed()
    {
      quickSearchCallback.quickSearchConfirmed();
    }

    @Override
    public void quickSearchCanceled()
    {
      quickSearchCallback.quickSearchCancelled();
    }
  }
}
