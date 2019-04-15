package de.adito.git.gui.dialogs;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AditoBaseDialog<T> extends JPanel
{

  @Nullable
  public abstract String getMessage();

  @Nullable
  public abstract T getInformation();

}
