package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author m.kaspera, 06.06.2019
 */
public class ShowTagWindowAction extends AbstractAction
{

  private final IDialogProvider dialogProvider;
  private final Consumer<ICommit> selectCommitCallback;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  public ShowTagWindowAction(IDialogProvider pDialogProvider, IIconLoader pIIconLoader, @Assisted Consumer<ICommit> pSelectCommitCallback,
                             @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super("Show Tag Overview");
    selectCommitCallback = pSelectCommitCallback;
    repository = pRepository;
    putValue(SMALL_ICON, pIIconLoader.getIcon(Constants.SHOW_TAGS_ACTION_ICON));
    dialogProvider = pDialogProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    dialogProvider.showTagOverviewDialog(selectCommitCallback, repository);
  }
}
