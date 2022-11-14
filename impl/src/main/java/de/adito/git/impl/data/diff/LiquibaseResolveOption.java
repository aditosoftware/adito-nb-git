package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Class for resolving conflicts in liquibase xml files, parses the xml content of the conflict and checks if the referenced files are different. If that is the case,
 * resolves the conflict by accepting both sides of the conflict, thereby adding the newly referenced files by both sides
 *
 * @author m.kaspera, 24.10.2022
 */
public class LiquibaseResolveOption extends XMLBasedResolveOption
{
  @Override
  public List<IDeltaTextChangeEvent> resolveConflict(@NotNull IChangeDelta acceptedDelta, @NotNull IFileDiff pAcceptedDiff, @NotNull IFileDiff pOtherDiff,
                                                     EConflictSide pConflictSide, ConflictPair pConflictPair)
  {
    return new ArrayList<>(pAcceptedDiff.acceptDelta(acceptedDelta, false, true, false));
  }

  @Override
  public boolean canResolveConflict(@NotNull IChangeDelta pChangeDelta, @NotNull IChangeDelta pOtherDelta, @NotNull EConflictSide pConflictSide)
  {
    try
    {
      NodeList nodeList = parseConflictText(new ByteArrayInputStream(pChangeDelta.getText(EChangeSide.NEW).getBytes(StandardCharsets.UTF_8)));
      Set<String> liquibaseFiles = new HashSet<>();
      for (int index = 0; index < nodeList.getLength(); index++)
      {
        String liquibaseFileName = getLiquibaseFileName(nodeList.item(index));
        // cannot parse the filename, or some other type of node -> cannot be resolved by this resolveOption
        if (liquibaseFileName == null)
          return false;
        liquibaseFiles.add(liquibaseFileName);
      }
      NodeList otherNodeList = parseConflictText(new ByteArrayInputStream(pOtherDelta.getText(EChangeSide.NEW).getBytes(StandardCharsets.UTF_8)));
      for (int index = 0; index < otherNodeList.getLength(); index++)
      {
        String liquibaseFileName = getLiquibaseFileName(otherNodeList.item(index));
        // if filename cannot be parsed or the other side declares the same filename this resolveOption cannot resolve the conflict
        if (liquibaseFileName == null || liquibaseFiles.contains(liquibaseFileName))
          return false;
      }

      return true;
    }
    catch (IOException | SAXException | ParserConfigurationException pE)
    {
      return false;
    }
  }

  /**
   * @param pNode XML node that contains the filename as attribute named "file"
   * @return the attribute "file" of the node, or null if no such attribute exists
   */
  @Nullable
  private String getLiquibaseFileName(@NotNull Node pNode)
  {
    return Optional.ofNullable(pNode.getAttributes().getNamedItem("file")).map(Node::getTextContent).orElse(null);
  }

  @Override
  public int getPosition()
  {
    return 300;
  }
}
