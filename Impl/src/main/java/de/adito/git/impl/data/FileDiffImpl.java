package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.impl.EnumMappings;
import de.adito.git.impl.GitRepositoryProvider;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.patch.FileHeader;

import java.io.IOException;

/**
 * Represents information about the uncovered changes by the diff command
 *
 * @author m.kaspera 21.09.2018
 */
public class FileDiffImpl implements IFileDiff {

    private DiffEntry diffEntry;
    private FileChangesImpl fileChanges;
    private String originalFileContents;
    private String newFileContents;

    public FileDiffImpl(DiffEntry pDiffEntry, String pOriginalFileContents, String pNewFileContents) {
        diffEntry = pDiffEntry;
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
            try (DiffFormatter formatter = new DiffFormatter(null)) {
                formatter.setRepository(GitRepositoryProvider.get());
                FileHeader fileHeader = formatter.toFileHeader(diffEntry);
                EditList edits = fileHeader.getHunks().get(0).toEditList();
                formatter.setDetectRenames(true);
                fileChanges = new FileChangesImpl(edits, originalFileContents, newFileContents);
                //TODO exception handling
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileChanges;
    }
}
