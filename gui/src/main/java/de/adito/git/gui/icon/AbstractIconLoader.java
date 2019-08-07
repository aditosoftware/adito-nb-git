package de.adito.git.gui.icon;

import de.adito.git.api.icon.IIconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Abstract IconLoader which provides private Methods for the IconLoader Implementations.
 *
 * @author m.haertel, 07.08.2019
 */
public abstract class AbstractIconLoader implements IIconLoader
{
  private static final String DARK_LAF_SUFFIX = "_dark";

  /**
   * {@inheritDoc}
   */
  @Override
  public String getIconResourceForTheme(@NotNull String pIconBase)
  {
    if (_isDarkLaf()){
      return _extendIconBaseWithSuffix(pIconBase);
    }else{
      return pIconBase;
    }
  }

  /**
   * @return true, if Dark LookAndFeel is enabled
   */
  private boolean _isDarkLaf(){
    if (UIManager.get("nb.dark.theme") != null){
      return (Boolean) UIManager.get("nb.dark.theme");
    }
    return false;
  }

  /**
   * Extends an IconBase-String with the Suffix "_dark".
   *
   * @param pIconBase the IconBase
   * @return the extended IconBase
   */
  private String _extendIconBaseWithSuffix(String pIconBase)
  {
    int idxExt = pIconBase.lastIndexOf('.');
    if ((idxExt != -1) && (idxExt > pIconBase.lastIndexOf('/'))) {
      return pIconBase.substring(0, idxExt) + DARK_LAF_SUFFIX + pIconBase.substring(idxExt);
    }
    return pIconBase + DARK_LAF_SUFFIX;
  }
}
