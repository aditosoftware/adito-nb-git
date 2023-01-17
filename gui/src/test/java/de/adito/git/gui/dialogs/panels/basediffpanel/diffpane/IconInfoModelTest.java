package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.gui.guice.dummies.SimpleFileSystemUtil;
import de.adito.git.gui.icon.SwingIconLoaderImpl;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.impl.StandAloneDiffProviderImpl;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the functionality of the IconInfoModel
 *
 * @author m.kaspera, 12.01.2023
 */
class IconInfoModelTest
{

  // use another line height to differentiate tests and have a chance to catch errors that have to do with differing line heights
  private static final int LINE_HEIGHT = 21;

  /**
   * test if the coordinates calculated by the model are correct if there is a single modification to the file
   */
  @Test
  void isCorrectIconsWhenSingleModify()
  {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setText("test\nchangedLine1\ntest\n");
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon imageIcon = iconLoader.getIcon("");
    ImageIcon imageIcon2 = iconLoader.getIcon("");
    IconInfoModel iconInfoModel = new IconInfoModel(new LineNumberModel(Observable.empty(), editorPane, Observable.empty()), EChangeSide.OLD, editorPane,
                                                    imageIcon, imageIcon2, BorderLayout.WEST);
    IDeltaTextChangeEvent deltaTextChangeEvent = mock(IDeltaTextChangeEvent.class);
    IFileDiff fileDiff = new StandAloneDiffProviderImpl(new SimpleFileSystemUtil()).diffOffline("test\nchangedLine\ntest\n", "test\nchangedLine1\ntest\n");
    when(deltaTextChangeEvent.getFileDiff()).thenReturn(fileDiff);
    iconInfoModel.lineNumbersChanged(deltaTextChangeEvent, new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT), new LineNumber(2, 0, LINE_HEIGHT, LINE_HEIGHT),
                                                                            new LineNumber(3, 0, LINE_HEIGHT * 2, LINE_HEIGHT),
                                                                            new LineNumber(4, 0, LINE_HEIGHT * 3, LINE_HEIGHT)});
    List<IconInfo> iconsToDraw = new ArrayList<>(iconInfoModel.getIconInfosToDraw(0, 1000));
    assertEquals(2, iconsToDraw.size());
    // check if the icon is at the height it should be
    assertEquals(LINE_HEIGHT + IconInfoModel.Y_ICON_OFFSET, iconsToDraw.get(0).getIconCoordinates().y);
    // icons do not overlap and share the same y value/same height
    assertFalse(iconsToDraw.get(0).getIconCoordinates().intersects(iconsToDraw.get(1).getIconCoordinates()));
    assertEquals(iconsToDraw.get(0).getIconCoordinates().y, iconsToDraw.get(1).getIconCoordinates().y);
  }

  /**
   * test if the coordinates calculated by the model are correct if there is a single delete change
   */
  @Test
  void isCorrectIconsWhenSingleDelete()
  {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setText("test\nchangedLine1\ntest\n");
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon imageIcon = iconLoader.getIcon("");
    ImageIcon imageIcon2 = iconLoader.getIcon("");
    IconInfoModel iconInfoModel = new IconInfoModel(new LineNumberModel(Observable.empty(), editorPane, Observable.empty()), EChangeSide.OLD, editorPane,
                                                    imageIcon, imageIcon2, BorderLayout.WEST);
    IDeltaTextChangeEvent deltaTextChangeEvent = mock(IDeltaTextChangeEvent.class);
    IFileDiff fileDiff = new StandAloneDiffProviderImpl(new SimpleFileSystemUtil()).diffOffline("test\nchangedLine\ntest\n", "test\ntest\n");
    when(deltaTextChangeEvent.getFileDiff()).thenReturn(fileDiff);
    iconInfoModel.lineNumbersChanged(deltaTextChangeEvent, new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT), new LineNumber(2, 0, LINE_HEIGHT, LINE_HEIGHT),
                                                                            new LineNumber(3, 0, LINE_HEIGHT * 2, LINE_HEIGHT),
                                                                            new LineNumber(4, 0, LINE_HEIGHT * 3, LINE_HEIGHT)});
    List<IconInfo> iconsToDraw = new ArrayList<>(iconInfoModel.getIconInfosToDraw(0, 1000));
    assertEquals(2, iconsToDraw.size());
    // check if the icon is at the height it should be
    assertEquals(LINE_HEIGHT + IconInfoModel.Y_ICON_OFFSET, iconsToDraw.get(0).getIconCoordinates().y);
    // icons do not overlap and share the same y value/same height
    assertFalse(iconsToDraw.get(0).getIconCoordinates().intersects(iconsToDraw.get(1).getIconCoordinates()));
    assertEquals(iconsToDraw.get(0).getIconCoordinates().y, iconsToDraw.get(1).getIconCoordinates().y);
  }

  /**
   * test if the coordinates calculated by the model are correct if there is a single insert
   */
  @Test
  void isCorrectIconsWhenSingleInsert()
  {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setText("test\nchangedLine1\ntest\n");
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon imageIcon = iconLoader.getIcon("");
    ImageIcon imageIcon2 = iconLoader.getIcon("");
    IconInfoModel iconInfoModel = new IconInfoModel(new LineNumberModel(Observable.empty(), editorPane, Observable.empty()), EChangeSide.OLD, editorPane,
                                                    imageIcon, imageIcon2, BorderLayout.WEST);
    IDeltaTextChangeEvent deltaTextChangeEvent = mock(IDeltaTextChangeEvent.class);
    IFileDiff fileDiff = new StandAloneDiffProviderImpl(new SimpleFileSystemUtil()).diffOffline("test\ntest\n", "test\nchangedLine\ntest\n");
    when(deltaTextChangeEvent.getFileDiff()).thenReturn(fileDiff);
    iconInfoModel.lineNumbersChanged(deltaTextChangeEvent, new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT), new LineNumber(2, 0, LINE_HEIGHT, LINE_HEIGHT),
                                                                            new LineNumber(3, 0, LINE_HEIGHT * 2, LINE_HEIGHT),
                                                                            new LineNumber(4, 0, LINE_HEIGHT * 3, LINE_HEIGHT)});
    List<IconInfo> iconsToDraw = new ArrayList<>(iconInfoModel.getIconInfosToDraw(0, 1000));
    assertEquals(2, iconsToDraw.size());
    // check if the icon is at the height it should be
    assertEquals(LINE_HEIGHT + IconInfoModel.Y_ICON_OFFSET, iconsToDraw.get(0).getIconCoordinates().y);
    // icons do not overlap and share the same y value/same height
    assertFalse(iconsToDraw.get(0).getIconCoordinates().intersects(iconsToDraw.get(1).getIconCoordinates()));
    assertEquals(iconsToDraw.get(0).getIconCoordinates().y, iconsToDraw.get(1).getIconCoordinates().y);
  }

  /**
   * test if the coordinates calculated by the model are correct if there is more than one change
   */
  @Test
  void isCorrectIconsWhenMultiChange()
  {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setText("test\nchangedLine1\ntest\n");
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon imageIcon = iconLoader.getIcon("");
    ImageIcon imageIcon2 = iconLoader.getIcon("");
    IconInfoModel iconInfoModel = new IconInfoModel(new LineNumberModel(Observable.empty(), editorPane, Observable.empty()), EChangeSide.OLD, editorPane,
                                                    imageIcon, imageIcon2, BorderLayout.WEST);
    IDeltaTextChangeEvent deltaTextChangeEvent = mock(IDeltaTextChangeEvent.class);
    IFileDiff fileDiff = new StandAloneDiffProviderImpl(new SimpleFileSystemUtil()).diffOffline("test\ntest\n\ntest\nchangedLine\ntest\n",
                                                                                                "test\nchangedLine\ntest\n\ntest\nchangedLine1\ntest\n");
    when(deltaTextChangeEvent.getFileDiff()).thenReturn(fileDiff);
    iconInfoModel.lineNumbersChanged(deltaTextChangeEvent, new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT), new LineNumber(2, 0, LINE_HEIGHT, LINE_HEIGHT),
                                                                            new LineNumber(3, 0, LINE_HEIGHT * 2, LINE_HEIGHT),
                                                                            new LineNumber(4, 0, LINE_HEIGHT * 3, LINE_HEIGHT),
                                                                            new LineNumber(5, 0, LINE_HEIGHT * 4, LINE_HEIGHT),
                                                                            new LineNumber(6, 0, LINE_HEIGHT * 5, LINE_HEIGHT)});
    List<IconInfo> iconsToDraw = new ArrayList<>(iconInfoModel.getIconInfosToDraw(0, 1000));
    assertEquals(4, iconsToDraw.size());
    // check if the icon is at the height it should be
    assertEquals(LINE_HEIGHT + IconInfoModel.Y_ICON_OFFSET, iconsToDraw.get(0).getIconCoordinates().y);
    // icons do not overlap and share the same y value/same height. Discard and Accept icons are always in sequence together -> 0,1 are discard or accept icons, 2,3 the other icon type
    assertFalse(iconsToDraw.get(0).getIconCoordinates().intersects(iconsToDraw.get(2).getIconCoordinates()));
    assertEquals(iconsToDraw.get(0).getIconCoordinates().y, iconsToDraw.get(2).getIconCoordinates().y);

    // check positions of the icons for the second change
    assertEquals(LINE_HEIGHT * 4 + IconInfoModel.Y_ICON_OFFSET, iconsToDraw.get(1).getIconCoordinates().y);
    assertFalse(iconsToDraw.get(1).getIconCoordinates().intersects(iconsToDraw.get(3).getIconCoordinates()));
    assertEquals(iconsToDraw.get(1).getIconCoordinates().y, iconsToDraw.get(3).getIconCoordinates().y);
  }
}