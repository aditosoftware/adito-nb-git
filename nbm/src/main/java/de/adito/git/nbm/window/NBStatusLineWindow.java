package de.adito.git.nbm.window;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.util.Optional;

/**
 * @author a.arnold, 07.11.2018
 */
@SuppressWarnings("unused")
public class NBStatusLineWindow extends JPanel
{

  @Inject
  public NBStatusLineWindow(IWindowContentProvider pWindowContentProvider, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    JComponent statusLineWindowContent = pWindowContentProvider.createStatusLineWindowContent(pRepository);
    add(statusLineWindowContent);
  }
}
