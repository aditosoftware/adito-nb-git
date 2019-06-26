package de.adito.git.nbm.sidebar;

import org.netbeans.spi.editor.SideBarFactory;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * @author m.kaspera, 19.06.2019
 */
// used by Netbeans, declared in layer file
@SuppressWarnings("unused")
public class ChangesOverviewStripFactory implements SideBarFactory
{
  @Override
  public JComponent createSideBar(JTextComponent pTarget)
  {
    if (pTarget == null || pTarget.getUI() == null || pTarget.getUI().getEditorKit(pTarget) == null)
      return null;

    DataObject dataObject = (DataObject) pTarget.getDocument().getProperty(Document.StreamDescriptionProperty);
    if (dataObject == null || dataObject.getPrimaryFile() == null)
      return null;

    if ("file".equals(dataObject.getPrimaryFile().toURI().getScheme()))
      return new ChangesOverviewStrip(pTarget);

    return null;
  }
}
