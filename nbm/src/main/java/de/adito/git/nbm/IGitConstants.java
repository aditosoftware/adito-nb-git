package de.adito.git.nbm;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.adito.git.nbm.guice.AditoNbmModule;

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
  // SubMenu of RightClick action
  String RIGHTCLICK_SUBMENU_ACTION_PATH = "Actions/Git/Other";

  // Annotator flag
  String ANNOTATOR_ACTIVF_FLAG = "adito-git-annotator-active";

  //Statics for the preferences
  String GIT_SSH_KEY = "git.sshKey";
  String GIT_PROJECT_LOCATION = "git.project.Location";
}
