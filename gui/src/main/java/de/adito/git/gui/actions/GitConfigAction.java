package de.adito.git.gui.actions;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.impl.data.SSHKeyDetails;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 24.12.2018
 */
class GitConfigAction extends AbstractTableAction
{

  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final IKeyStore keyStore;

  @Inject
  public GitConfigAction(IDialogProvider pDialogProvider, IKeyStore pKeyStore, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super("Settings", _getIsEnabledObservable(pRepository));
    keyStore = pKeyStore;
    putValue(Action.SMALL_ICON, Constants.GIT_CONFIG_ICON);
    repository = pRepository;
    dialogProvider = pDialogProvider;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    DialogResult<?, Multimap<String, Object>> dialogResult = dialogProvider.showGitConfigDialog(repository);
    if (dialogResult.isPressedOk())
    {
      // only set sshKeyLocation for now since that is the only supported setting (for now)
      for (Object obj : dialogResult.getInformation().get(Constants.SSH_KEY_KEY))
      {
        SSHKeyDetails value = (SSHKeyDetails) obj;
        if (value.getKeyLocation() != null)
        {
          repository.blockingFirst().ifPresent(pRepo -> pRepo.getConfig().setSshKeyLocation(value.getKeyLocation(), value.getRemoteName()));
          if (value.getPassPhrase() != null)
          {
            keyStore.save(value.getKeyLocation(), value.getPassPhrase(), null);
          }
          value.nullifyPassphrase();
        }
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository)
  {
    return pRepository.map(pRepo -> Optional.of(pRepo.isPresent()));
  }
}
