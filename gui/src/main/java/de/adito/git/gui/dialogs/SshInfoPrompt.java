package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import java.io.File;
import java.util.Arrays;

/**
 * Dialog that has fields for the location of an ssh key and the passphrase for that ssh key
 *
 * @author m.kaspera, 20.12.2018
 */
class SshInfoPrompt extends AditoBaseDialog<char[]>
{

  private static final int PW_FIELD_NUM_CHARS = 30;
  private static final String SSH_KEY_FIELD_LABEL = "SSH key path: ";
  private static final String SSH_PASSPHRASE_FIELD_LABEL = "Passphrase for ssh key: ";
  private final JTextField sshKeyField = new JTextField();
  private final JPasswordField sshPassphraseField = new JPasswordField(PW_FIELD_NUM_CHARS);
  private final String message;
  private final IDialogDisplayer.IDescriptor isValidDescriptor;
  @Nullable
  private final IKeyStore keyStore;

  @Inject
  SshInfoPrompt(@Assisted("message") String pMessage, @Nullable @Assisted("keyLocation") String pSshKeyLocation, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                @Nullable @Assisted char[] pPassphrase, @Nullable @Assisted IKeyStore pKeyStore)
  {
    message = pMessage;
    isValidDescriptor = pIsValidDescriptor;
    keyStore = pKeyStore;
    if (pSshKeyLocation != null)
      sshKeyField.setText(pSshKeyLocation);
    sshKeyField.getDocument().addDocumentListener(new IsValidListener());
    if (pPassphrase != null)
    {
      sshPassphraseField.setText(String.valueOf(pPassphrase));
      Arrays.fill(pPassphrase, (char) 0);
    }
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
                     gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, 3, 1, new JLabel(message));
    tlu.add(1, 3, new JLabel(SSH_KEY_FIELD_LABEL));
    tlu.add(3, 3, sshKeyField);
    tlu.add(5, 3, _browseSshKeyButton());
    tlu.add(1, 5, new JLabel(SSH_PASSPHRASE_FIELD_LABEL));
    tlu.add(3, 5, 5, 5, sshPassphraseField);
  }

  private JButton _browseSshKeyButton()
  {
    JButton locationBrowseButton = new JButton("Browse");
    locationBrowseButton.addActionListener(e -> {
      JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnValue = fc.showSaveDialog(null);
      if (returnValue == JFileChooser.APPROVE_OPTION)
      {
        sshKeyField.setText(fc.getSelectedFile().getAbsolutePath());
        if (keyStore != null)
        {
          @org.jetbrains.annotations.Nullable char[] passphrase = keyStore.read(sshKeyField.getText());
          if (passphrase != null)
            sshPassphraseField.setText(String.valueOf(passphrase));
        }
      }
    });
    return locationBrowseButton;
  }

  @Override
  public String getMessage()
  {
    return sshKeyField.getText();
  }

  @Override
  public char[] getInformation()
  {
    return sshPassphraseField.getPassword();
  }

  /**
   * Checks if the content of the textField denotes a valid and existing file
   */
  private class IsValidListener implements DocumentListener
  {
    @Override
    public void insertUpdate(DocumentEvent e)
    {
      _documentChanged(e);
    }


    @Override
    public void removeUpdate(DocumentEvent e)
    {
      _documentChanged(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
      _documentChanged(e);
    }

    private void _documentChanged(DocumentEvent pDocumentEvent)
    {
      try
      {
        isValidDescriptor.setValid(new File(pDocumentEvent.getDocument().getText(0, pDocumentEvent.getDocument().getLength())).isFile());
      }
      catch (BadLocationException pE)
      {
        isValidDescriptor.setValid(false);
      }
    }
  }
}
