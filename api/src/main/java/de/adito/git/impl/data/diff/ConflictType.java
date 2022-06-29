package de.adito.git.impl.data.diff;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author m.kaspera, 14.06.2022
 */
public class ConflictType
{

  public static ConflictType NONE = new ConflictType(EConflictType.NONE);
  public static ConflictType CONFLICTING = new ConflictType(EConflictType.CONFLICTING);
  @Nullable
  private final ResolveOption resolveOption;
  @NotNull
  private final EConflictType conflictType;


  public ConflictType(@NotNull ResolveOption pResolveOption, @NotNull EConflictType pConflictType)
  {
    resolveOption = pResolveOption;
    conflictType = pConflictType;
  }

  private ConflictType(@NotNull EConflictType pConflictType)
  {
    conflictType = pConflictType;
    resolveOption = null;
  }

  /**
   * @return ResolveOption that can be used to resolve the conflict, or null if the conflict cannot be resolved (or conflictType is NONE)
   */
  @Nullable
  public ResolveOption getResolveOption()
  {
    return resolveOption;
  }

  /**
   * @return EConflictType that denotes the type of conflict
   */
  @NotNull
  public EConflictType getConflictType()
  {
    return conflictType;
  }
}
