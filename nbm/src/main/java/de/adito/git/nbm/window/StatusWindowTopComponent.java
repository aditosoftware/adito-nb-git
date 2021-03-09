package de.adito.git.nbm.window;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

import java.awt.BorderLayout;
import java.util.Optional;

/**
 * A {@link AbstractRepositoryTopComponent} that shows the status of changed files in the project
 *
 * @author m.kaspera 06.11.2018
 */
public class StatusWindowTopComponent extends AbstractRepositoryTopComponent
{

  @NotNull
  private final IPrefStore prefStore;

  @Inject
  StatusWindowTopComponent(IWindowContentProvider pWindowContentProvider, @NotNull IPrefStore pPrefStore, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super(pRepository);
    prefStore = pPrefStore;
    setLayout(new BorderLayout());
    add(pWindowContentProvider.createStatusWindowContent(pRepository));
  }

  @Override
  protected String getInitialMode()
  {
    return prefStore.get(StatusWindowTopComponent.class.getName()) == null ? "output" : prefStore.get(StatusWindowTopComponent.class.getName());
  }

  @Override
  protected String getTopComponentName()
  {
    return NbBundle.getMessage(StatusWindowTopComponent.class, "Label.StatusWindow");
  }

  @Override
  protected void componentClosed()
  {
    super.componentClosed();
    Mode mode = WindowManager.getDefault().findMode(this);
    if (mode != null)
      prefStore.put(StatusWindowTopComponent.class.getName(), mode.getName());
  }

}
