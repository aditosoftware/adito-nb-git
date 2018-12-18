package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.IDiscardable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class for a panel that can be customized in such a way that it can both work as a panel for the Merge and Diff dialog.
 * This is because it has a central JTextPane (that is passed in and whose behavior can be defined outside this class)
 * and methods that add panels with lineNumbers (whose behavior is defined when creating the lineNumPanels) and/or
 * panels with buttons for accept/discard changes (behaviour is, once more, defined when creating that particular panel).
 *
 * @author m.kaspera 17.12.2018
 */
public class BaseDiffPanel extends JPanel implements IDiscardable
{

  private final JScrollPane mainScrollPane;
  private final List<JScrollPane> componentScrollPaneList = new ArrayList<>();
  private final List<IDiscardable> discardAbles = new ArrayList<>();
  private final JTextPane textPane;
  // since the BorderLayout only has one spot for east/west, the components have to be added to nested Panel each time a component is added
  private JPanel outerEasternPanel = this;
  private JPanel outerWesternPanel = this;

  BaseDiffPanel(JScrollPane pScrollPane, JTextPane pJTextPane)
  {
    mainScrollPane = pScrollPane;
    mainScrollPane.setBorder(null);
    textPane = pJTextPane;
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    add(mainScrollPane, BorderLayout.CENTER);
  }

  /**
   * @param pModel           DiffPanelModel with the Observable list of fileChangeChunks
   * @param pLineOrientation String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                         Defaults to BorderLayout.EAST if another String is passed
   */
  void addLineNumPanel(DiffPanelModel pModel, String pLineOrientation)
  {
    JPanel nestedPanel = new JPanel(new BorderLayout());
    LineNumPanel lineNumPanel = new LineNumPanel(pModel);
    discardAbles.add(lineNumPanel);
    JScrollPane lineNumContentScrollPane = lineNumPanel.getContentScrollPane();
    componentScrollPaneList.add(lineNumContentScrollPane);
    _coupleScrollPanes(mainScrollPane, lineNumContentScrollPane);
    nestedPanel.add(lineNumContentScrollPane, BorderLayout.CENTER);
    if (pLineOrientation.equals(BorderLayout.WEST))
    {
      outerWesternPanel.add(nestedPanel, BorderLayout.WEST);
      outerWesternPanel = nestedPanel;
    }
    else
    {
      outerEasternPanel.add(nestedPanel, BorderLayout.EAST);
      outerEasternPanel = nestedPanel;
    }
  }

  /**
   * @param pModel           DiffPanelModel with the Observable list of fileChangeChunks
   * @param pDiscardIcon     ImageIcon for the discard button
   * @param pAcceptIcon      ImageIcon for the accept button
   * @param pDoOnDiscard     Consumer that defines what happens if the discard button is pressed
   * @param pDoOnAccept      Consumer that defines what happens if the accept button is pressed
   * @param pLineOrientation String with the orientation of the Panel, pass either BorderLayout.EAST or BorderLayout.WEST.
   *                         Defaults to BorderLayout.EAST if another String is passed
   */
  void addChoiceButtonPanel(DiffPanelModel pModel, ImageIcon pDiscardIcon, ImageIcon pAcceptIcon,
                            Consumer<IFileChangeChunk> pDoOnDiscard, Consumer<IFileChangeChunk> pDoOnAccept, String pLineOrientation)
  {
    JPanel nestedPanel = new JPanel(new BorderLayout());
    ChoiceButtonPanel choiceButtonPanel = new ChoiceButtonPanel(pModel, pDiscardIcon, pAcceptIcon,
                                                                textPane.getFontMetrics(textPane.getFont()).getHeight(),
                                                                pDoOnDiscard, pDoOnAccept, pLineOrientation);
    discardAbles.add(choiceButtonPanel);
    JScrollPane choiceButtonContentScrollPane = choiceButtonPanel.getContentScrollPane();
    componentScrollPaneList.add(choiceButtonContentScrollPane);
    _coupleScrollPanes(mainScrollPane, choiceButtonContentScrollPane);
    nestedPanel.add(choiceButtonContentScrollPane, BorderLayout.CENTER);
    if (pLineOrientation.equals(BorderLayout.WEST))
    {
      outerWesternPanel.add(nestedPanel, BorderLayout.WEST);
      outerWesternPanel = nestedPanel;
    }
    else
    {
      outerEasternPanel.add(nestedPanel, BorderLayout.EAST);
      outerEasternPanel = nestedPanel;
    }
  }

  @Override
  public void discard()
  {
    for (IDiscardable discardAble : discardAbles)
    {
      discardAble.discard();
    }
  }

  JScrollPane getMainScrollPane()
  {
    return mainScrollPane;
  }

  void coupleToScrollPane(JScrollPane pMainScrollPane)
  {
    _coupleScrollPanes(pMainScrollPane, mainScrollPane);
    for (JScrollPane componentScrollPane : componentScrollPaneList)
    {
      _coupleScrollPanes(pMainScrollPane, componentScrollPane);
    }
  }

  /**
   * Makes the masterScrollPane determine the scrolling behaviour/speed of the slaveScrollPane.
   * Both Panes are then  intertwined and cannot be scrolled independently
   *
   * @param pMasterScrollPane ScrollPane that will control scrolling behaviour. Is notified when the slave scrolls and does scroll then, too
   * @param pSlaveScrollPane  ScrollPane that is linked to the master. Scrolling in the master also means scrolling in the slave, and vice versa
   */
  private void _coupleScrollPanes(JScrollPane pMasterScrollPane, JScrollPane pSlaveScrollPane)
  {
    pSlaveScrollPane.getVerticalScrollBar().setModel(pMasterScrollPane.getVerticalScrollBar().getModel());
    pSlaveScrollPane.setWheelScrollingEnabled(false);
    pSlaveScrollPane.addMouseWheelListener(pMasterScrollPane::dispatchEvent);
  }
}
