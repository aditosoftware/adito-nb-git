package de.adito.git.gui;

import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog/Panel for displaying the merge-conflicts
 * Also offers the possibility of accepting changes from both the
 * merge-base/current side and the side that is being merged
 *
 * @author m.kaspera 22.10.2018
 */
public class MergeDialog extends JPanel implements IDiscardable {

    private final List<IMergeDiff> mergeDiffs;
    private final JTextPane currentBranchVersionPane = new JTextPane();
    private final JTextPane forkPointVersionPane = new JTextPane();
    private final JTextPane mergeBranchVersionPane = new JTextPane();
    private Disposable currentBranchDisposable;
    private Disposable mergeBranchDisposeable;

    public MergeDialog(List<IMergeDiff> pMergeDiffs){
        mergeDiffs = pMergeDiffs;
        _initGui();
        _writeText();
    }

    private void _initGui() {
        // create a panel in a scrollPane for each of the textPanes
        setLayout(new BorderLayout());
        JPanel currentBranchPanel = new JPanel(new BorderLayout());
        JPanel forkPointPanel = new JPanel(new BorderLayout());
        JPanel mergeBranchPanel = new JPanel(new BorderLayout());
        currentBranchPanel.add(currentBranchVersionPane, BorderLayout.CENTER);
        forkPointPanel.add(forkPointVersionPane);
        mergeBranchPanel.add(mergeBranchVersionPane);
        JScrollPane currentBranchScrollPane = new JScrollPane(currentBranchPanel);
        JScrollPane forkPointScrollPane = new JScrollPane(forkPointPanel);
        JScrollPane mergeBranchScrollPane = new JScrollPane(mergeBranchPanel);

        // add a splitPane on top of another splitPane so there are three sub-windows for each textPane/version of the file
        JSplitPane currentToForkSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, currentBranchScrollPane, forkPointScrollPane);
        JSplitPane threeWayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, currentToForkSplit, mergeBranchScrollPane);
        currentToForkSplit.setResizeWeight(0.5);
        // 0.66 because the left side contains two sub-windows, the right only one
        threeWayPane.setResizeWeight(0.66);
        JPanel diffPanel = new JPanel(new BorderLayout());
        diffPanel.add(threeWayPane, BorderLayout.CENTER);
        add(diffPanel, BorderLayout.CENTER);
    }

    private void _writeText() {
        for(IMergeDiff mergeDiff: mergeDiffs){
            // each time the observable fires, reset the text in the textPane and construct it anew
            currentBranchDisposable = mergeDiff.getBaseSideDiff().getFileChanges().getChangeChunks().subscribe(changeChunks -> {
                // reset the text, else the text will just be appended to the textPane after each accepted change
               currentBranchVersionPane.setText("");
               forkPointVersionPane.setText("");
               for(IFileChangeChunk changeChunk: changeChunks){
                   TextHighlightUtil.insertText(currentBranchVersionPane.getStyledDocument(), changeChunk.getBLines(), changeChunk.getChangeType());
                   TextHighlightUtil.insertText(forkPointVersionPane.getStyledDocument(), changeChunk.getALines(), Color.WHITE);
               }
            });
            // each time the observable fires, reset the text in the textPane and construct it anew
            mergeBranchDisposeable = mergeDiff.getMergeSideDiff().getFileChanges().getChangeChunks().subscribe(changeChunks -> {
                // reset the text, else the text will just be appended to the textPane after each accepted change
                mergeBranchVersionPane.setText("");
                for (IFileChangeChunk changeChunk : changeChunks) {
                    TextHighlightUtil.insertText(mergeBranchVersionPane.getStyledDocument(), changeChunk.getBLines(), changeChunk.getChangeType());
                }
            });
        }
    }

    @Override
    public void discard() {
        if(currentBranchDisposable != null && !currentBranchDisposable.isDisposed()){
            currentBranchDisposable.dispose();
        }
        if(mergeBranchDisposeable != null && !mergeBranchDisposeable.isDisposed()){
            mergeBranchDisposeable.dispose();
        }
    }
}
