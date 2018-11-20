package de.adito.git.api.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the AncestryLines currently running for a commit
 * This allows a display of the branching and merging of the commits over time
 *
 * @author m.kaspera 16.11.2018
 */
public class CommitHistoryTreeListItem {

    private static int maxWidth;
    private final ICommit commit;
    private final List<AncestryLine> ancestryLines;

    public CommitHistoryTreeListItem(ICommit commit, List<AncestryLine> ancestryLines) {
        this.commit = commit;
        this.ancestryLines = ancestryLines;
    }

    /**
     * Provided with the next commit in the List, this method calculates the
     * CommitHistoryTreeListItem following this CommitHistoryTreeListItem
     *
     * @param pNext ICommit that follows the ICommit of this CommitHistoryTreeListItem in the log
     * @return ICommitHistoryTreeListItem that has the information for the next ICommit in the log
     */
    public CommitHistoryTreeListItem nextItem(ICommit pNext) {
        List<AncestryLine> updatedAncestryLines = new ArrayList<>();
        boolean processedChildren = false;
        for (AncestryLine oldAncestryLine : ancestryLines) {
            if (oldAncestryLine.getParent().equals(commit)) {
                if (!processedChildren) {
                    processedChildren = true;
                    for (AncestryLine childLine : oldAncestryLine.getChildLines()) {
                        updatedAncestryLines.add(new AncestryLine(childLine.getParent(), childLine.getColor()));
                    }
                }
            } else {
                updatedAncestryLines.add(oldAncestryLine);
            }
        }
        CommitHistoryTreeListItem.setMaxWidth(updatedAncestryLines.size());
        return new CommitHistoryTreeListItem(pNext, updatedAncestryLines);
    }

    /**
     * @return the commit for which the AncestryLines were gathered
     */
    public ICommit getCommit() {
        return commit;
    }

    /**
     * @return the List of AncestryLines that are running for this commit
     */
    public List<AncestryLine> getAncestryLines() {
        return ancestryLines;
    }

    /**
     * @return the maximum encountered number of parallel ancestryLines
     */
    public static int getMaxWidth() {
        return maxWidth;
    }

    /**
     * @param maxWidth the new maximum number of parallel ancestryLines. If smaller than the current maximum no effect
     */
    public static void setMaxWidth(int maxWidth) {
        if (maxWidth > CommitHistoryTreeListItem.maxWidth)
            CommitHistoryTreeListItem.maxWidth = maxWidth;
    }
}
