package de.adito.git.gui.dialogs;

import javax.swing.*;

public abstract class AditoBaseDialog<T> extends JPanel
{

  public abstract String getMessage();

  public abstract T getInformation();

}
