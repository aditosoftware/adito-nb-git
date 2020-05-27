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

  private final JTextComponent targetEditor;

  public ShowAnnotationNBAction(JTextComponent pTargetEditor)
  {
    super("Annotate");
    targetEditor = pTargetEditor;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    Object annotatorActiveFlag = targetEditor.getClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG);
    targetEditor.putClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG, annotatorActiveFlag == null || !(Boolean) annotatorActiveFlag);
  }
}
