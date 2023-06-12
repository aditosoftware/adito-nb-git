package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.IChangeDelta;
import de.adito.git.gui.Constants;
import de.adito.git.gui.icon.SwingIconLoaderImpl;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests if the iconPressMouseAdapter works as intended
 *
 * @author m.kaspera, 02.02.2023
 */
class IconPressMouseAdapterTest
{

  /**
   * Test if the discard button is correctly recognized in an easterly orientation
   */
  @Test
  void isDoDiscardCalledOnWestOrientation()
  {
    AtomicBoolean calledAccept = new AtomicBoolean(false);
    AtomicBoolean calledDiscard = new AtomicBoolean(false);
    int xValue = 29;
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon discardIcon = iconLoader.getIcon(Constants.DISCARD_CHANGE_ICON);
    ImageIcon acceptIcon = iconLoader.getIcon(Constants.ACCEPT_CHANGE_YOURS_ICON);
    List<IconInfo> iconInfos = List.of(new IconInfo(acceptIcon, 198, 2, mock(IChangeDelta.class)), new IconInfo(discardIcon, 198, 18, mock(IChangeDelta.class)));
    prepareTest(calledAccept, calledDiscard, iconInfos, true, MouseEvent.BUTTON1, xValue);

    assertAll(() -> assertTrue(calledDiscard.get(), "discard should have been executed"),
              () -> assertFalse(calledAccept.get(), "accept should not have been executed"));

  }

  /**
   * Test if the accept button is correctly recognized in an westerly orientation
   */
  @Test
  void isDoAcceptCalledOnWestOrientation()
  {
    AtomicBoolean calledAccept = new AtomicBoolean(false);
    AtomicBoolean calledDiscard = new AtomicBoolean(false);
    int xValue = 12;
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon discardIcon = iconLoader.getIcon(Constants.DISCARD_CHANGE_ICON);
    ImageIcon acceptIcon = iconLoader.getIcon(Constants.ACCEPT_CHANGE_YOURS_ICON);
    List<IconInfo> iconInfos = List.of(new IconInfo(acceptIcon, 198, 2, mock(IChangeDelta.class)), new IconInfo(discardIcon, 198, 18, mock(IChangeDelta.class)));
    prepareTest(calledAccept, calledDiscard, iconInfos, true, MouseEvent.BUTTON1, xValue);

    assertAll(() -> assertFalse(calledDiscard.get(), "discard should not have been executed"),
              () -> assertTrue(calledAccept.get(), "accept should have been executed"));

  }

  /**
   * Test if the discard button is correctly recognized in an easterly orientation
   */
  @Test
  void isDoDiscardCalledOnEastOrientation()
  {
    AtomicBoolean calledAccept = new AtomicBoolean(false);
    AtomicBoolean calledDiscard = new AtomicBoolean(false);
    int xValue = 14;
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon discardIcon = iconLoader.getIcon(Constants.DISCARD_CHANGE_ICON);
    ImageIcon acceptIcon = iconLoader.getIcon(Constants.ACCEPT_CHANGE_YOURS_ICON);
    List<IconInfo> iconInfos = List.of(new IconInfo(acceptIcon, 198, 18, mock(IChangeDelta.class)), new IconInfo(discardIcon, 198, 2, mock(IChangeDelta.class)));
    prepareTest(calledAccept, calledDiscard, iconInfos, false, MouseEvent.BUTTON1, xValue);

    assertAll(() -> assertTrue(calledDiscard.get(), "discard should have been executed"),
              () -> assertFalse(calledAccept.get(), "accept should not have been executed"));

  }

  /**
   * Test if the accept button is correctly recognized in an easterly orientation
   */
  @Test
  void isDoAcceptCalledOnEastOrientation()
  {
    AtomicBoolean calledAccept = new AtomicBoolean(false);
    AtomicBoolean calledDiscard = new AtomicBoolean(false);
    int xValue = 26;
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon discardIcon = iconLoader.getIcon(Constants.DISCARD_CHANGE_ICON);
    ImageIcon acceptIcon = iconLoader.getIcon(Constants.ACCEPT_CHANGE_YOURS_ICON);
    List<IconInfo> iconInfos = List.of(new IconInfo(acceptIcon, 198, 18, mock(IChangeDelta.class)), new IconInfo(discardIcon, 198, 2, mock(IChangeDelta.class)));
    prepareTest(calledAccept, calledDiscard, iconInfos, false, MouseEvent.BUTTON1, xValue);

    assertAll(() -> assertFalse(calledDiscard.get(), "discard should not have been executed"),
              () -> assertTrue(calledAccept.get(), "accept should have been executed"));

  }

  /**
   * Test if nothing happens if there are no active buttons
   */
  @Test
  void isDoNothingWhenNoIcons()
  {
    AtomicBoolean calledAccept = new AtomicBoolean(false);
    AtomicBoolean calledDiscard = new AtomicBoolean(false);
    int xValue = 29;
    List<IconInfo> iconInfos = List.of();
    prepareTest(calledAccept, calledDiscard, iconInfos, true, MouseEvent.BUTTON1, xValue);

    assertAll(() -> assertFalse(calledDiscard.get(), "discard should not have been executed"),
              () -> assertFalse(calledAccept.get(), "accept should not have been executed"));

  }

  /**
   * Test if nothing happens if the used mouse button was the wrong button for accept/change events
   */
  @Test
  void isDoNothingWhenWrongButton()
  {
    AtomicBoolean calledAccept = new AtomicBoolean(false);
    AtomicBoolean calledDiscard = new AtomicBoolean(false);
    int xValue = 29;
    SwingIconLoaderImpl iconLoader = new SwingIconLoaderImpl();
    ImageIcon discardIcon = iconLoader.getIcon(Constants.DISCARD_CHANGE_ICON);
    ImageIcon acceptIcon = iconLoader.getIcon(Constants.ACCEPT_CHANGE_YOURS_ICON);
    List<IconInfo> iconInfos = List.of(new IconInfo(acceptIcon, 198, 2, mock(IChangeDelta.class)), new IconInfo(discardIcon, 198, 18, mock(IChangeDelta.class)));
    prepareTest(calledAccept, calledDiscard, iconInfos, true, MouseEvent.BUTTON2, xValue);

    assertAll(() -> assertFalse(calledDiscard.get(), "discard should not have been executed"),
              () -> assertFalse(calledAccept.get(), "accept should not have been executed"));

  }

  /**
   * Set up the mocks required for the test
   *
   * @param pOnAccept          AtomicBoolean that should be set if the doOnAccept Method is called
   * @param pOnDiscard         AtomicBoolean that should be set if the doOnDiscard Method is called
   * @param pIconInfos         List with iconInfos that the iconInfoModel should return when queried for the icons
   * @param pIsWestOrientation if the mocked panel should have a western orientation. Your side of a merge = west
   * @param pMouseButton       Which mouse button should be returned by the mouseEvent. Use MouseEvent.BUTTON1 and the like
   * @param pMouseXValue       x value for position of the mouseEvent
   */
  private static void prepareTest(@NonNull AtomicBoolean pOnAccept, @NonNull AtomicBoolean pOnDiscard, @NonNull List<IconInfo> pIconInfos,
                                  boolean pIsWestOrientation, int pMouseButton, int pMouseXValue)
  {
    Consumer<IChangeDelta> doOnAccept = pDelta -> pOnAccept.set(true);
    Consumer<IChangeDelta> doOnDiscard = pDelta -> pOnDiscard.set(true);
    IconInfoModel iconInfoModel = mock(IconInfoModel.class);
    when(iconInfoModel.getIconInfosToDraw(Mockito.anyInt(), Mockito.anyInt())).thenReturn(pIconInfos);
    Supplier<Rectangle> viewSupplier = () -> new Rectangle(0, 0, 36, 900);
    IconPressMouseAdapter adapter = new IconPressMouseAdapter(16, doOnAccept, doOnDiscard, iconInfoModel, viewSupplier, pIsWestOrientation);
    MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);
    when(mouseEvent.getButton()).thenReturn(pMouseButton);
    when(mouseEvent.getPoint()).thenReturn(new Point(pMouseXValue, 200));
    when(mouseEvent.getX()).thenReturn(pMouseXValue);
    adapter.mousePressed(mouseEvent);
  }
}