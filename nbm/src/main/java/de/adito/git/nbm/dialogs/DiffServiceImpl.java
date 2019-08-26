package de.adito.git.nbm.dialogs;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IDiffService;
import de.adito.git.api.IStandAloneDiffProvider;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.nbm.IGitConstants;
import org.openide.util.lookup.ServiceProvider;

import java.io.File;
import java.util.List;

/**
 * @author m.kaspera, 23.08.2019
 */
@ServiceProvider(service = IDiffService.class)
public class DiffServiceImpl implements IDiffService
{

  private static final String TEMP_FOLDER_NAME = "/tmp";

  private final IStandAloneDiffProvider standAloneDiffProvider;
  private final IDialogProvider dialogProvider;

  public DiffServiceImpl()
  {
    standAloneDiffProvider = IGitConstants.INJECTOR.getInstance(IStandAloneDiffProvider.class);
    dialogProvider = IGitConstants.INJECTOR.getInstance(IDialogProvider.class);
  }

  @Override
  public void showDiff(String pVersion1, String pVersion2)
  {
    dialogProvider.showDiffDialog(new File(TEMP_FOLDER_NAME), List.of(standAloneDiffProvider.diffOffline(pVersion1, pVersion2)), null, false, false);
  }

  @Override
  public void showDiff(byte[] pVersion1, byte[] pVersion2)
  {
    dialogProvider.showDiffDialog(new File(TEMP_FOLDER_NAME), List.of(standAloneDiffProvider.diffOffline(pVersion1, pVersion2)), null, false, false);
  }
}
