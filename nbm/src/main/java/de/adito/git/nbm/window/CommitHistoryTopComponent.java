package de.adito.git.nbm.window;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;
import org.openide.util.NbBundle;

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

  @Nullable
  private final String displayableContext;

  @Inject
  CommitHistoryTopComponent(IWindowContentProvider pWindowContentProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                            @Assisted TableModel tableModel, @Assisted Runnable loadMoreCallback, @Assisted Consumer<ICommitFilter> pRefreshContent,
                            @Assisted ICommitFilter pStartFilter,
                            @Assisted @Nullable String pDisplayableContext)
  {
    super(pRepository);
    displayableContext = pDisplayableContext;
    setLayout(new BorderLayout());
    add(pWindowContentProvider.createCommitHistoryWindowContent(pRepository, tableModel, loadMoreCallback, pRefreshContent, pStartFilter), BorderLayout.CENTER);
  }

  @Override
  public String getInitialMode()
  {
    return "output";
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
}
