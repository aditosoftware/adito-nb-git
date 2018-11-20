package de.adito.git.api.data;

/**
 * An empty branch object
 *
 * @author a.arnold, 05.11.2018
 */
class EmptyBranch implements IBranch {
    @Override
    public String getName() {
        throw new RuntimeException();
    }

    @Override
    public String getId() {
        throw new RuntimeException();
    }

    @Override
    public String getSimpleName() {
        throw new RuntimeException();
    }

    @Override
    public EBranchType getType() {
        throw new RuntimeException();
    }
}
