package de.adito.git.gui;

/**
 * Central space to store variable definitions that are used in several places
 * and who are valid and pre-determined in the scope of the whole application
 *
 * @author m.kaspera 12.11.2018
 */
public class Constants
{
  public static final int SCROLL_SPEED_INCREMENT = 16;

  // Merge Dialog Icons
  public static final String ACCEPT_CHANGE_YOURS_ICON = "/de/adito/git/gui/icons/acceptChange_dark.png";
  public static final String ACCEPT_CHANGE_THEIRS_ICON = "/de/adito/git/gui/icons/acceptChangeRight_dark.png";
  public static final String DISCARD_CHANGE_ICON = "/de/adito/git/gui/icons/discardChange_dark.png";
  public static final String ARROW_RIGHT = "/de/adito/git/gui/icons/arrowRight.png";

  // Action Icons
  public static final String COMMIT_ACTION_ICON = "/de/adito/git/nbm/actions/commit.png";
  public static final String RESOLVE_CONFLICTS_ACTION_ICON = "/de/adito/git/nbm/actions/fix_conflicts_dark.png";
  public static final String REVERT_ACTION_ICON = "/de/adito/git/nbm/actions/revert_dark.png";
  public static final String IGNORE_ACTION_ICON = "/de/adito/git/nbm/actions/ignore_dark.png";
  public static final String EXCLUDE_ACTION_ICON = "/de/adito/git/nbm/actions/exclude_dark.png";
  public static final String DIFF_ACTION_ICON = "/de/adito/git/nbm/actions/diff_dark.png";
  public static final String GIT_CONFIG_ICON = "/de/adito/git/gui/icons/settings_dark.png";

  // Sidebar Icon
  public static final String ROLLBACK_ICON = "/de/adito/git/nbm/sidebar/rollback_dark.png";

  //Settings map keys
  public static final String SSH_KEY_KEY = "sshKey";

  private Constants()
  {
  }
}

