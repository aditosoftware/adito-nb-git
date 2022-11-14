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
import java.util.stream.Collectors;

/**
 * Resolves conflicts in language xml files by parsing the xml text of the conflict and comparing the contained data
 *
 * @author m.kaspera, 24.10.2022
 */
public class LanguageFileResolveOption extends XMLBasedResolveOption
{
  @Override
  public List<IDeltaTextChangeEvent> resolveConflict(@NotNull IChangeDelta acceptedDelta, @NotNull IFileDiff pAcceptedDiff, @NotNull IFileDiff pOtherDiff, EConflictSide pConflictSide, ConflictPair pConflictPair)
  {
    return new ArrayList<>(pAcceptedDiff.acceptDelta(acceptedDelta, false, true, false));
  }

  @Override
  public boolean canResolveConflict(@NotNull IChangeDelta pChangeDelta, @NotNull IChangeDelta pOtherDelta, @NotNull EConflictSide pConflictSide)
  {
    try
    {
      NodeList nodeList = getStringEntryList(pChangeDelta.getText(EChangeSide.NEW));
      NodeList otherNodeList = getStringEntryList(pOtherDelta.getText(EChangeSide.NEW));
      if (nodeList == null || otherNodeList == null)
        return false;
      Map<String, String> languageKeyValues = readLanguageKeyValues(nodeList);
      return !languageKeyValues.isEmpty() && readLanguageKeyValues(otherNodeList).entrySet().stream()
          .noneMatch(pEntry -> languageKeyValues.containsKey(pEntry.getKey()) && !pEntry.getValue().equals(languageKeyValues.get(pEntry.getKey())));
    }
    catch (IOException | SAXException | ParserConfigurationException pE)
    {
      return false;
    }
  }

  @Override
  public int getPosition()
  {
    return 400;
  }

  /**
   * Read a nodeList, based on the assumption that the nodes contained in the list are "stringEntry" nodes that each contain a "value" and "name" node as children.
   * This mirrors a key-value pair in a language file
   *
   * @param pNodeList NodeList containing the values to be read
   * @return Map with the "name" values as keys and the "value" values as values of the map. If in any node either the value or the name cannot be found returns an empty map
   */
  @NotNull
  private Map<String, String> readLanguageKeyValues(@NotNull NodeList pNodeList)
  {
    Map<String, String> valueMap = new HashMap<>();
    for (int index = 0; index < pNodeList.getLength(); index++)
    {
      Node node = pNodeList.item(index);
      if ("stringEntry".equals(node.getNodeName()))
      {
        NodeList childNodes = node.getChildNodes();
        String nameContent = Optional.ofNullable(findNode(childNodes, "name")).map(Node::getFirstChild).map(Node::getTextContent).orElse(null);
        String valueContent = Optional.ofNullable(findNode(childNodes, "value")).map(Node::getFirstChild).map(Node::getTextContent).orElse(null);
        if (nameContent != null && valueContent != null)
        {
          valueMap.put(nameContent, valueContent);
        }
        else
        {
          return Map.of();
        }
      }
    }
    return valueMap;
  }

  /**
   * Wraps the provided text in a "values" tag to ensure that the text can be parsed, even if the text contains several root nodes with the same name
   * This can occur because the conflict text is not a full XML, but a snipped of it. The parser cannot parse a xml with identical root nodes
   *
   * @param pConflictText text of a conflict, will be parsed by the XML parser
   * @return NodeList with the values of the parsed pConflictText XML
   * @throws IOException                  If any IO errors occur.
   * @throws SAXException                 If any parse errors occur.
   * @throws ParserConfigurationException by the documentBuilder for parsing XML files: if a DocumentBuilder cannot be created which satisfies the configuration requested.
   */
  @Nullable
  private NodeList getStringEntryList(@NotNull String pConflictText) throws IOException, ParserConfigurationException, SAXException
  {
    String cleanedConflictText = Arrays.stream(pConflictText.split("\n")).map(String::trim).collect(Collectors.joining());
    if (cleanedConflictText.endsWith("<stringEntry>"))
      cleanedConflictText = "<stringEntry>" + cleanedConflictText.substring(0, cleanedConflictText.length() - "<stringEntry>".length());
    NodeList rootNodeList = parseConflictText(new ByteArrayInputStream(("<values>" + cleanedConflictText + "</values>").getBytes(StandardCharsets.UTF_8)));
    return Optional.ofNullable(findNode(rootNodeList, "values")).map(Node::getChildNodes).orElse(null);
  }

  /**
   * Go through a list of nodes and search for a node with name pName
   *
   * @param pNodeList List of nodes
   * @param pName     Name of the node to find
   * @return node with the given name, or null if no such node can be found
   */
  @Nullable
  private Node findNode(@NotNull NodeList pNodeList, @NotNull String pName)
  {
    for (int index = 0; index < pNodeList.getLength(); index++)
    {
      if (pName.equals(pNodeList.item(index).getNodeName()))
      {
        return pNodeList.item(index);
      }
    }
    return null;
  }
}
