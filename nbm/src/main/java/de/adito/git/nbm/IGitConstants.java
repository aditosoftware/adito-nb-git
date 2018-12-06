package de.adito.git.nbm;

import com.google.inject.*;
import de.adito.git.nbm.Guice.AditoNbmModule;

/**
 * A class for all constants in in IGit
 *
 * @author a.arnold, 22.10.2018
 */
public interface IGitConstants
{
  Injector INJECTOR = Guice.createInjector(new AditoNbmModule());
  //Toolbar
  String TOOLBAR_ACTION_PATH = "Toolbars/Git";
  //Main menu
  String MENU_ACTION_PATH = "Menu/Git";
  //Projects
  String RIGHTCLICK_ACTION_PATH = "Actions/Git";

  //Time to wait till the NetBeans editor window updated (MILLISECONDS)
  int TIMETOWAIT = 500;
}
