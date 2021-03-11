package de.adito.git.gui.swing;

import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel that creates an amount of title + textfields, arranged in a table-like structure. The number of textfields created depends on the number of field titles passed
 *
 * The contents of the text fields can be retrieved by calling getFieldContent with the name of the associated field title
 *
 * @author m.kaspera, 10.03.2020
 */
public class InputFieldTablePanel extends JPanel
{
  private final Map<String, TextFieldWithPlaceholder> textFieldList = new HashMap<>();

  /**
   * Creats the Panel, the parameters in the lists have to match up index-wise (first title is used with the first placeholder and so on)
   *
   * @param pFieldTitles  list of labels/titles for the inputFields
   * @param pFieldContent list with predermined content of the inputField. Use an empty string if the field should show the placeholder
   * @param pPlaceholders list of placeholders
   */
  public InputFieldTablePanel(@NotNull List<String> pFieldTitles, @NotNull List<String> pFieldContent, @NotNull List<String> pPlaceholders)
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 10;
    double[] cols = {pref, gap, fill};
    double[] rows = new double[2 * pFieldTitles.size() - 1];
    for (int index = 0; index < rows.length; index++)
    {
      // order is pref, gap, pref, ... -> all even indices are pref, all odd indices are gap
      rows[index] = index % 2 == 0 ? pref : gap;
    }

    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);

    for (int index = 0; index < pFieldTitles.size(); index++)
    {
      TextFieldWithPlaceholder textField = new TextFieldWithPlaceholder(pFieldContent.size() <= index ? "" : pFieldContent.get(index),
                                                                        pPlaceholders.size() <= index ? null : pPlaceholders.get(index));
      textFieldList.put(pFieldTitles.get(index), textField);
      tlu.add(0, index * 2, new JLabel(pFieldTitles.get(index)));
      tlu.add(2, index * 2, textField);
    }
    setBorder(new EmptyBorder(5, 0, 5, 0));
  }

  /**
   * Retrieves the contents of the text field associated with the passed title
   *
   * @param pFieldTitle title for the text field
   * @return contents of the associated text field
   */
  public String getFieldContent(String pFieldTitle)
  {
    return textFieldList.get(pFieldTitle).getText();
  }
}
