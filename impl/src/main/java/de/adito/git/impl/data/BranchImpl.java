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
    private String simpleName = null;

    public BranchImpl(Ref pBranchRef) {
        branchRef = pBranchRef;
        branchType = EBranchType.EMPTY;
        String[] split = branchRef.getName().split("/");
        if (split.length > 1) {
            if (split[1].equals("remotes")) {
                branchType = EBranchType.REMOTE;
            }
            if (split[1].equals("heads")) {
                branchType = EBranchType.LOCAL;
            }
        }
        if (split.length == 1 && branchRef.getName().equals("DETACHED")) {
            branchType = EBranchType.DETACHED;
            simpleName = getId();
        }
    }
    public BranchImpl(ObjectId pId){
        branchType = EBranchType.DETACHED;
        simpleName = ObjectId.toString(pId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        if(branchType == EBranchType.DETACHED){
            return simpleName;
        }
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if(EBranchType.DETACHED == this.getType())
            return this.getId();
        return branchRef.getName();
    }
}
