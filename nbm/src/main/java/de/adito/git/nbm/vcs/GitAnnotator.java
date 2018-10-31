package de.adito.git.nbm.vcs;

import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author a.arnold, 30.10.2018
 */
class GitAnnotator extends VCSAnnotator {

    @Override
    public String annotateName(String name, VCSContext context) {
        return super.annotateName(name, context);
    }

    @Override
    public Image annotateIcon(Image icon, VCSContext context) {
        return super.annotateIcon(icon, context);
    }

    @Override
    public Action[] getActions(VCSContext context, ActionDestination destination) {
        return Utilities.actionsForPath("Actions/Git").toArray(new Action[0]);
    }

}
