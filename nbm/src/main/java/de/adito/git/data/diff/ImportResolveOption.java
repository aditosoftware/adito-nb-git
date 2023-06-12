package de.adito.git.data.diff;

import com.google.common.collect.Multimap;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.IJsParserUtility;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.data.diff.*;
import de.adito.git.impl.data.diff.ConflictPair;
import de.adito.git.impl.data.diff.ResolveOption;
import de.adito.git.nbm.IGitConstants;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author m.kaspera, 13.06.2022
 */
public class ImportResolveOption implements ResolveOption
{
  @Override
  public List<IDeltaTextChangeEvent> resolveConflict(@NonNull IChangeDelta acceptedDelta, @NonNull IFileDiff pAcceptedDiff, @NonNull IFileDiff pOtherDiff, @NonNull EConflictSide pConflictSide, @NonNull ConflictPair pConflictPair)
  {
    IJsParserUtility jsParserUtility = IJsParserUtility.getInstance();
    IChangeDelta otherDelta = pOtherDiff.getChangeDeltas().get(pConflictPair.getIndexOfSide(EConflictSide.getOpposite(pConflictSide)));
    try
    {
      Multimap<String, String> parsedImports = jsParserUtility.parseImports(acceptedDelta.getText(EChangeSide.NEW));
      combineNonDuplicates(parsedImports, jsParserUtility.parseImports(otherDelta.getText(EChangeSide.NEW)));
      String combinedImports = jsParserUtility.appendImports("", parsedImports);
      if (combinedImports.endsWith("null"))
        combinedImports = combinedImports.substring(0, Math.max(0, combinedImports.length() - 4));
      pAcceptedDiff.setResolved(acceptedDelta);
      pOtherDiff.setResolved(otherDelta);
      pAcceptedDiff.processTextEvent(acceptedDelta.getStartTextIndex(EChangeSide.OLD), acceptedDelta.getEndTextIndex(EChangeSide.OLD) - acceptedDelta.getStartTextIndex(EChangeSide.OLD), combinedImports, EChangeSide.OLD, false, true);
      pOtherDiff.processTextEvent(otherDelta.getStartTextIndex(EChangeSide.OLD), otherDelta.getEndTextIndex(EChangeSide.OLD) - otherDelta.getStartTextIndex(EChangeSide.OLD), combinedImports, EChangeSide.OLD, false, true);
    }
    catch (Exception pE)
    {
      IGitConstants.INJECTOR.getInstance(INotifyUtil.class).notify(pE, "Git: Error during conflict resolution", true);
    }
    return List.of();
  }

  @Override
  public boolean canResolveConflict(@NonNull IChangeDelta pChangeDelta, @NonNull IChangeDelta pOtherDelta, @NonNull EConflictSide pConflictSide,
                                    @NonNull IFileDiffHeader pFileDiffHeader)
  {
    if (!"js".equals(pFileDiffHeader.getFileExtension(EChangeSide.NEW)))
      return false;
    IJsParserUtility jsParserUtility = IJsParserUtility.getInstance();
    //noinspection ConstantConditions The nullable annotation is wrong here - at least in the only available implementation now. Lookups can return null if no service is found
    if (jsParserUtility == null)
      return false;
    return Arrays.stream(pChangeDelta.getText(EChangeSide.NEW).split("\n"))
        .filter(pString -> !pString.isEmpty())
        .allMatch(jsParserUtility::isImportLine) &&
        Arrays.stream(pOtherDelta.getText(EChangeSide.NEW).split("\n"))
            .filter(pString -> !pString.isEmpty())
            .allMatch(jsParserUtility::isImportLine);
  }

  @Override
  public int getPosition()
  {
    return 200;
  }

  /**
   * Insert all but duplicate elements from the second map into the first (pInsertMap)
   *
   * @param pInsertMap Map that will include non-duplicate elements from both maps
   * @param pSecondMap source for the additional elemtents
   */
  private static void combineNonDuplicates(@NonNull Multimap<String, String> pInsertMap, @NonNull Multimap<String, String> pSecondMap)
  {
    for (Map.Entry<String, String> entry : pSecondMap.entries())
    {
      if (pInsertMap.containsKey(entry.getKey()))
      {
        if (!pInsertMap.get(entry.getKey()).contains(entry.getValue()))
        {
          pInsertMap.put(entry.getKey(), entry.getValue());
        }
      }
      else
      {
        pInsertMap.put(entry.getKey(), entry.getValue());
      }
    }
  }
}
