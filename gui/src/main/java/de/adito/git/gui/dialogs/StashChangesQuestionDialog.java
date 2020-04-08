package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.icon.IIconLoader;
import io.reactivex.Observable;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 25.11.2019
 */
class StashChangesQuestionDialog extends RevertFilesDialog
{
  @Inject
  public StashChangesQuestionDialog(IActionProvider pActionProvider, IIconLoader pIconLoader, IPrefStore pPrefStore, IFileSystemUtil pFileSystemUtil,
                                    @Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<IFileChangeType> pFilesToRevert, @Assisted File pProjectDir)
  {
    super(pActionProvider, pIconLoader, pPrefStore, pFileSystemUtil, pRepository, pFilesToRevert, pProjectDir);
    if (pFilesToRevert.size() == 1)
    {
      descriptionLabel.setText("<html>The following file was changed: " + pFilesToRevert.get(0).getFile().getAbsolutePath() + ". <br><br>The changes to the file have " +
                                   "to either be stashed or discarded before Git can continue</html>");
    }
    else
    {
      descriptionLabel.setHorizontalAlignment(SwingConstants.LEFT);
      descriptionLabel.setText("Found several changed files, the changes to the listed files have to either be stashed or discarded before Git can continue");
    }
  }
}
