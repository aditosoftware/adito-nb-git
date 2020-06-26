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
    super(_isActive(pTargetEditor) ? "Close Annotations" : "Annotate");
    targetEditor = pTargetEditor;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    Object annotatorActiveFlag = targetEditor.getClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG);
    targetEditor.putClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG, annotatorActiveFlag == null || !(Boolean) annotatorActiveFlag);
  }

  /**
   * @param pTargetEditor Editor for which the annotator would display values. The active flag is set there as client property
   * @return true if the annotator is currently active
   */
  private static boolean _isActive(JTextComponent pTargetEditor)
  {
    Object property = pTargetEditor.getClientProperty(IGitConstants.ANNOTATOR_ACTIVF_FLAG);
    return property != null && (Boolean) property;
  }
}
