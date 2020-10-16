package de.adito.git.nbm.dialogs;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IDiffService;
import de.adito.git.api.IStandAloneDiffProvider;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.nbm.IGitConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
  public void showDiff(@NotNull String pVersionLeft, @NotNull String pVersionRight)
  {
    dialogProvider.showDiffDialog(new File(TEMP_FOLDER_NAME),
                                  List.of(standAloneDiffProvider.diffOffline(pVersionLeft, pVersionRight)), null, null, null, null, false, false);
  }

  @Override
  public void showDiff(@NotNull byte[] pVersionLeft, @NotNull byte[] pVersionRight)
  {
    dialogProvider.showDiffDialog(new File(TEMP_FOLDER_NAME),
                                  List.of(standAloneDiffProvider.diffOffline(pVersionLeft, pVersionRight)), null, null, null, null, false, false);
  }

  @Override
  public void showDiff(@NotNull String pVersionLeft, @NotNull String pVersionRight, @NotNull String pTitle, @Nullable String pHeaderLeft, @Nullable String pHeaderRight)
  {
    dialogProvider.showDiffDialog(new File(TEMP_FOLDER_NAME), List.of(standAloneDiffProvider.diffOffline(pVersionLeft, pVersionRight)), null, pTitle, pHeaderLeft,
                                  pHeaderRight, false, false);
  }

  @Override
  public void showDiff(@NotNull byte[] pVersionLeft, @NotNull byte[] pVersionRight, @NotNull String pTitle, @Nullable String pHeaderLeft, @Nullable String pHeaderRight)
  {
    dialogProvider.showDiffDialog(new File(TEMP_FOLDER_NAME), List.of(standAloneDiffProvider.diffOffline(pVersionLeft, pVersionRight)), null, pTitle, pHeaderLeft,
                                  pHeaderRight, false, false);
  }
}
