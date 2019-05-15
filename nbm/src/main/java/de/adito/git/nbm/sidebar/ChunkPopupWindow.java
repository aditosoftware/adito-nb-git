package de.adito.git.nbm.sidebar;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.panels.basediffpanel.IDiffPaneUtil;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * PopupWindow for the EditorColorizer, basically a popup for the lines that are marked as changed.
 * Shows a pre-view of the state in HEAD and offers a way (via button on top) to go back to that state.
 *
 * @author m.kaspera, 23.01.2019
 */
class ChunkPopupWindow extends JWindow
{

  private static final int DEFAULT_MIN_WIDTH = 50;
  private static final int INSET_RIGHT = 25;
  private static final int MIN_HEIGHT = 16;
  private final IIconLoader iconLoader = IGitConstants.INJECTOR.getInstance(IIconLoader.class);
  private final IFileChangeChunk changeChunk;
  private final Observable<List<IFileChangeChunk>> changeChunkList;
  private final EditorColorizer editorColorizer;
  private _WindowDisposer windowDisposer;
  private JScrollPane scrollPane;
  private JToolBar toolBar;

  /**
   * @param pRepository      Observable that contains the IRepository, used for retrieving the HEAD version of the file in pTextComponent
   * @param pParent          Window who should be the owner of this window
   * @param pLocation        Absolute position that this window should appear at
   * @param pChangeChunk     IFileChangeChunk for which this window offers rollback functionality
   * @param pChangeChunkList Observable of the list of IFileChangeChunk that is kept up-to-date
   * @param pTextComponent   JTextComponent that contains the text which the pChangeChunks is part of
   * @param pEditorColorizer EditorColorizer that spawns this PopupWindow. Needed to fire mouseEvents (that spawn a new PopupWindow)
   * @param pFile            File whose contents are opened in pTextComponent
   */
  ChunkPopupWindow(Observable<Optional<IRepository>> pRepository, Window pParent, Point pLocation, IFileChangeChunk pChangeChunk,
                   Observable<List<IFileChangeChunk>> pChangeChunkList, JTextComponent pTextComponent, EditorColorizer pEditorColorizer, File pFile)
  {
    super(pParent);
    changeChunk = pChangeChunk;
    changeChunkList = pChangeChunkList;
    editorColorizer = pEditorColorizer;
    windowDisposer = new _WindowDisposer();
    pLocation.x = pTextComponent.getLocationOnScreen().x - new JScrollPane().getInsets().left;
    setLocation(pLocation);
    _RollbackInformation rollbackInformation;
    rollbackInformation = pRepository.blockingFirst()
        .map(pRepo -> _calculateRollbackInfo(pRepo, pChangeChunk, pTextComponent, pFile)).orElse(new _RollbackInformation(0, 0, ""));
    setLayout(new BorderLayout());
    Dimension viewPortSize = pTextComponent.getParent().getSize();
    _initGui(rollbackInformation, pTextComponent);
    // calculated size of the pane
    Dimension paneSize = _calculateSize(rollbackInformation, pTextComponent.getFontMetrics(pTextComponent.getFont()));
    // size of the pane + toolbar
    Dimension neededSize = new Dimension(Math.max(paneSize.width, toolBar.getPreferredSize().width),
                                         paneSize.height + toolBar.getPreferredSize().height);
    // minimum of the needed size of the window or the size of the toolbar + pane. Any excess size is covered by the scrollPane
    Dimension actualSize = new Dimension(Math.min(neededSize.width, viewPortSize.width), Math.min(neededSize.height, viewPortSize.height));
    if (neededSize.width > actualSize.width)
    {
      // three times the fontHeight to make sure that at least the first line can be clearly seen, even if the horizontal scrollBar is added
      setMinimumSize(new Dimension(1, pTextComponent.getFontMetrics(pTextComponent.getFont()).getHeight() * 3 + toolBar.getPreferredSize().height));
    }
    setPreferredSize(actualSize);
    try
    {
      setAlwaysOnTop(true);
    }
    catch (SecurityException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void setVisible(boolean pIsVisible)
  {
    pack();
    super.setVisible(pIsVisible);
    SwingUtilities.invokeLater(() -> Toolkit.getDefaultToolkit().addAWTEventListener(windowDisposer, AWTEvent.MOUSE_EVENT_MASK
        | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK | AWTEvent.KEY_EVENT_MASK));
  }

  /**
   * Sets up the GUI elements, like buttons/the editorPane and defines their behaviour
   *
   * @param pRollbackInformation RollbackInformation for displaying and performing the rollback
   * @param pTextComponent       TextComponent that the rollback would be enacted upon
   */
  private void _initGui(_RollbackInformation pRollbackInformation, JTextComponent pTextComponent)
  {
    JEditorPane editorPane = new JEditorPane();
    if (pTextComponent instanceof JEditorPane)
    {
      editorPane.setEditorKit(((JEditorPane) pTextComponent).getEditorKit());
    }
    if (pRollbackInformation.getReplacement().endsWith("\n"))
      editorPane.setText(pRollbackInformation.getReplacement().substring(0, pRollbackInformation.getReplacement().length() - 1));
    else
      editorPane.setText(pRollbackInformation.getReplacement());
    editorPane.setEditable(false);
    scrollPane = new JScrollPane(editorPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    InputMap iMap = editorPane.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap aMap = editorPane.getActionMap();
    iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "ctrlV");
    aMap.put("ctrlV", new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(
                new StringSelection(editorPane.getSelectedText()),
                null
            );
      }
    });
    add(scrollPane, BorderLayout.CENTER);
    toolBar = _initToolbar(pRollbackInformation, pTextComponent);
    add(toolBar, BorderLayout.NORTH);
  }

  private JToolBar _initToolbar(_RollbackInformation pRollbackInformation, JTextComponent pTextComponent)
  {
    JButton button = new JButton(iconLoader.getIcon(Constants.ROLLBACK_ICON));
    button.setToolTipText("Undo changes");
    button.addActionListener(e -> {
      _performRollback(pRollbackInformation, pTextComponent);
      windowDisposer.disposeWindow();
    });
    toolBar = new JToolBar(SwingConstants.HORIZONTAL);
    toolBar.setFloatable(false);
    toolBar.add(button);
    JButton nextChangeButton = new JButton(iconLoader.getIcon(Constants.NEXT_OCCURRENCE));
    IFileChangeChunk nextChunk = IFileChanges.getNextChangedChunk(this.changeChunk, changeChunkList.blockingFirst());
    if (nextChunk != null)
    {
      nextChangeButton.addActionListener(new MoveChunkActionListener(pTextComponent, nextChunk));
    }
    else
    {
      nextChangeButton.setEnabled(false);
    }
    JButton previousChangeButton = new JButton(iconLoader.getIcon(Constants.PREVIOUS_OCCURRENCE));
    IFileChangeChunk previousChunk = IFileChanges.getPreviousChangedChunk(changeChunk, changeChunkList.blockingFirst());
    if (previousChunk != null)
    {
      previousChangeButton.addActionListener(new MoveChunkActionListener(pTextComponent, previousChunk));
    }
    else
    {
      previousChangeButton.setEnabled(false);
    }
    toolBar.add(nextChangeButton);
    toolBar.add(previousChangeButton);
    return toolBar;
  }

  /**
   * @param pRollbackInformation Information about which parts of the text to delete and what to insert
   * @param pTextComponent       JTextComponent on which to perform the rollback
   */
  private void _performRollback(_RollbackInformation pRollbackInformation, JTextComponent pTextComponent)
  {
    try
    {
      pTextComponent.getDocument().remove(pRollbackInformation.getStartOffset(),
                                          Math.min(pRollbackInformation.getLength(), pTextComponent.getDocument().getLength() - pRollbackInformation.getStartOffset()));
      pTextComponent.getDocument().insertString(pRollbackInformation.getStartOffset(), pRollbackInformation.getReplacement(), null);
    }
    catch (BadLocationException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * @param pRepo          IRepository from which to retrieve the HEAD version of the file
   * @param pChangeChunk   IFileChangeChunk describing the changed lines
   * @param pTextComponent JTextComponent on which potentially perform the roll back of the lines in the IFileChangeChunk
   * @param pFile          File that is opened in the JTextComponent, needed to retrieve the version in HEAD
   * @return _RollbackInformation with which the roll back can be performed
   */
  private _RollbackInformation _calculateRollbackInfo(IRepository pRepo, IFileChangeChunk pChangeChunk, JTextComponent pTextComponent,
                                                      File pFile)
  {
    int startOffset;
    int length;
    String content;
    try
    {
      startOffset = pTextComponent.getDocument().getDefaultRootElement().getElement(pChangeChunk.getStart(EChangeSide.NEW)).getStartOffset();
      int endOffset = pTextComponent.getDocument().getDefaultRootElement().getElement(pChangeChunk.getEnd(EChangeSide.NEW) - 1).getEndOffset();
      if (endOffset <= startOffset)
        length = 0;
      else
        length = endOffset - startOffset - 1;
      content = pRepo.getFileContents(pRepo.getFileVersion(pRepo.getCommit(null).getId(),
                                                           pRepo.getTopLevelDirectory().toURI().relativize(pFile.toURI()).getPath())).getFileContent();
    }
    catch (AditoGitException | IOException pE)
    {
      throw new RuntimeException(pE);
    }
    // If it is an insert, remove the newline at the end as well (because else the newline is still an insertion)
    if (pChangeChunk.getEnd(EChangeSide.OLD) == pChangeChunk.getStart(EChangeSide.OLD))
      length += 1;
    return new _RollbackInformation(startOffset, length, _getAffectedContents(content, pChangeChunk));
  }

  /**
   * @param pContents    Original contents of the file, as in HEAD
   * @param pChangeChunk IFileChangeChunk with information about the affected lines
   * @return String containing all the lines marked as affected by the IFileChangeChunk
   */
  private String _getAffectedContents(String pContents, IFileChangeChunk pChangeChunk)
  {
    if (pContents.contains("\n"))
    {
      pContents = pContents.replace("\r", "");
    }
    else
    {
      pContents = pContents.replace("\r", "\n");
    }
    String[] lines = pContents.replace("\r", "").split("\n");
    StringBuilder builder = new StringBuilder();
    for (int index = pChangeChunk.getStart(EChangeSide.OLD); index < pChangeChunk.getEnd(EChangeSide.OLD); index++)
    {
      builder.append(lines[index]).append("\n");
    }
    if (pChangeChunk.getChangeType() == EChangeType.MODIFY)
      builder.deleteCharAt(builder.lastIndexOf("\n"));
    return builder.toString();
  }

  /**
   * @param pRollbackInformation RollbackInformation for the lines written in the JTextComponent
   * @param pFontMetrics         FontMetrics used in the JTextComponent
   * @return Dimension with minimum size for this window
   */
  private Dimension _calculateSize(_RollbackInformation pRollbackInformation, FontMetrics pFontMetrics)
  {
    int minWidth = DEFAULT_MIN_WIDTH;
    String[] lines = pRollbackInformation.getReplacement().split("\n");
    for (String line : lines)
    {
      if (pFontMetrics.stringWidth(line) > minWidth)
        minWidth = pFontMetrics.stringWidth(line);
    }
    int minHeight = pFontMetrics.getHeight() * lines.length;
    return new Dimension(minWidth + INSET_RIGHT + scrollPane.getInsets().left + scrollPane.getInsets().right,
                         (minHeight > MIN_HEIGHT ? minHeight : MIN_HEIGHT) + scrollPane.getInsets().top + scrollPane.getInsets().bottom);
  }

  /**
   * AWTEventListener that disposes of the window if any mouse/keyboard/windowAction happens outside this component or any of its children
   */
  private class _WindowDisposer implements AWTEventListener
  {

    @Override
    public void eventDispatched(AWTEvent pEvent)
    {
      if (!_isSourceRecursive(pEvent, ChunkPopupWindow.this))
      {
        if (pEvent instanceof MouseEvent && (pEvent.getID() == MouseEvent.MOUSE_CLICKED || pEvent.getID() == MouseEvent.MOUSE_WHEEL))
        {
          disposeWindow();
        }
        if (pEvent instanceof WindowEvent && pEvent.getID() != WindowEvent.WINDOW_OPENED
            && !(pEvent.getID() == WindowEvent.WINDOW_CLOSED && pEvent.getSource() instanceof ChunkPopupWindow))
        {
          disposeWindow();
        }
        if (pEvent instanceof KeyEvent)
        {
          disposeWindow();
        }
      }
      else if (pEvent instanceof KeyEvent && ((KeyEvent) pEvent).getKeyCode() == KeyEvent.VK_ESCAPE)
      {
        disposeWindow();
      }
    }

    /**
     * removes the AWTEventListener and disposes of the ChunkPopupWindow
     */
    void disposeWindow()
    {
      Toolkit.getDefaultToolkit().removeAWTEventListener(this);
      dispose();
    }

    /**
     * Checks if the container or any of its components (or the components components or...)
     * is the source of the event.
     * This is a recursive function
     *
     * @param pAWTEvent  event to look for
     * @param pContainer Container to check
     * @return true if event originated in the Container or any of its children, false otherwise
     */
    private boolean _isSourceRecursive(AWTEvent pAWTEvent, Container pContainer)
    {
      for (Component component : pContainer.getComponents())
      {
        if (pAWTEvent.getSource() == component || (component instanceof Container && _isSourceRecursive(pAWTEvent, (Container) component)))
          return true;
      }
      return false;
    }
  }

  private class MoveChunkActionListener implements ActionListener
  {

    private final JTextComponent textComponent;
    private final IFileChangeChunk moveTo;

    public MoveChunkActionListener(JTextComponent pTextComponent, IFileChangeChunk pMoveTo)
    {
      textComponent = pTextComponent;
      moveTo = pMoveTo;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      IDiffPaneUtil.moveCaretToChunk(textComponent, moveTo, EChangeSide.NEW);
      try
      {
        editorColorizer.showPopupForChunk(moveTo);
      }
      catch (BadLocationException pE)
      {
        // nothing, no popup is shown
      }
    }
  }

  /**
   * Stores Information for rolling back a changed part of a file to the HEAD state
   */
  private class _RollbackInformation
  {

    private final int startOffset;
    private final int length;
    private final String replacement;

    _RollbackInformation(int pStartOffset, int pLength, String pReplacement)
    {
      startOffset = pStartOffset;
      length = pLength;
      replacement = pReplacement;
    }

    /**
     * @return offset at which the delete/insert begins
     */
    int getStartOffset()
    {
      return startOffset;
    }

    /**
     * @return length of the text to delete
     */
    int getLength()
    {
      return length;
    }

    /**
     * @return String with text that should be inserted after the deletion occurred
     */
    String getReplacement()
    {
      return replacement;
    }
  }

}
