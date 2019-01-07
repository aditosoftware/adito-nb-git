package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.*;
import de.adito.git.gui.*;
import de.adito.git.gui.dialogs.panels.TextPanes.DiffPaneWrapper;
import de.adito.git.impl.data.FileChangeChunkImpl;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.util.Optional;

/**
 * Class to handle the basic layout of the two panels that display the differences between two files
 *
 * @author m.kaspera 12.11.2018
 */
public class DiffPanel extends JPanel implements IDiscardable
{
  private final BaseDiffPanel currentVersionPanel;
  private final BaseDiffPanel oldVersionPanel;

  public DiffPanel(Observable<Optional<IFileDiff>> pFileDiffObs, ImageIcon pAcceptIcon, IEditorKitProvider pEditorKitProvider)
  {

    DiffPanelModel currentDiffPanelModel = new DiffPanelModel(pFileDiffObs, pFileChunk -> pFileChunk.getBEnd() - pFileChunk.getBStart(),
                                                              IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines);
    DiffPaneWrapper currentVersionDiffPane = new DiffPaneWrapper(currentDiffPanelModel, pEditorKitProvider);
    currentVersionPanel = new BaseDiffPanel(currentVersionDiffPane.getPane(), currentVersionDiffPane.getTextPane());

    DiffPanelModel oldDiffPanelModel = new DiffPanelModel(pFileDiffObs, pFilChunk -> pFilChunk.getAEnd() - pFilChunk.getAStart(),
                                                          IFileChangeChunk::getALines, IFileChangeChunk::getAParityLines);
    DiffPaneWrapper oldVersionDiffPane = new DiffPaneWrapper(oldDiffPanelModel, pEditorKitProvider);
    oldVersionPanel = new BaseDiffPanel(oldVersionDiffPane.getPane(), oldVersionDiffPane.getTextPane());

    currentVersionPanel.addLineNumPanel(currentDiffPanelModel, BorderLayout.WEST);
    oldVersionPanel.addLineNumPanel(oldDiffPanelModel, BorderLayout.EAST);
    oldVersionPanel.addChoiceButtonPanel(oldDiffPanelModel, null, pAcceptIcon,
                                         pFileChangeChunk -> pFileDiffObs.blockingFirst().ifPresent(pFileDiff -> pFileDiff.getFileChanges()
                                             .replace(pFileChangeChunk,
                                                      new FileChangeChunkImpl(pFileChangeChunk, EChangeType.SAME), true)),
                                         pChangeChunk -> pFileDiffObs.blockingFirst()
                                             .ifPresent(pFileDiff -> pFileDiff.getFileChanges().resetChanges(pChangeChunk)),
                                         BorderLayout.EAST);
    oldVersionPanel.coupleToScrollPane(currentVersionPanel.getMainScrollPane());
    setLayout(new BorderLayout());
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldVersionPanel, currentVersionPanel);
    mainSplitPane.setResizeWeight(0.5);
    setBorder(new JScrollPane().getBorder());
    add(mainSplitPane, BorderLayout.CENTER);
  }

  @Override
  public void discard()
  {
    currentVersionPanel.discard();
    oldVersionPanel.discard();
  }
}