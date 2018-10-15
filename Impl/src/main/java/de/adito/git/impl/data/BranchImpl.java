package de.adito.git.impl.data;

import de.adito.git.api.data.IBranch;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

/**
 * @author m.kaspera 25.09.2018
 */
public class BranchImpl implements IBranch {

    private Ref branchRef;

    public BranchImpl(Ref pBranchRef) {
        branchRef = pBranchRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return branchRef.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return ObjectId.toString(branchRef.getObjectId());
    }

    /**
     *
     *{@inheritDoc}
     */
    @Override
    public String getSimpleName(IBranch branch) {
        String simpleName = null;
        String name = branch.getName();

        String[] split = name.split("/");
        simpleName = split[split.length-1];
        return simpleName;
    }
}
