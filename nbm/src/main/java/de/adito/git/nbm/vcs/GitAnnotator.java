package de.adito.git.nbm.vcs;

import de.adito.git.api.data.IFileChangeType;
import org.netbeans.modules.versioning.spi.*;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This annotator decorates the data tree with git colors
 *
 * @author a.arnold, 30.10.2018
 */
class GitAnnotator extends VCSAnnotator
{
  @Override
  public String annotateName(String pName, VCSContext pContext)
  {
    File contextFile = pContext.getFiles().iterator().next();
    IFileChangeType change = GitVCSUtility.findChanges(contextFile);
    if (change != null)
    {
      Color fileChangeColor = change.getChangeType().getStatusColor();
      if (fileChangeColor != null)
      {
        String contextColor = Integer.toHexString(fileChangeColor.getRGB()).substring(2);
        return "<font color=\"#" + contextColor + "\">" + pName + "</font>";
      }
    }
    return pName;
  }

  @Override
  public Action[] getActions(VCSContext pContext, ActionDestination pDestination)
  {
    return Utilities.actionsForPath("Actions/Git").toArray(new Action[0]);
  }
}
