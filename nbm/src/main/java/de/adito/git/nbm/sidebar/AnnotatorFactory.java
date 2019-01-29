package de.adito.git.nbm.sidebar;

import de.adito.git.nbm.util.RepositoryUtility;
import org.netbeans.spi.editor.SideBarFactory;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * @author a.arnold, 22.01.2019
 */
@SuppressWarnings("unused") //Layer.xml
public class AnnotatorFactory implements SideBarFactory
{
  @Override
  public JComponent createSideBar(JTextComponent pTarget)
  {
    if (!(pTarget == null || pTarget.getUI() == null || pTarget.getUI().getEditorKit(pTarget) == null))
    {
      DataObject dataObject = (DataObject) pTarget.getDocument().getProperty(Document.StreamDescriptionProperty);
      if (dataObject == null || dataObject.getPrimaryFile() == null)
        return null;

      if ("file".equals(dataObject.getPrimaryFile().toURI().getScheme()))
        return new Annotator(RepositoryUtility.find(dataObject), dataObject, pTarget);
    }
    return null;
  }
}
