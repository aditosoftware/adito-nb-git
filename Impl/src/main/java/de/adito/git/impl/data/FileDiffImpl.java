package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.patch.FileHeader;

/**
 * Represents information about the uncovered changes by the diff command
 *
 * @author m.kaspera 21.09.2018
 */
public class FileDiffImpl implements IFileDiff {

    private DiffEntry diffEntry;
    private FileHeader fileHeader;
    private FileChangesImpl fileChanges;
    private String originalFileContents;
    private String newFileContents;

    public FileDiffImpl(DiffEntry pDiffEntry, FileHeader pFileHeader, String pOriginalFileContents, String pNewFileContents) {
        diffEntry = pDiffEntry;
        fileHeader = pFileHeader;
        originalFileContents = pOriginalFileContents;
        newFileContents = pNewFileContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId(EChangeSide side) {
        return (side == EChangeSide.NEW ? diffEntry.getNewId() : diffEntry.getOldId()).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EChangeType getChangeType() {
        return EnumMappings._toEChangeType(diffEntry.getChangeType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EFileType getFileType(EChangeSide side) {
        return EnumMappings._toEFileType(side == EChangeSide.NEW ? diffEntry.getMode(DiffEntry.Side.NEW) : diffEntry.getMode(DiffEntry.Side.OLD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilePath(EChangeSide side) {
        return side == EChangeSide.NEW ? diffEntry.getNewPath() : diffEntry.getOldPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileChangesImpl getFileChanges() {
        if (fileChanges == null) {
            EditList edits = fileHeader.getHunks().get(0).toEditList();
            fileChanges = new FileChangesImpl(edits, originalFileContents, newFileContents);
        }
        return fileChanges;
    }
}
