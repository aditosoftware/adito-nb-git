package de.adito.git.api;

import de.adito.git.api.data.ICommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the AncestryLines currently running for a commit
 * This allows a display of the branching and merging of the commits over time
 *
 * @author m.kaspera 16.11.2018
 */
public class CommitHistoryTreeListItem {

    private final ICommit commit;
    private final List<AncestryLine> ancestryLines;
    private final ColorRoulette colorRoulette;

    public CommitHistoryTreeListItem(@NotNull ICommit commit, @NotNull List<AncestryLine> ancestryLines, @NotNull ColorRoulette pColorRoulette) {
        this.commit = commit;
        this.ancestryLines = ancestryLines;
        colorRoulette = pColorRoulette;
    }

    /**
     * Provided with the next commit in the List, this method calculates the
     * CommitHistoryTreeListItem following this CommitHistoryTreeListItem
     *
     * @param pNext ICommit that follows the ICommit of this CommitHistoryTreeListItem in the log
     * @return ICommitHistoryTreeListItem that has the information for the next ICommit in the log
     */
    public CommitHistoryTreeListItem nextItem(@NotNull ICommit pNext, @Nullable ICommit pAfterNext) {
        List<AncestryLine> updatedAncestryLines = new ArrayList<>();
        /*
         gather candidates for STILLBORN lines here with the index of the AncestryLines that will spawn them.
         Gathering them is done because in order to identify them, all AncestryLines have to be known (else it is
         uncertain if the line is potentially the only one leading to the next commit and thus not STILLBORN)
          */
        Map<Integer, AncestryLine> checkForStillborns = new HashMap<>();
        // since several AncestryLines leading to the same commit only spawn lines once, remember if we already processed their children
        boolean processedChildren = false;
        // counter and foreach loop instead of counting loop because better readability
        int counter = 0;
        for (AncestryLine oldAncestryLine : ancestryLines) {
            // throw out STILLBORN lines since they end in the very next line after they're born
            if (!(oldAncestryLine.getLineType() == AncestryLine.LineType.STILLBORN)) {
                // Candidates for STILLBORN lines are lines whose parent equals the next commit
                if (oldAncestryLine.getParent().equals(pNext)) {
                    checkForStillborns.put(counter, oldAncestryLine);
                }
                // the only lines that are advanced are those whose parent is the current commit (lines do not fork/split/end spontaneously, only after encountering their parent commit
                if (oldAncestryLine.getParent().equals(commit)) {
                    if (!processedChildren) {
                        processedChildren = true;
                        for (AncestryLine childLine : oldAncestryLine.getChildLines()) {
                            if (childLine.getLineType() == AncestryLine.LineType.STILLBORN) {
                                updatedAncestryLines.add(new AncestryLine(childLine.getParent(),
                                        childLine.getColor(), AncestryLine.LineType.STILLBORN, childLine.getStillBornMeetingIndex(), colorRoulette));
                            } else {
                                AncestryLine newAncestryLine = new AncestryLine(childLine.getParent(), childLine.getColor(), colorRoulette);
                                // check again for parent matching the next commit here, since if the parent being the current commit can hide the next commit being the parent of the child line
                                if (newAncestryLine.getParent().equals(pNext))
                                    checkForStillborns.put(counter, newAncestryLine);
                                updatedAncestryLines.add(newAncestryLine);
                            }
                        }
                    } else {
                        colorRoulette.returnColor(oldAncestryLine.getColor());
                    }
                } else {
                    updatedAncestryLines.add(oldAncestryLine);
                }
            } else {
                colorRoulette.returnColor(oldAncestryLine.getColor());
            }
            counter++;
        }
        // All new AncestryLines are known now, so the candidates for being STILLBORN can now be verified
        for (Integer index : checkForStillborns.keySet()) {
            checkForStillborns.get(index).hasStillbornChildren(pAfterNext, updatedAncestryLines, index);
        }
        return new CommitHistoryTreeListItem(pNext, updatedAncestryLines, colorRoulette);
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
}
