package de.adito.git.nbm.actions;

/**
 * Interface that just stores the positions of the different actions in the Netbeans toolbar/right click menu
 * This simplifies the management since there is only one place where all positions can be adjusted and it
 * is a lot simpler to see if several actions have the same position (or which positions are still free)
 *
 * @author m.kaspera, 14.12.2018
 */
interface INBActionPositions
{

  // --------------------------  right click positions ---------------------------
  int PULL_ACTION_RIGHT_CLICK = 100;
  int PUSH_ACTION_RIGHT_CLICK = 200;
  int COMMIT_ACTION_RIGHT_CLICK = 300;
  int IGNORE_ACTION_RIGHT_CLICK = 400;
  int EXCLUDE_ACTION_RIGHT_CLICK = 500;
  int DIFF_LOCAL_CHANGES_ACTION_RIGHT_CLICK = 600;
  int SHOW_STATUS_WINDOW_ACTION_RIGHT_CLICK = 700;
  int SHOW_FILE_HISTORY_ACTION_RIGHT_CLICK = 800;
  int STASH_CHANGES_ACTION_RIGHT_CLICK = 900;
  int UN_STASH_CHANGES_ACTION_RIGHT_CLICK = 1000;
  int REVERT_ACTION_RIGHT_CLICK = 1100;
  int RESOLVE_CONFLICTS_ACTION_RIGHT_CLICK = 1200;
  int RENORMALIZE_NEWLINES_RIGHT_CLICK = 1225;
  int GIT_INIT_RIGHT_CLICK = 1250;
  int GIT_CONFIG_ACTION_RIGHT_CLICK = 1300;

  // ----------------------  other right click positions -------------------------
  int FETCH_ACTION_RIGHT_CLICK = 210;

  // --------------------------  toolbar positions -------------------------------
  int PULL_ACTION_TOOLBAR = 100;
  int COMMIT_ACTION_TOOLBAR = 200;
  int PUSH_ACTION_TOOLBAR = 300;
  int DIFF_LOCAL_CHANGES_ACTION_TOOLBAR = 400;
  int SHOW_STATUS_WINDOW_ACTION_TOOLBAR = 600;
  int SHOW_ALL_COMMITS_ACTION_TOOLBAR = 800;
}
