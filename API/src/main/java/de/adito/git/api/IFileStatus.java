package de.adito.git.api;

import de.adito.git.api.data.EStageState;

import java.util.Map;
import java.util.Set;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IFileStatus {

    /**
     *
     * @return {@code true} wenn keine Unterschiede zwischen Head,
     *          index und dem working-tree existieren
     */
    boolean isClean();

    /**
     *
     * @return {@code true} wenn sich ein getracktes File geändert hat
     */
    boolean hasUncommittedChanges();

    /**
     *
     * @return Liste mit Files die im Index sind aber nicht im HEAD,
     *          z.B. neu angelegtes File das mit {@code git add ...} zum Index hinzugefügt wurde
     */
    Set<String> getAdded();

    /**
     *
     * @return Liste mit Files die sich von Index zu HEAD unterscheiden,
     *          z.B. ein geändertes existierendes File das mit {@code git add ...} zum Index hinzugefügt wurde
     */
    Set<String> getChanged();

    /**
     *
     * @return Liste mit Files die vom Index entfernt wurden, aber im HEAD liegen,
     *          z.B. ein File das mit {@code git rm ...} entfernt wurde
     */
    Set<String> getRemoved();

    /**
     *
     * @return Liste mit Files die im Index liegen aber nicht mehr im lokalen Filesystem,
     *          z.B. Files die mit {@code rm ...} entfernt wurden
     */
    Set<String> getMissing();

    /**
     *
     * @return Liste mit Files die sich relativ zum Index verändert haben,
     *          z.B. Files die lokal verändert wurden aber noch nicht zum Index hinzugefügt wurden
     */
    Set<String> getModified();

    /**
     *
     * @return Liste mit Files die nicht ignoriert wurden und nicht im Index liegen,
     *          z.B. ein neu angelegtes File das noch nicht zum Index hinzugefügt wurde
     */
    Set<String> getUntracked();

    /**
     *
     * @return Set an Ordnern die nicht ignoriert wurden und sich nicht im Index befinden
     */
    Set<String> getUntrackedFolders();

    /**
     *
     * @return Set mit Files die sich im Konfliktzustand befinden,
     *          z.B. wenn ein File lokal verändert wurde das in der Zwischenzeit von einer anderen Person bearbeitet wurde
     */
    Set<String> getConflicting();

    /**
     *
     * @return Map die Files im Konfliktzustand auf ihre {@link EStageState} mappt
     */
    Map<String, EStageState> getConflictingStageState();

    /**
     *
     * @return Set mit Ordnern und Files die ignoriert sind und sich nicht im Index befinden
     */
    Set<String> getIgnoredNotInIndex();

    /**
     *
     * @return Set mit sämtlichen uncommiteten Änderungen, also sämtliche Files die sich relativ zum Index
     *          verändert haben
     */
    Set<String> getUncommittedChanges();
}
