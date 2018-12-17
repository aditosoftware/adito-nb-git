package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.TextPanes.DiffTextPaneWrapper;
import de.adito.git.impl.data.FileChangeChunkImpl;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.BorderLayout;
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

  public DiffPanel(Observable<Optional<IFileDiff>> pFileDiffObs, ImageIcon pAcceptIcon)
  {
    DiffPanelModel currentDiffPanelModel = new DiffPanelModel(pFileDiffObs, pFileChunk -> pFileChunk.getBEnd() - pFileChunk.getBStart(),
                                                              IFileChangeChunk::getBLines, IFileChangeChunk::getBParityLines);
    DiffPanelModel oldDiffPanelModel = new DiffPanelModel(pFileDiffObs, pFilChunk -> pFilChunk.getAEnd() - pFilChunk.getAStart(),
                                                          IFileChangeChunk::getALines, IFileChangeChunk::getAParityLines);
    currentVersionPanel = new BaseDiffPanel(new DiffTextPaneWrapper(currentDiffPanelModel).getTextPane());
    oldVersionPanel = new BaseDiffPanel(new DiffTextPaneWrapper(oldDiffPanelModel).getTextPane());
    currentVersionPanel.addLineNumPanel(currentDiffPanelModel, BorderLayout.WEST);
    oldVersionPanel.addLineNumPanel(oldDiffPanelModel, BorderLayout.EAST);
    oldVersionPanel.addChoiceButtonPanel(oldDiffPanelModel, null, pAcceptIcon,
                                         pFileChangeChunk -> pFileDiffObs.blockingFirst().ifPresent(pFileDiff -> pFileDiff.getFileChanges()
                                             .replace(pFileChangeChunk,
                                                      new FileChangeChunkImpl(pFileChangeChunk, EChangeType.SAME))),
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