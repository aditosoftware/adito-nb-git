package de.adito.git.nbm.sidebar;

import de.adito.git.api.IDiscardable;
import de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.MarkedScrollbar;
import de.adito.git.gui.rxjava.ViewPortSizeObservable;
import de.adito.git.impl.observables.PropertyChangeObservable;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

/**
 * Forms a bar on the side of the scrollbar, displays the areas/lines where changes occurred
 *
 * @author m.kaspera, 19.06.2019
 */
public class ChangesOverviewStrip extends JPanel implements IDiscardable
{

  private static final int PIXELS_FOR_LINE = 3;
  private static final int HEIGHT_OFFSET = 20;
  private static final int LINE_SEPARATOR_SIZE = 1;
  private final JTextComponent targetComponent;
  private final JScrollPane scrollPane;
  private Disposable disposable;
  private BufferedImage cachedImage;

  ChangesOverviewStrip(JTextComponent pTargetComponent)
  {
    setToolTipText("");
    targetComponent = pTargetComponent;
    scrollPane = EditorColorizer._getJScrollPane(pTargetComponent);
    addPropertyChangeListener(evt -> {
      if ("ancestor".equals(evt.getPropertyName()))
      {
        if (evt.getNewValue() == null)
        {
          discard();
        }
        else if (evt.getOldValue() == null)
        {
          _buildObservable();
        }
      }
    });
  }

  @Override
  protected void paintComponent(Graphics pG)
  {
    super.paintComponent(pG);
    if (cachedImage != null)
      _drawImage(pG, cachedImage);
  }

  private void _drawImage(Graphics pGraphics, BufferedImage pImage)
  {
    ((Graphics2D) pGraphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    pGraphics.drawImage(pImage, 0, 0, null);
  }

  @Override
  public void discard()
  {
    if (disposable != null)
      disposable.dispose();
  }

  @SuppressWarnings("unchecked")
  private void _buildObservable()
  {
    Observable<List<EditorColorizer._ChangeHolder>> changeObservable = Observable
        .create(new PropertyChangeObservable<>(targetComponent, IGitConstants.CHANGES_LOCATIONS_OBSERVABLE))
        .switchMap(pValue -> {
          if (pValue.isPresent())
            return (Observable<List<EditorColorizer._ChangeHolder>>) pValue.orElse(Observable.just(Optional.empty()));
          else return Observable.just(List.<EditorColorizer._ChangeHolder>of());
        })
        .startWith(_getChangeHolderObservable());
    disposable = Observable.combineLatest(Observable.create(new ViewPortSizeObservable(scrollPane.getViewport())), changeObservable, (pView, pList) -> pList)
        .subscribe(pList -> {
          cachedImage = _calculateImage(pList);
          repaint();
        });
  }

  /**
   * retrieve the Observable of the ChangeHolders that have the informations about which locations of the file were changed from the ClientProperties
   *
   * @return Observable of the ChangeHolders
   */
  private Observable<List<EditorColorizer._ChangeHolder>> _getChangeHolderObservable()
  {
    Object clientProperty = targetComponent.getClientProperty(IGitConstants.CHANGES_LOCATIONS_OBSERVABLE);
    if (clientProperty instanceof Observable)
    {
      @SuppressWarnings("unchecked")
      Observable<List<EditorColorizer._ChangeHolder>> imageObservable = (Observable<List<EditorColorizer._ChangeHolder>>) clientProperty;
      return imageObservable;
    }
    else
      return Observable.just(List.of());
  }

  /**
   * calculates the BufferedImage to draw next to the Scrollbar from the infromation of the ChangeHolders
   *
   * @param pList List of ChangeHolders that hold information about the locations of the changed lines
   * @return BufferedImage
   */
  private BufferedImage _calculateImage(List<EditorColorizer._ChangeHolder> pList)
  {

    // copied from Netbeans AnnotatorView so that the lines match up
    int visibleHeight = scrollPane.getViewport().getExtentSize().height;

    int topButton = _topOffset();
    int bottomButton = UIManager.getInsets("Nb.Editor.ErrorStripe.ScrollBar.Insets").bottom;

    int height = visibleHeight - topButton - bottomButton;

    // end netbeans copy

    BufferedImage image = new BufferedImage(Math.max(1, getWidth()), Math.max(1, height + topButton), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = (Graphics2D) image.getGraphics();
    double distortionFactor = height / (double) targetComponent.getHeight();
    pList.forEach(pChangeHolder -> {
      // start netbeans copy to have blocks start at the same height
      int paintFrom;
      if (targetComponent.getHeight() <= height)
      {
        //1:1 mapping:
        paintFrom = (int) pChangeHolder.rectangle.getY() + topButton;
      }
      else
      {
        double position = pChangeHolder.rectangle.getY() / targetComponent.getHeight();
        int blocksCount = height / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE);
        int block = (int) (position * blocksCount);

        paintFrom = block * (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE) + topButton;
      }
      // end netbeans copy
      // if the maxValue is very high and the marking small this could be 0 height without the Math.max (should always be shown though)
      int paintHeight = Math.max(MarkedScrollbar.MIN_MARKING_HEIGHT, (int) (pChangeHolder.rectangle.getHeight() * distortionFactor));
      graphics.setColor(pChangeHolder.color);
      graphics.fillRect(0, paintFrom, this.getWidth(), paintHeight);
    });
    return image;
  }

  /**
   * copied from Netbeans AnnotatorView, to make the changed and annotated lines match up
   *
   * @return offset from the top
   */
  private int _topOffset()
  {
    Insets scrollBar = UIManager.getInsets("Nb.Editor.ErrorStripe.ScrollBar.Insets");
    if (scrollBar == null)
    {
      //no help for #54080:
      return HEIGHT_OFFSET;
    }

    return (Math.max(HEIGHT_OFFSET, scrollBar.top)) + PIXELS_FOR_LINE;
  }
}
