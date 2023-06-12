package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import lombok.NonNull;

/**
 * @author m.kaspera, 19.03.2020
 */
public interface IDiffDetails
{

  @NonNull
  String getId(EChangeSide pChangeSide);

  @NonNull
  EChangeType getChangeType();

  @NonNull
  EFileType getFileType(EChangeSide pChangeSide);

}
