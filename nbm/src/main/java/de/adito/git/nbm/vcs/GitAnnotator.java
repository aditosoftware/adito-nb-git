package de.adito.git.nbm.vcs;

import de.adito.git.api.data.EChangeType;
import org.netbeans.modules.versioning.spi.*;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Function;

/**
 * This annotator decorates the data tree with git colors
 *
 * @author a.arnold, 30.10.2018
 */
class GitAnnotator extends VCSAnnotator
{

  private final Function<File, EChangeType> changeTypeProvider;

  GitAnnotator(Function<File, EChangeType> pChangeTypeProvider)
  {
    changeTypeProvider = pChangeTypeProvider;
  }

  @Override
  public String annotateName(String pName, VCSContext pContext)
  {
    File contextFile = pContext.getFiles().iterator().next();
    EChangeType change = changeTypeProvider.apply(contextFile);
    if (change != null)
    {
      Color fileChangeColor = change.getStatusColor();
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
