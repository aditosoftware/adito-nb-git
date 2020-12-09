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
  public static final String ACCEPT_CHANGE_YOURS_ICON = "de/adito/git/gui/icons/acceptChange.png";
  public static final String ACCEPT_CHANGE_THEIRS_ICON = "de/adito/git/gui/icons/acceptChangeRight.png";
  public static final String DISCARD_CHANGE_ICON = "de/adito/git/gui/icons/discardChange.png";
  public static final String ARROW_RIGHT = "de/adito/git/gui/icons/arrowRight.png";
  public static final String CARET_RIGHT = "de/adito/git/gui/icons/caret-right.png";

  // Diff Dialog Icons
  public static final String NEXT_OCCURRENCE = "de/adito/git/gui/icons/next.png";
  public static final String PREVIOUS_OCCURRENCE = "de/adito/git/gui/icons/previous.png";

  // Action Icons
  public static final String COMMIT_ACTION_ICON = "de/adito/git/nbm/actions/commit.png";
  public static final String REVERT_ACTION_ICON = "de/adito/git/nbm/actions/revert.png";
  public static final String DIFF_ACTION_ICON = "de/adito/git/nbm/actions/diff.png";
  public static final String FILE_HISTORY_ACTION_ICON = "de/adito/git/gui/icons/file_history.png";
  public static final String SHOW_TAGS_ACTION_ICON = "de/adito/git/gui/icons/tags.png";
  public static final String GIT_CONFIG_ICON = "de/adito/git/gui/icons/settings.png";
  public static final String TRASH_ICON = "de/adito/git/gui/icons/trash.png";
  public static final String REFRESH_CONTENT_ICON = "de/adito/git/gui/icons/refresh.png";
  public static final String DELETE_ICON = "de/adito/git/gui/icons/delete.png";
  public static final String ADD_TAG_ACTION_ICON = "de/adito/git/gui/icons/tag_icon.png";
  public static final String EXPAND_TREE_ACTION_ICON = "de/adito/git/gui/icons/expand.png";
  public static final String COLLAPSE_TREE_ACTION_ICON = "de/adito/git/gui/icons/compress.png";
  public static final String CHERRY_PICK = "de/adito/git/gui/icons/cherrypick.png";
  public static final String ACCEPT_ALL_LEFT = "de/adito/git/gui/icons/acceptLeft.png";
  public static final String ACCEPT_ALL_RIGHT = "de/adito/git/gui/icons/acceptRight.png";
  public static final String ACCEPT_ALL_NON_CONFLICTING = "de/adito/git/gui/icons/accept-non-conflicting.png";
  public static final String SWITCH_TREE_VIEW_FLAT = "de/adito/git/gui/icons/tree_view_flat.png";
  public static final String SWITCH_TREE_VIEW_HIERARCHICAL = "de/adito/git/gui/icons/tree_view_hierarchical.png";
  public static final String SHOW_MORE_DIALOG_OPTION = "de/adito/git/gui/icons/arrowRightSmall12.png";
  public static final String SHOW_LESS_DIALOG_OPTION = "de/adito/git/gui/icons/arrowDownSmall12.png";

  // Sidebar Icon
  public static final String ROLLBACK_ICON = "de/adito/git/nbm/sidebar/rollback.png";

  // Branch Window Icons
  public static final String NEW_BRANCH_ICON = "de/adito/git/gui/icons/plus12.png";

  // Branch Icon in CommitHistoryWindow
  public static final String BRANCH_ICON_LOCAL = "de/adito/git/gui/icons/branch_icon_local.png";
  public static final String BRANCH_ICON_HEAD = "de/adito/git/gui/icons/branch_icon_head.png";
  public static final String BRANCH_ICON_ORIGIN = "de/adito/git/gui/icons/branch_icon_origin.png";
  public static final String STASH_COMMIT_ICON = "de/adito/git/gui/icons/stash_icon.png";
  public static final String TAG_ICON = "de/adito/git/gui/icons/tag_icon.png";

  //Settings map keys
  public static final String REMOTE_INFO_KEY = "git.remote.info";
  public static final String SSH_KEY_KEY = "sshKey";
  public static final String LOG_LEVEL_SETTINGS_KEY = "gitLogLevel";
  public static final String RAW_TEXT_COMPARATOR_SETTINGS_KEY = "rawTextComparator";
  public static final String AUTO_RESOLVE_SETTINGS_KEY = "git.flags.autoresolve";
  public static final String TREE_VIEW_TYPE_KEY = "git.settings.tree.view";
  public static final String TREE_VIEW_FLAT = "flat";
  public static final String TREE_VIEW_HIERARCHICAL = "hierarchical";

  private Constants()
  {
  }
}

