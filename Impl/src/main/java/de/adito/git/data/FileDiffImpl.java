package de.adito.git.data;

import de.adito.git.EnumMappings;
import de.adito.git.GitRepositoryProvider;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.api.data.*;
import de.adito.git.data.FileChangesImpl;
import de.adito.git.data.LineChangeImpl;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
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
    private int changedStartLine = -1;
    private int changedEndLine = -1;
    private FileChangesImpl fileChanges = null;

    public FileDiffImpl(DiffEntry pDiffEntry) {
        diffEntry = pDiffEntry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChangeStartLine() {
        if (changedStartLine != -1) {
            return changedStartLine;
        } else {
            _setChangedLines();
            return changedStartLine;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChangeEndLine() {
        if (changedEndLine != -1) {
            return changedEndLine;
        } else {
            _setChangedLines();
            return changedEndLine;
        }
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
                formatter.setDetectRenames(true);
                formatter.format(diffEntry);
                // if the changedStart/EndLine is not yet set do so since we already created the diffFormatter
                fileChanges = new FileChangesImpl(_getLineChanges(bout.toString()));
                if (changedStartLine == -1) {
                    FileHeader fileHeader = formatter.toFileHeader(diffEntry);
                    changedStartLine = fileHeader.getStartOffset();
                    changedEndLine = fileHeader.getEndOffset();
                }
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

    /**
     * sets the start/endlines of changed part of the file, so that the {@link DiffFormatter} doesn't have to be
     * set up each time they are queried
     */
    private void _setChangedLines() {
        try (DiffFormatter formatter = new DiffFormatter(null)) {
            formatter.setRepository(GitRepositoryProvider.get());
            formatter.setDetectRenames(true);
            FileHeader fileHeader = formatter.toFileHeader(diffEntry);
            changedStartLine = fileHeader.getStartOffset();
            changedEndLine = fileHeader.getEndOffset();
            //TODO exception handling
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
