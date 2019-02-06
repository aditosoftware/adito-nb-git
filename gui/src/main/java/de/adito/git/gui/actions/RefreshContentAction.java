package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action that executes a passed Runnable in the actionPerformed method, has a refresh icon as symbol
 *
 * @author m.kaspera, 06.02.2019
 */
class RefreshContentAction extends AbstractAction
{

  private final Runnable refreshCallBack;

  @Inject
  RefreshContentAction(IIconLoader pIconLoader, @Assisted Runnable pRefreshCallBack)
  {
    super("refresh");
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.REFRESH_CONTENT_ICON));
    refreshCallBack = pRefreshCallBack;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    refreshCallBack.run();
  }
}
