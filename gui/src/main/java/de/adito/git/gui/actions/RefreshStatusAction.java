package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 11.03.2019
 */
public class RefreshStatusAction extends AbstractAction
{

  private final Observable<Optional<IRepository>> repository;

  @Inject
  public RefreshStatusAction(IIconLoader pIconLoader, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super("refresh");
    repository = pRepository;
    putValue(Action.SMALL_ICON, pIconLoader.getIcon(Constants.REFRESH_CONTENT_ICON));
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    repository.blockingFirst().ifPresent(IRepository::refreshStatus);
  }
}
