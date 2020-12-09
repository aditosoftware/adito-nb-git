package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IRemote;
import de.adito.git.gui.swing.ADocumentListener;
import de.adito.git.impl.data.RemoteImpl;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Panel that contains the settings and parts of the logic of adding a new Remote
 *
 * @author m.kaspera, 30.07.2019
 */
public class NewRemotePanel extends JPanel implements IDiscardable
{

  private static final String REMOTE_NAME_FIELD_LABEL = "Remote name: ";
  private static final String REMOTE_URL_FIELD_LABEL = "Remote address: ";

  private final JTextField remoteNameField = new JTextField();
  private final JTextField remoteUrlField = new JTextField();
  private JButton addRemoteButton;
  private final DocumentListener isActiveListener;
  private final Consumer<IRemote> createRemotePanelFn;

  public NewRemotePanel(Set<IRemote> pRemotes, Consumer<IRemote> pCreateRemotePanelFn)
  {
    isActiveListener = new ADocumentListener()
    {
      @Override
      public void updated(DocumentEvent pE)
      {
        if (addRemoteButton != null)
          addRemoteButton.setEnabled(!remoteNameField.getText().isEmpty()
                                         && pRemotes.stream().noneMatch(pRemote -> pRemote.getName().equals(remoteNameField.getText()))
                                         && !remoteUrlField.getText().isEmpty());
      }
    };
    remoteUrlField.getDocument().addDocumentListener(isActiveListener);
    remoteNameField.getDocument().addDocumentListener(isActiveListener);
    createRemotePanelFn = pCreateRemotePanelFn;
    _initGui();
  }

  private void _initGui()
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, pref, gap, fill, gap, pref, gap};
    double[] rows = {gap,
                     pref,
                     gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, new JLabel(REMOTE_NAME_FIELD_LABEL));
    tlu.add(3, 1, 5, 1, remoteNameField);
    tlu.add(1, 3, new JLabel(REMOTE_URL_FIELD_LABEL));
    tlu.add(3, 3, remoteUrlField);
    tlu.add(5, 3, _addRemoteButton());
  }

  private JButton _addRemoteButton()
  {
    addRemoteButton = new JButton("Add");
    addRemoteButton.setEnabled(false);
    addRemoteButton.addActionListener(e -> {
      createRemotePanelFn.accept(new RemoteImpl(remoteNameField.getText(), remoteUrlField.getText(), IRemote.getFetchStringFromName(remoteNameField.getText())));
      remoteNameField.setText("");
      remoteUrlField.setText("");
    });
    return addRemoteButton;
  }

  @Override
  public void discard()
  {
    remoteNameField.getDocument().removeDocumentListener(isActiveListener);
    remoteUrlField.getDocument().removeDocumentListener(isActiveListener);
  }
}
