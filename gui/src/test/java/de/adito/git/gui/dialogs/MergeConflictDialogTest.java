package de.adito.git.gui.dialogs;

import de.adito.git.api.*;
import de.adito.git.api.data.IMergeDetails;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.impl.data.MergeDetailsImpl;
import de.adito.git.impl.data.diff.ResolveOptionsProvider;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link MergeConflictDialog}.
 *
 * @author r.hartinger, 17.01.2023
 */
class MergeConflictDialogTest
{

  /**
   * Tests the update of the remaining conflicts label
   */
  @Nested
  class RemainingConflictsLabel
  {
    /**
     * Tests that the label will be updated after removing elements.
     * The change needs to be triggered with {@code mergeDiffStatusModel.getTableModelListeners()[0].tableChanged(null);}.
     */
    @Test
    void shouldUpdate()
    {
      // some values for the constructor
      IPrefStore pPrefStore = Mockito.spy(IPrefStore.class);
      IDialogProvider pDialogProvider = Mockito.spy(IDialogProvider.class);
      IAsyncProgressFacade pProgressFacade = Mockito.spy(IAsyncProgressFacade.class);
      IQuickSearchProvider pQuickSearchProvider = Mockito.spy(IQuickSearchProvider.class);
      INotifyUtil pNotifyUtil = Mockito.spy(INotifyUtil.class);
      ResolveOptionsProvider pResolveOptionsProvider = Mockito.spy(ResolveOptionsProvider.class);
      IDialogDisplayer.IDescriptor pIsValidDescriptor = Mockito.spy(IDialogDisplayer.IDescriptor.class);

      IRepository repository = Mockito.spy(IRepository.class);
      Mockito.doReturn(Observable.just(Optional.empty())).when(repository).getStatus();
      Observable<Optional<IRepository>> pRepository = Observable.just(Optional.of(repository));

      boolean pOnlyConflicting = true;
      boolean pShowAutoResolve = true;


      List<IMergeData> mergeData = new ArrayList<>();
      // adding 10 dummy elements
      for (int i = 0; i < 10; i++)
      {
        mergeData.add(Mockito.spy(IMergeData.class));
      }

      IMergeDetails mergeDetails = new MergeDetailsImpl(mergeData, "2022.2", "2022.0");


      MergeConflictDialog mergeConflictDialog = new MergeConflictDialog(pPrefStore, pDialogProvider, pProgressFacade, pQuickSearchProvider, pNotifyUtil, pResolveOptionsProvider, pIsValidDescriptor, pRepository, mergeDetails, pOnlyConflicting, pShowAutoResolve);

      // there is not just our listener but also other listeners
      assertEquals(3, mergeConflictDialog.getMergeDiffStatusModel().getTableModelListeners().length, "model has three listeners");


      assertEquals("10 remaining conflicts", mergeConflictDialog.getRemainingConflicts().getText(), "text of label after constructing");


      // remove an entry
      mergeData.remove(0);
      Arrays.stream(mergeConflictDialog.getMergeDiffStatusModel().getTableModelListeners()).forEach(pTableModelListener -> pTableModelListener.tableChanged(null));
      assertAll(() -> assertEquals(9, mergeData.size(), "size of merge data"),
                () -> assertEquals("9 remaining conflicts", mergeConflictDialog.getRemainingConflicts().getText(), "text after removing an element"));


      // remove three entries
      mergeData.remove(0);
      mergeData.remove(0);
      mergeData.remove(0);
      Arrays.stream(mergeConflictDialog.getMergeDiffStatusModel().getTableModelListeners()).forEach(pTableModelListener -> pTableModelListener.tableChanged(null));
      assertAll(
          () -> assertEquals(6, mergeData.size(), "size of merge data"),
          () -> assertEquals("6 remaining conflicts", mergeConflictDialog.getRemainingConflicts().getText(), "text after removing an element"));
    }
  }
}