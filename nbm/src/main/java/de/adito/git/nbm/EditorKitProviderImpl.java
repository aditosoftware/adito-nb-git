package de.adito.git.nbm;

import de.adito.git.gui.IEditorKitProvider;
import org.openide.text.CloneableEditorSupport;

import javax.swing.text.EditorKit;

/**
 * @author m.kaspera 13.11.2018
 */
public class EditorKitProviderImpl implements IEditorKitProvider
{

  @Override
  public EditorKit getEditorKit(String pMimeType)
  {
    return CloneableEditorSupport.getEditorKit(pMimeType);
  }
}
