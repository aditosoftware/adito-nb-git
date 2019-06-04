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

  // Diff Dialog Icons
  public static final String NEXT_OCCURRENCE = "/de/adito/git/gui/icons/next_dark.png";
  public static final String PREVIOUS_OCCURRENCE = "/de/adito/git/gui/icons/previous_dark.png";

  // Action Icons
  public static final String COMMIT_ACTION_ICON = "/de/adito/git/nbm/actions/commit.png";
  public static final String REVERT_ACTION_ICON = "/de/adito/git/nbm/actions/revert_dark.png";
  public static final String DIFF_ACTION_ICON = "/de/adito/git/nbm/actions/diff_dark.png";
  public static final String FILE_HISTORY_ACTION_ICON = "/de/adito/git/gui/icons/file_history_dark.png";
  public static final String GIT_CONFIG_ICON = "/de/adito/git/gui/icons/settings_dark.png";
  public static final String REFRESH_CONTENT_ICON = "/de/adito/git/gui/icons/refresh_dark.png";
  public static final String DELETE_ICON = "/de/adito/git/gui/icons/delete_dark.png";
  public static final String ADD_TAG_ACTION_ICON = "/de/adito/git/gui/icons/tag_icon_dark.png";
  public static final String EXPAND_TREE_ACTION_ICON = "/de/adito/git/gui/icons/expand_dark.png";
  public static final String COLLAPSE_TREE_ACTION_ICON = "/de/adito/git/gui/icons/compress_dark.png";
  public static final String CHERRY_PICK = "/de/adito/git/gui/icons/cherrypick_dark.png";
  public static final String ACCEPT_ALL_LEFT = "/de/adito/git/gui/icons/acceptLeft_dark.png";
  public static final String ACCEPT_ALL_RIGHT = "/de/adito/git/gui/icons/acceptRight_dark.png";

  // Sidebar Icon
  public static final String ROLLBACK_ICON = "/de/adito/git/nbm/sidebar/rollback_dark.png";

  // Branch Icon in CommitHistoryWindow
  public static final String BRANCH_ICON_LOCAL = "/de/adito/git/gui/icons/branch_icon_local.png";
  public static final String BRANCH_ICON_HEAD = "/de/adito/git/gui/icons/branch_icon_head.png";
  public static final String BRANCH_ICON_ORIGIN = "/de/adito/git/gui/icons/branch_icon_origin.png";
  public static final String STASH_COMMIT_ICON = "/de/adito/git/gui/icons/stash_icon.png";
  public static final String TAG_ICON = "/de/adito/git/gui/icons/tag_icon.png";

  //Settings map keys
  public static final String SSH_KEY_KEY = "sshKey";
  public static final String LOG_LEVEL_SETTINGS_KEY = "gitLogLevel";

  private Constants()
  {
  }
}

