package de.adito.git.impl.data;

import de.adito.git.impl.EnumMappings;
import de.adito.git.impl.GitRepositoryProvider;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.patch.FileHeader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents information about the uncovered changes by the diff command
 *
 * @author m.kaspera 21.09.2018
 */
public class FileDiffImpl implements IFileDiff {

    private DiffEntry diffEntry;
    private FileChangesImpl fileChanges;
    private FileHeader fileHeader;

    public FileDiffImpl(DiffEntry pDiffEntry) {
        diffEntry = pDiffEntry;
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
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (DiffFormatter formatter = new DiffFormatter(bout)) {
                formatter.setRepository(GitRepositoryProvider.get());
                FileHeader fileHeader = formatter.toFileHeader(diffEntry);
                EditList edits = fileHeader.getHunks().get(0).toEditList();
                formatter.setDetectRenames(true);
                formatter.format(diffEntry);
                fileChanges = new FileChangesImpl(_getLineChanges(bout.toString()), edits);
                //TODO exception handling
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileChanges;
    }

    /**
     * @param pFormattedString String with output from the DiffFormatter.format for one file
     * @return List of {@link LineChangeImpl} that contains the changes from the String in a queryable format
     */
    @NotNull
    private List<ILineChange> _getLineChanges(@NotNull String pFormattedString) {
        List<ILineChange> lineChanges = new ArrayList<>();
        for (String formattedLine : pFormattedString.split("\n")) {
            if(formattedLine.startsWith("+") && !formattedLine.startsWith("+++"))
                lineChanges.add(new LineChangeImpl(EChangeType.ADD, formattedLine.substring(1)));
            else if(formattedLine.startsWith("-") && !formattedLine.startsWith("---"))
                lineChanges.add(new LineChangeImpl(EChangeType.DELETE, formattedLine.substring(1)));
        }
        return lineChanges;
    }

}
