package de.adito.git.gui.actions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRemote;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IGitConfigDialogResult;
import de.adito.git.impl.data.SSHKeyDetails;
import de.adito.git.impl.util.GitRawTextComparator;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 24.12.2018
 */
class GitConfigAction extends AbstractTableAction
{

  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repositoryObs;
  private final IPrefStore prefStore;
  private final IKeyStore keyStore;

  @Inject
  public GitConfigAction(IDialogProvider pDialogProvider, IPrefStore pPrefStore, IKeyStore pKeyStore, @Assisted Observable<Optional<IRepository>> pRepositoryObs)
  {
    super("Settings", _getIsEnabledObservable(pRepositoryObs));
    prefStore = pPrefStore;
    keyStore = pKeyStore;
    putValue(Action.SMALL_ICON, Constants.GIT_CONFIG_ICON);
    repositoryObs = pRepositoryObs;
    dialogProvider = pDialogProvider;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    IGitConfigDialogResult<?, Multimap<String, Object>> dialogResult = dialogProvider.showGitConfigDialog(repositoryObs);
    if (dialogResult.doSave())
    {
      IRepository repository = repositoryObs.blockingFirst().orElse(null);
      if (repository != null)
      {
        _storeRemoteInfos(dialogResult, repository);
      }
      // only set sshKeyLocation for now since that is the only supported setting (for now)
      _storeSSHKeyInfos(dialogResult, repository);
      Object autoResolveFlag = Iterables.getFirst(dialogResult.getInformation().get(Constants.AUTO_RESOLVE_SETTINGS_KEY), null);
      if (autoResolveFlag != null)
        prefStore.put(Constants.AUTO_RESOLVE_SETTINGS_KEY, autoResolveFlag.toString());
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

      Iterator<Object> comparatorIter = dialogResult.getInformation().get(Constants.RAW_TEXT_COMPARATOR_SETTINGS_KEY).iterator();
      if (comparatorIter.hasNext())
      {
        Object currentComparator = comparatorIter.next();
        prefStore.put(Constants.RAW_TEXT_COMPARATOR_SETTINGS_KEY, currentComparator.toString());
        GitRawTextComparator.setCurrent(currentComparator.toString());
      }
    }
  }

  private void _storeRemoteInfos(@NotNull IGitConfigDialogResult<?, Multimap<String, Object>> pDialogResult, @NotNull IRepository pRepository)
  {
    List<IRemote> storedRemotes = pRepository.getRemotes();
    List<IRemote> remotes = pDialogResult.getInformation().get(Constants.REMOTE_INFO_KEY).stream().map(pObj -> (IRemote) pObj).collect(Collectors.toList());
    for (IRemote storedRemote : storedRemotes)
    {
      if (!remotes.contains(storedRemote))
      {
        pRepository.getConfig().removeRemote(storedRemote);
      }
    }
    for (IRemote remote : remotes)
    {
      if (!storedRemotes.contains(remote))
      {
        pRepository.getConfig().saveRemote(remote);
      }
    }
  }

  private void _storeSSHKeyInfos(@NotNull IGitConfigDialogResult<?, Multimap<String, Object>> pDialogResult, @Nullable IRepository pRepository)
  {
    for (Object obj : pDialogResult.getInformation().get(Constants.SSH_KEY_KEY))
    {
      SSHKeyDetails value = (SSHKeyDetails) obj;
      if (value.getKeyLocation() != null && pRepository != null)
      {
        pRepository.getConfig().setSshKeyLocation(value.getKeyLocation(), value.getRemoteName());
        if (value.getPassPhrase() != null)
        {
          keyStore.save(value.getKeyLocation(), value.getPassPhrase(), null);
        }
        value.nullifyPassphrase();
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository)
  {
    return pRepository.map(pRepo -> Optional.of(pRepo.isPresent()));
  }
}
