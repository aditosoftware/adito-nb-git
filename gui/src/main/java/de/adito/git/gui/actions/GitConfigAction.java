package de.adito.git.gui.actions;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.impl.data.SSHKeyDetails;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.*;

/**
 * @author m.kaspera, 24.12.2018
 */
class GitConfigAction extends AbstractTableAction
{

  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final IPrefStore prefStore;
  private final IKeyStore keyStore;

  @Inject
  public GitConfigAction(IDialogProvider pDialogProvider, IPrefStore pPrefStore, IKeyStore pKeyStore, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super("Settings", _getIsEnabledObservable(pRepository));
    prefStore = pPrefStore;
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
      Iterator<Object> logLevelIter = dialogResult.getInformation().get(Constants.LOG_LEVEL_SETTINGS_KEY).iterator();
      if (logLevelIter.hasNext())
      {
        Level setLevel = (Level) logLevelIter.next();
        prefStore.put(Constants.LOG_LEVEL_SETTINGS_KEY, setLevel.toString());
        Logger gitLogger = Logger.getLogger("de.adito.git");
        Handler[] handlers = gitLogger.getHandlers();
        gitLogger.setLevel(setLevel);
        for (Handler h : handlers)
        {
          if (h instanceof FileHandler)
            h.setLevel(setLevel);
        }
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository)
  {
    return pRepository.map(pRepo -> Optional.of(pRepo.isPresent()));
  }
}
