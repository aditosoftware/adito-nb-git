package de.adito.git.gui.dialogs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EAutoResolveOptions;
import de.adito.git.api.data.IRemote;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.NewRemotePanel;
import de.adito.git.gui.dialogs.panels.RemotePanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.impl.data.FileChangeTypeImpl;
import de.adito.git.impl.util.GitRawTextComparator;
import io.reactivex.Observable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author m.kaspera, 24.12.2018
 */
public class GitConfigDialog extends AditoBaseDialog<Multimap<String, Object>> implements IDiscardable, RemotePanel.IDeletionRequestListener
{

  private static final Border DEFAULT_MARGIN_BORDER = new EmptyBorder(15, 15, 0, 0);
  private static final Border DEFAULT_PANEL_BORDERS = new EmptyBorder(15, 15, 0, 15);
  private NewRemotePanel addRemotePanel;
  private final List<RemotePanel> remoteSettingsPanels = new ArrayList<>();
  private final GlobalSettingsPanel globalSettingsPanel;
  private JTabbedPane tabbedPane = null;
  private Set<IRemote> remotes = null;
  private IRepository repository = null;

  @Inject
  public GitConfigDialog(IActionProvider pActionProvider, IKeyStore pKeyStore, IPrefStore pPrefStore, IIconLoader pIconLoader,
                         @Assisted Observable<Optional<IRepository>> pRepository)
  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Optional<IRepository> optionalIRepository = pRepository.blockingFirst(Optional.empty());
    if (optionalIRepository.isPresent())
    {
      repository = optionalIRepository.get();
      remotes = new HashSet<>(optionalIRepository.get().getRemotes());
      tabbedPane = new JTabbedPane();
      JPanel labelPanel = new JPanel(new BorderLayout());
      JLabel remotesLabel = _getBoldLabel("Remotes:");
      remotesLabel.setAlignmentX(LEFT_ALIGNMENT);
      labelPanel.add(remotesLabel);
      labelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
      add(labelPanel);
      remotesLabel.setBorder(DEFAULT_MARGIN_BORDER);
      tabbedPane.setBorder(new CompoundBorder(DEFAULT_MARGIN_BORDER, tabbedPane.getBorder()));
      tabbedPane.setMinimumSize(new Dimension(1, 205));
      tabbedPane.setPreferredSize(new Dimension(600, 205));
      add(tabbedPane);
      Consumer<IRemote> createAddRemotePanel = pRemote -> {
        RemotePanel remoteSettingsPanel = new RemotePanel(optionalIRepository.get(), pRemote, pKeyStore, pIconLoader);
        remoteSettingsPanels.add(remoteSettingsPanel);
        remoteSettingsPanel.addDeletionRequestListener(this);
        tabbedPane.add(remoteSettingsPanel, tabbedPane.getTabCount() - 1);
      };
      Consumer<IRemote> addRemoteSettingsPanel = pRemote -> {
        createAddRemotePanel.accept(pRemote);
        remotes.add(pRemote);
      };
      addRemotePanel = new NewRemotePanel(remotes, addRemoteSettingsPanel);
      tabbedPane.add("+", addRemotePanel);
      for (IRemote remote : remotes)
      {
        createAddRemotePanel.accept(remote);
      }
      tabbedPane.setSelectedIndex(0);
    }
    globalSettingsPanel = new GlobalSettingsPanel(pPrefStore);
    add(Box.createVerticalStrut(15));
    JSeparator separator1 = new JSeparator();
    add(separator1);
    add(globalSettingsPanel);
    add(Box.createVerticalStrut(15));
    JSeparator separator2 = new JSeparator();
    add(separator2);
    add(new MiscSettingPanel(pActionProvider, pRepository));
    add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)));
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Multimap<String, Object> getInformation()
  {
    Multimap<String, Object> settingsMap = HashMultimap.create();
    // Mutlimap -> keys with same value do not override each other, but are stored in "sublist"
    for (RemotePanel remoteSettingsPanel : remoteSettingsPanels)
    {
      settingsMap.putAll(remoteSettingsPanel.getInformation());
    }
    settingsMap.putAll(globalSettingsPanel.getInformation());
    return settingsMap;
  }

  @Override
  public void requestDeletion(IRemote pRemote)
  {
    if (repository != null)
      removeRemote(pRemote);
  }

  /**
   * @param pRemote IRemote whose assigned panel should be removed
   */
  private void removeRemote(IRemote pRemote)
  {
    remotes.remove(pRemote);
    Iterator<RemotePanel> remotePanelIterator = remoteSettingsPanels.iterator();
    while (remotePanelIterator.hasNext())
    {
      RemotePanel remotePanel = remotePanelIterator.next();
      if (remotePanel.getRemote().equals(pRemote))
      {
        remotePanel.removeDeletionRequestListener(this);
        tabbedPane.remove(remotePanel);
        remotePanelIterator.remove();
      }
    }
  }

  /**
   * creates a normal JLabel, except that the text is bold
   *
   * @param pText Text the label should have
   * @return JLabel with pText in bold
   */
  private static JLabel _getBoldLabel(String pText)
  {
    JLabel label = new JLabel(pText);
    label.setFont(new Font(label.getFont().getFontName(), Font.BOLD, label.getFont().getSize()));
    return label;
  }

  @Override
  public void discard()
  {
    addRemotePanel.discard();
  }

  /**
   * Put any global settings in to this panel (settings that affect all repositories)
   */
  private static class GlobalSettingsPanel extends JPanel
  {

    private final JComboBox<Level> logLevelBox;
    private final JComboBox<GitRawTextComparator> rawTextComparatorBox;
    private final JComboBox<EAutoResolveOptions> autoResolveComboBox;

    GlobalSettingsPanel(IPrefStore pPrefStore)
    {
      setBorder(DEFAULT_PANEL_BORDERS);
      setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
      setLayout(new BorderLayout(0, 15));
      JLabel titleLabel = _getBoldLabel("Global settings");
      add(titleLabel, BorderLayout.NORTH);
      JPanel otherSettingsPanel = new JPanel(new BorderLayout());
      Logger gitLogger = Logger.getLogger("de.adito.git");
      logLevelBox = new JComboBox<>(new Vector<>(List.of(Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST)));
      Level currentLogLevel = gitLogger.getLevel() == null ? gitLogger.getParent().getLevel() : gitLogger.getLevel();
      logLevelBox.setSelectedItem(currentLogLevel);
      otherSettingsPanel.add(new JLabel("Log level:"), BorderLayout.WEST);
      otherSettingsPanel.add(logLevelBox, BorderLayout.EAST);
      add(otherSettingsPanel, BorderLayout.NORTH);

      rawTextComparatorBox = new JComboBox<>(new Vector<>(GitRawTextComparator.getInstances()));
      rawTextComparatorBox.setSelectedItem(GitRawTextComparator.getCurrent());
      JPanel comparatorPanel = new JPanel(new BorderLayout());
      comparatorPanel.add(new JLabel("Whitespace and Line-Endings:"), BorderLayout.WEST);
      comparatorPanel.add(rawTextComparatorBox, BorderLayout.EAST);
      comparatorPanel.add(new JPanel(new BorderLayout()), BorderLayout.NORTH);
      add(comparatorPanel, BorderLayout.CENTER);

      autoResolveComboBox = new JComboBox<>(EAutoResolveOptions.values());
      autoResolveComboBox.setSelectedItem(EAutoResolveOptions.getFromStringValue(pPrefStore.get(Constants.AUTO_RESOLVE_SETTINGS_KEY)));
      JPanel autoResolvePanel = new JPanel(new BorderLayout());
      autoResolvePanel.add(autoResolveComboBox, BorderLayout.EAST);
      autoResolvePanel.add(new JLabel("Use auto-resolve in merges with conflicts"), BorderLayout.WEST);
      add(autoResolvePanel, BorderLayout.SOUTH);
    }

    public Multimap<String, Object> getInformation()
    {
      Multimap<String, Object> settingsMap = HashMultimap.create();
      settingsMap.put(Constants.LOG_LEVEL_SETTINGS_KEY, logLevelBox.getSelectedItem());
      settingsMap.put(Constants.RAW_TEXT_COMPARATOR_SETTINGS_KEY, rawTextComparatorBox.getSelectedItem().toString());
      settingsMap.put(Constants.AUTO_RESOLVE_SETTINGS_KEY, autoResolveComboBox.getSelectedItem());
      return settingsMap;
    }
  }

  /**
   * Contains miscellaneous options/actions that have to do with the settings of a git Repository
   */
  private static class MiscSettingPanel extends JPanel
  {

    MiscSettingPanel(IActionProvider pActionProvider, Observable<Optional<IRepository>> pRepository)
    {
      setBorder(DEFAULT_PANEL_BORDERS);
      setName("MiscSettingsPanel");
      setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
      setLayout(new BorderLayout(0, 15));
      JLabel titleLabel = _getBoldLabel("Misc");
      add(titleLabel, BorderLayout.NORTH);
      IFileChangeType ignoreFile = _getIgnoreFile(pRepository.blockingFirst().orElse(null));
      // only show the "open xxx file" if that file actually exists
      if (ignoreFile != null && ignoreFile.getFile().exists())
      {
        Action openFileAction = pActionProvider.getOpenFileAction(Observable.just(Optional.of(List.of(ignoreFile))));
        openFileAction.putValue(Action.NAME, "Open git ignore file");
        add(new JButton(openFileAction), BorderLayout.EAST);
      }
      IFileChangeType configFile = _getConfigFile(pRepository.blockingFirst().orElse(null));
      if (configFile != null && configFile.getFile().exists())
      {
        Action openConfigFileAction = pActionProvider.getOpenFileAction(Observable.just(Optional.of(List.of(configFile))));
        openConfigFileAction.putValue(Action.NAME, "Open git config file");
        add(new JButton(openConfigFileAction), BorderLayout.WEST);
      }
    }

    @Nullable
    private IFileChangeType _getIgnoreFile(@Nullable IRepository pRepo)
    {
      if (pRepo == null)
        return null;
      File ignoreFileLoc = new File(pRepo.getTopLevelDirectory(), ".gitignore");
      return new FileChangeTypeImpl(ignoreFileLoc, ignoreFileLoc, EChangeType.SAME);
    }

    @Nullable
    private IFileChangeType _getConfigFile(@Nullable IRepository pRepo)
    {
      if (pRepo == null)
        return null;
      File configFileLoc = Paths.get(pRepo.getTopLevelDirectory().getAbsolutePath(), ".git", "config").toFile();
      return new FileChangeTypeImpl(configFileLoc, configFileLoc, EChangeType.SAME);
    }
  }
}
