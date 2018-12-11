package de.adito.git.nbm.sidebar;

import de.adito.git.nbm.util.RepositoryUtility;
import org.netbeans.spi.editor.SideBarFactory;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.*;


/**
 * @author a.arnold, 22.11.2018
 */
@SuppressWarnings("unused") //Layer.xml
public class EditorColorizerFactory implements SideBarFactory
{

  /**
   * creates the sidebar (left of the editor pane) for the version control software
   *
   * @param pTarget the editor JTextComponent
   * @return the EditorColorizer
   */
  @Override
  public JComponent createSideBar(JTextComponent pTarget)
  {
    if (pTarget == null || pTarget.getUI() == null || pTarget.getUI().getEditorKit(pTarget) == null)
      return null;

    DataObject dataObject = (DataObject) pTarget.getDocument().getProperty(Document.StreamDescriptionProperty);
    if (dataObject == null || dataObject.getPrimaryFile() == null)
      return null;

    return new EditorColorizer(RepositoryUtility.find(dataObject), pTarget);
  }

}
