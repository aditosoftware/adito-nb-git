package de.adito.git.gui.dialogs;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IBeforeCommitAction;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.DelayedSupplier;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import de.adito.git.impl.data.FileChangeTypeImpl;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.tree.TreePath;
import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link CommitDialog}.
 *
 * @author r.hartinger, 17.11.2022
 */
class CommitDialogTest
{

  /**
   * Tests if the selected Files are correctly detected and old results are thrown away.
   */
  @Test
  void testDetectSelectedFiles()
  {
    IDialogDisplayer.IDescriptor pIsValidDescriptor = Mockito.mock(IDialogDisplayer.IDescriptor.class);
    Observable<Optional<IRepository>> pRepository = Observable.just(Optional.empty());
    Observable<Optional<List<IFileChangeType>>> pFilesToCommit = Observable.just(Optional.empty());
    DelayedSupplier<List<File>> pDelayedSupplier = new DelayedSupplier<>();
    DelayedSupplier<List<IBeforeCommitAction>> pSelectedActionsSupplier = new DelayedSupplier<>();

    CommitDialog commitDialog = new CommitDialog(null, null, null, null, null, null, pIsValidDescriptor, pRepository, pFilesToCommit, null, pSelectedActionsSupplier, pDelayedSupplier);

    File packageJson = new File("c:/projects/basic/package.json");
    File entityValueChange = new File("c:/projects/basic/entity/My_entity/entityfields/myfield/onValueChange.js");

    commitDialog.getCheckBoxTree().getCheckBoxTreeSelectionModel().addSelectionPaths(new TreePath[]{createTreePath(packageJson), createTreePath(entityValueChange)});

    List<File> selectedFiles = new ArrayList<>();
    selectedFiles.add(new File("c:/projects/basic/readme.md"));


    commitDialog._detectSelectedFiles(selectedFiles);
    Collections.sort(selectedFiles);

    List<File> expected = Arrays.asList(packageJson, entityValueChange);
    Collections.sort(expected);
    assertEquals(expected, selectedFiles);
  }

  /**
   * Creates a simple TreePath. Only the file is relevant in this case.
   *
   * @param nodeFile the file of the FileChangeTypeNodeInfo
   * @return the created tree path
   */
  @NonNull
  private static TreePath createTreePath(@NonNull File nodeFile)
  {
    IFileChangeType iFileChangeType = new FileChangeTypeImpl(nodeFile, nodeFile, EChangeType.CHANGED);
    return new TreePath(new FileChangeTypeNode(new FileChangeTypeNodeInfo("not relevant", new File("C:/should/not/be/used"), List.of(iFileChangeType))));
  }
}