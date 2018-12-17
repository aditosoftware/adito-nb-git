package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.*;
import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class ChoiceButtonPanel implements IDiscardable
{

  private static final int ADDITIONAL_HEIGHT_PIXELS = 3;
  private static final int BUTTON_BAR_WIDTH = 35;
  private static final int HALF_BUTTON_HEIGHT = 8;

  private final JPanel buttonBar = new JPanel();
  private final JScrollPane buttonPanelScrollPane = new JScrollPane(buttonBar, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                                                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  private final Disposable disposable;
  private final ImageIcon discardIcon;
  private final ImageIcon acceptIcon;
  private final int lineHeight;
  private final Consumer<IFileChangeChunk> doOnDiscard;
  private final Consumer<IFileChangeChunk> doOnAccept;
  private String orientation;


  ChoiceButtonPanel(DiffPanelModel pModel, ImageIcon pDiscardIcon, ImageIcon pAcceptIcon,
                    int pLineHeight, Consumer<IFileChangeChunk> pDoOnDiscard, Consumer<IFileChangeChunk> pDoOnAccept, String pOrientation)
  {
    discardIcon = pDiscardIcon;
    acceptIcon = pAcceptIcon;
    lineHeight = pLineHeight;
    doOnDiscard = pDoOnDiscard;
    doOnAccept = pDoOnAccept;
    disposable = pModel.getFileDiff().switchMap(pFileDiff -> pFileDiff
        .map(pDiff -> pDiff.getFileChanges().getChangeChunks())
        .orElse(Observable.just(Collections.emptyList())))
        .subscribe(this::_initButtonPanel);
    orientation = pOrientation;
  }

  JScrollPane getContentScrollPane()
  {
    return buttonPanelScrollPane;
  }

  private void _initButtonPanel(List<IFileChangeChunk> pChangeChunks)
  {
    buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.Y_AXIS));
    buttonBar.setMinimumSize(new Dimension(BUTTON_BAR_WIDTH, 0));
    buttonBar.setMaximumSize(new Dimension(BUTTON_BAR_WIDTH, Integer.MAX_VALUE));
    // since the mergePanel has no parityLines, the number of Lines * the height of one line should be the total size of the other components
    // start with -3 because else the the buttons are not aligned with the lines, probably due to borders or something
    int heightBefore = -ADDITIONAL_HEIGHT_PIXELS;
    for (IFileChangeChunk changeChunk : pChangeChunks)
    {
      if (changeChunk.getChangeType() != EChangeType.SAME)
      {
        heightBefore = _insertButtonsForChunk(changeChunk, heightBefore, lineHeight);
      }
    }
    buttonBar.add(Box.createGlue());
  }

  /**
   * @param pChangeChunk  IFileChangeChunk for which to draw the button
   * @param pHeightBefore number of pixels from the very top down to the middle of the last buttonPanel
   * @param pLineHeight   the height of one line of text in the JPanel that contains the text of the IFileChangeChunks
   */
  private int _insertButtonsForChunk(IFileChangeChunk pChangeChunk, int pHeightBefore, int pLineHeight)
  {
    JPanel dualButtonPanel = new JPanel(new BorderLayout());
    dualButtonPanel.setMaximumSize(new Dimension(BUTTON_BAR_WIDTH, discardIcon.getIconHeight()));
    JButton discardChangeButton = _createButton(discardIcon, "discard change");
    JButton acceptChangeButton = _createButton(acceptIcon, "accept this change");
    acceptChangeButton.setBorder(null);
    discardChangeButton.setBorder(null);
    dualButtonPanel.add(discardChangeButton, BorderLayout.CENTER);
    dualButtonPanel.add(acceptChangeButton, orientation);
    int currentHeight = (int) ((pChangeChunk.getBStart() + (double) (pChangeChunk.getBEnd() - pChangeChunk.getBStart()) / 2) * pLineHeight);
    buttonBar.add(Box.createRigidArea(new Dimension(0, (currentHeight - (pHeightBefore + HALF_BUTTON_HEIGHT)))));
    buttonBar.add(dualButtonPanel);
    discardChangeButton.addActionListener(e -> doOnDiscard.accept(pChangeChunk));
    acceptChangeButton.addActionListener(e -> doOnAccept.accept(pChangeChunk));
    return currentHeight + HALF_BUTTON_HEIGHT;
  }

  private JButton _createButton(ImageIcon pIcon, String pToolTip)
  {
    JButton createdButton = new JButton(pIcon);
    createdButton.setToolTipText(pToolTip);
    return createdButton;
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
