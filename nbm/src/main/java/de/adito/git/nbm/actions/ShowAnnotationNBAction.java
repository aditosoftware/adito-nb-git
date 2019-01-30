package de.adito.git.nbm.actions;

import de.adito.git.nbm.IGitConstants;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;

/**
 * @author m.kaspera, 29.01.2019
 */
public class ShowAnnotationNBAction extends AbstractAction
{

  private JTextComponent targetEditor;

  public ShowAnnotationNBAction(JTextComponent pTargetEditor)
  {
    super("Annotate");
    targetEditor = pTargetEditor;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    Object annotator_active_flag = targetEditor.getClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG);
    if (annotator_active_flag == null || !(Boolean) annotator_active_flag)
    {
      targetEditor.putClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG, true);
    }
    else
    {
      targetEditor.putClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG, false);
    }
  }
}
