package de.adito.git.impl.data;

import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

/**
 * @author m.kaspera 25.09.2018
 */
public class BranchImpl implements IBranch {
    private Ref branchRef;
    private EBranchType branchType;

    public BranchImpl(Ref pBranchRef) {
        branchRef = pBranchRef;
        String[] split = branchRef.getName().split("/");
        if (split[1].equals("remotes")) {
            branchType = EBranchType.REMOTE;
        } else
            branchType = EBranchType.LOCAL;
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
     * {@inheritDoc}
     */
    @Override
    // TODO: 24.10.2018
    public String getSimpleName() {
        String simpleName = null;
        String name = getName();

        String[] split = name.split("/");
        simpleName = split[split.length - 1];
        return simpleName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EBranchType getType() {
        return branchType;
    }
}
