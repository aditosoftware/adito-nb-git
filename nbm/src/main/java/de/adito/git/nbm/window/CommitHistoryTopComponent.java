package de.adito.git.nbm.window;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

import javax.annotation.Nullable;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link AbstractRepositoryTopComponent} that shows the commit history window
 *
 * @author a.arnold, 24.10.2018
 */
class CommitHistoryTopComponent extends AbstractRepositoryTopComponent
{

  @NotNull
  private final IPrefStore prefStore;
  @Nullable
  private final String displayableContext;

  @Inject
  CommitHistoryTopComponent(@NotNull IWindowContentProvider pWindowContentProvider, @NotNull IPrefStore pPrefStore,
                            @Assisted Observable<Optional<IRepository>> pRepository, @Assisted TableModel tableModel, @Assisted Runnable loadMoreCallback,
                            @Assisted Consumer<ICommitFilter> pRefreshContent, @Assisted ICommitFilter pStartFilter, @Assisted @Nullable String pDisplayableContext)
  {
    super(pRepository);
    prefStore = pPrefStore;
    displayableContext = pDisplayableContext;
    setLayout(new BorderLayout());
    add(pWindowContentProvider.createCommitHistoryWindowContent(pRepository, tableModel, loadMoreCallback, pRefreshContent, pStartFilter), BorderLayout.CENTER);
  }

  @Override
  public String getInitialMode()
  {
    return prefStore.get(CommitHistoryTopComponent.class.getName()) == null ? "output" : prefStore.get(CommitHistoryTopComponent.class.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getTopComponentName()
  {
    if (displayableContext == null)
      return (NbBundle.getMessage(CommitHistoryTopComponent.class, "Label.Commits"));
    else
      return displayableContext;
  }

  @Override
  protected void componentClosed()
  {
    super.componentClosed();
    Mode mode = WindowManager.getDefault().findMode(this);
    if (mode != null)
      prefStore.put(CommitHistoryTopComponent.class.getName(), mode.getName());
  }
}
