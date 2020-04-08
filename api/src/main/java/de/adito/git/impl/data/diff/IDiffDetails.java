package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import org.jetbrains.annotations.NotNull;

/**
 * @author m.kaspera, 19.03.2020
 */
public interface IDiffDetails
{

  @NotNull
  String getId(EChangeSide pChangeSide);

  @NotNull
  EChangeType getChangeType();

  @NotNull
  EFileType getFileType(EChangeSide pChangeSide);

}
