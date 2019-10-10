package de.adito.git.impl;

import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.attributes.Attribute;
import org.eclipse.jgit.attributes.AttributesNode;
import org.eclipse.jgit.attributes.AttributesRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author m.kaspera, 09.10.2019
 */
public class GitAttributesChecker
{

  /**
   * Checks if there is a gitattributes file, if none exists asks the user if one should be added
   * If there is one, the rules from that file are compared to the default nodes and, if any rules are missing or changed, asks the user if those rules should be added
   * or changed to be in line with the default nodes.
   *
   * @param pDialogProvider   IDialogProvider used to show dialogs to the user
   * @param defaultNodes      AttributesNode filled with the default attributes
   * @param configNodes       AttributesNode filled with the attributes of the .gitattributes file
   * @param pProjectDirectory Directory that contains the .gitattributes file and forms the top level of the project
   * @throws IOException if the given gitattributes or the default config cannot be opened/read/written to
   */
  static void compareToDefault(@NotNull IDialogProvider pDialogProvider, @NotNull AttributesNode defaultNodes, @Nullable AttributesNode configNodes,
                               @Nullable File pProjectDirectory) throws IOException
  {
    if (pProjectDirectory == null)
      return;
    File gitAttributesFile = new File(pProjectDirectory, ".gitattributes");
    if (_containsDoNotAskAgainFlag(gitAttributesFile))
      return;
    if (configNodes == null || configNodes.getRules() == null)
    {
      DialogResult dialogResult = pDialogProvider.showCheckboxPrompt("<html>No attributes set, use suggested attributes?<br><br>Gitattributes can be used to customize " +
                                                                         "the behaviour of git, an example would be automatic conversion of lineendings, " +
                                                                         "depending on the OS used</html>",
                                                                     "Do not ask me again");
      if (dialogResult.isPressedOk())
      {
        _copyDefaultConfig(gitAttributesFile);
      }
      Boolean isDoNotAskAgain = (Boolean) dialogResult.getInformation();
      if (isDoNotAskAgain)
        _addDoNotAskAgainFlag(gitAttributesFile);
    }
    else
    {
      List<AttributesRule> missingOrChangedAttributes = _getMissingOrChangedRules(defaultNodes, configNodes);
      if (!missingOrChangedAttributes.isEmpty())
      {
        _handleMissingAttributes(pDialogProvider, gitAttributesFile, missingOrChangedAttributes);
      }
    }
  }

  /**
   * Compares the defaultNodes to the configNodes and puts all rules from the defaultNodes into a list if one of the following is the case:
   * - They exist in the defaultNodes, but not in the configNodes
   * - The patterns exists both in the defaultNodes as well as the configNodes, but the attributes differ
   *
   * @param defaultNodes AttributesNode with the rules from the default gitattributes
   * @param configNodes  AttributesNode with the rules from the current gitattributes
   * @return List of AttributesRule that contains the changed or missing rules when comparing the defaultNodes to the configNodes
   */
  @NotNull
  private static List<AttributesRule> _getMissingOrChangedRules(@NotNull AttributesNode defaultNodes, @NotNull AttributesNode configNodes)
  {
    List<AttributesRule> configRules = configNodes.getRules();
    List<AttributesRule> missingOrChangedAttributes = new ArrayList<>();
    HashMap<String, List<Attribute>> patternToAttributes = new HashMap<>();
    for (AttributesRule configRule : configRules)
    {
      patternToAttributes.put(configRule.getPattern(), configRule.getAttributes());
    }
    for (AttributesRule defaultRule : defaultNodes.getRules())
    {
      if (!patternToAttributes.containsKey(defaultRule.getPattern()) || !patternToAttributes.get(defaultRule.getPattern()).equals(defaultRule.getAttributes()))
      {
        missingOrChangedAttributes.add(defaultRule);
      }
    }
    return missingOrChangedAttributes;
  }

  /**
   * Asks the user if the given changed or missing attributes should be added to the gitattributes file and performs that operation if the user presses okay
   *
   * @param pDialogProvider             IDialogProvider used to show dialogs to the user
   * @param pGitAttributesFile          File containing the gitattributes
   * @param pMissingOrChangedAttributes List with attributes that are missing or changed compared to the default gitattributes defined in the exampe_gitattributes
   * @throws IOException if the given gitattributes or the default config cannot be opened/read/written to
   */
  private static void _handleMissingAttributes(@NotNull IDialogProvider pDialogProvider, @NotNull File pGitAttributesFile,
                                               @NotNull List<AttributesRule> pMissingOrChangedAttributes) throws IOException
  {
    String missingAttributes = pMissingOrChangedAttributes.stream().map(GitAttributesChecker::_ruleAsString).collect(Collectors.joining("<br>"));
    DialogResult dialogResult = pDialogProvider.showCheckboxPrompt("<html>Attribute file found but some default entries seem to be missing:<br><br>" + missingAttributes +
                                                                       "<br><br>Change the file to include those rules?<br></html>",
                                                                   "Do not ask me again");
    if (dialogResult.isPressedOk())
    {
      String modifiedGitAttributes = _getModifiedGitAttributes(pMissingOrChangedAttributes, pGitAttributesFile);
      try (FileOutputStream outputStream = new FileOutputStream(pGitAttributesFile))
      {
        outputStream.write(modifiedGitAttributes.getBytes());
      }
    }
    Boolean isDoNotAskAgain = (Boolean) dialogResult.getInformation();
    if (isDoNotAskAgain)
      _addDoNotAskAgainFlag(pGitAttributesFile);
  }

  /**
   * parses the gitAttributes file and builds a String with the changes and additions from pMissingOrChangedAttributes (that can be used to re-write the file)
   *
   * @param pMissingOrChangedAttributes list of AttributeRules that were missing or changed
   * @param pGitAttributeFile           File that contains the current gitAttributes
   * @return String that forms the modified/updated gitAttributes file, use this to override the current gitAttributes file
   * @throws IOException if the current or the default gitAttributes file cannot be opened or read
   */
  private static String _getModifiedGitAttributes(@NotNull List<AttributesRule> pMissingOrChangedAttributes, @NotNull File pGitAttributeFile) throws IOException
  {
    StringBuilder modifiedGitAttributes = new StringBuilder();
    try (BufferedReader gitAttributesInStream = new BufferedReader(new FileReader(pGitAttributeFile)))
    {
      String readLine = gitAttributesInStream.readLine();
      while (readLine != null)
      {
        // comment line, leave as-is
        if (readLine.startsWith("#") || readLine.startsWith(";"))
        {
          modifiedGitAttributes.append(readLine).append("\n");
          readLine = gitAttributesInStream.readLine();
          continue;
        }
        readLine = _modifyLineIfNecessary(pMissingOrChangedAttributes, readLine);
        modifiedGitAttributes.append(readLine).append("\n");
        readLine = gitAttributesInStream.readLine();
      }
    }
    pMissingOrChangedAttributes.forEach(pMissingAttribute -> modifiedGitAttributes.append(_ruleAsString(pMissingAttribute)).append("\n"));
    return modifiedGitAttributes.toString();
  }

  /**
   * check if the line matches any of the rules that should be changed. If so, changes the rule to match the fitting one from pMissingOrChangedAttributes
   *
   * @param pMissingOrChangedAttributes List of rules that were abent or changed
   * @param pReadLine                   line that should be checked for matches in pMissingOrChangedAttributes
   * @return the given line, modified if necessary
   */
  private static String _modifyLineIfNecessary(@NotNull List<AttributesRule> pMissingOrChangedAttributes, @NotNull String pReadLine)
  {
    AttributesRule matchedRule = null;
    String trimmedLine = pReadLine.trim();
    for (AttributesRule rule : pMissingOrChangedAttributes)
    {
      if (_isLineOfAttribute(trimmedLine, rule) && !_isLineEqualsAttribute(trimmedLine, rule))
      {
        int patternStartIndex = pReadLine.indexOf(rule.getPattern());
        pReadLine = pReadLine.substring(0, patternStartIndex + rule.getPattern().length()) + "\t" + _ruleAttributesAsString(rule);
        matchedRule = rule;
        break;
      }
    }
    if (matchedRule != null)
      pMissingOrChangedAttributes.remove(matchedRule);
    return pReadLine;
  }

  /**
   * checks if the attributes of the given line match the attributes of the given rule
   *
   * @param pTrimmedLine line whose attributes should be checked
   * @param pRule        rule with the attributes to be compared
   * @return true
   */
  private static boolean _isLineEqualsAttribute(@NotNull String pTrimmedLine, @NotNull AttributesRule pRule)
  {
    String[] trimmedLineParts = pTrimmedLine.split("[ ]|\t");
    if (trimmedLineParts.length <= 1)
    {
      return false;
    }
    return trimmedLineParts[trimmedLineParts.length - 1].equals(_ruleAttributesAsString(pRule));
  }

  /**
   * checks if the given trimmed line matches the rule pattern
   *
   * @param pTrimmedLine line to be checked
   * @param pRule        rule of which to use the pattern
   * @return true if the pattern given by the rule matches the one from the given line
   */
  private static boolean _isLineOfAttribute(@NotNull String pTrimmedLine, @NotNull AttributesRule pRule)
  {
    return pRule.getPattern().equals(pTrimmedLine.split("[ ]|\t")[0]);
  }

  /**
   * Converts the attributes of the rule into the format that is used in the gitattributes file
   *
   * @param pRule any Rule
   * @return The Attributes as concatendated string, seperated by a space
   */
  private static String _ruleAttributesAsString(@NotNull AttributesRule pRule)
  {
    return StringUtils.join(pRule.getAttributes(), " ");
  }

  /**
   * Converts the rule to a String, the same as it would be in the gitattributes file
   *
   * @param pRule any Rule
   * @return Rule representation as String
   */
  private static String _ruleAsString(@NotNull AttributesRule pRule)
  {
    return pRule.getPattern() + "\t" + _ruleAttributesAsString(pRule);
  }

  /**
   * Writes the "do not ask again" flag that is set if the config deviates from the the default config and the user does not want to be
   * asked again.
   * The Flag is stored in the gitattributes files because once set it should be present for all users of the repository, not just the local user (still requires
   * a push though)
   *
   * @param pGiAttributesFile File to which the "do not ask again"
   * @throws IOException if the flag cannot be written to the file due to various reasons
   */
  private static void _addDoNotAskAgainFlag(@NotNull File pGiAttributesFile) throws IOException
  {
    try (OutputStream outputStream = Files.newOutputStream(pGiAttributesFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND))
    {
      outputStream.write("\n### adito.gitattributes.donotask\t\t Flag for the Adito Git Plugin, do not modify. Only remove to deactivate the flag".getBytes());
    }
  }

  /**
   * checks if the given file contains the "do no ask again flag" that is set if the config deviates from the the default config and the user does not want to be
   * asked again.
   * The Flag is stored in the gitattributes files because once set it should be present for all users of the repository, not just the local user (still requires
   * a push though)
   *
   * @param pGitAttributesFile file that should be searched for the flag
   * @return true if the flag is found in the file, false otherwise
   * @throws IOException if the file cannot be read
   */
  private static boolean _containsDoNotAskAgainFlag(@NotNull File pGitAttributesFile) throws IOException
  {
    if (!pGitAttributesFile.exists())
      return false;
    try (Stream<String> lines = Files.lines(pGitAttributesFile.toPath()))
    {
      return lines.anyMatch(pLine -> pLine.startsWith("### adito.gitattributes.donotask"));
    }
  }

  /**
   * Copies the default/example config to the given File
   *
   * @param pGitAttributesFile Location that the config should be copied to
   * @throws IOException if the file cannot be written or an error occurs while reading the example config
   */
  private static void _copyDefaultConfig(@NotNull File pGitAttributesFile) throws IOException
  {
    try (FileOutputStream fileOutputStream = new FileOutputStream(pGitAttributesFile);
         InputStream inputStream = GitAttributesChecker.class.getResourceAsStream("example_gitattributes"))
    {
      byte[] buffer = new byte[2048];
      int bytesRead = inputStream.read(buffer);
      while (bytesRead != -1)
      {
        fileOutputStream.write(buffer, 0, bytesRead);
        bytesRead = inputStream.read(buffer);
      }
    }
  }

}
