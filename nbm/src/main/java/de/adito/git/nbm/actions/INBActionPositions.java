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
  int REVERT_ACTION_RIGHT_CLICK = 300;
  int IGNORE_ACTION_RIGHT_CLICK = 500;
  int SHOW_STATUS_WINDOW_ACTION_RIGHT_CLICK = 600;
  int COMMIT_ACTION_RIGHT_CLICK = 700;
  int RESOLVE_CONFLICTS_ACTION_RIGHT_CLICK = 800;

  // --------------------------  toolbar positions -------------------------------
  int PULL_ACTION_TOOLBAR = 100;
  int COMMIT_ACTION_TOOLBAR = 200;
  int PUSH_ACTION_TOOLBAR = 300;
  int SHOW_STATUS_WINDOW_ACTION_TOOLBAR = 600;
  int SHOW_ALL_COMMITS_ACTION_TOOLBAR = 800;
}