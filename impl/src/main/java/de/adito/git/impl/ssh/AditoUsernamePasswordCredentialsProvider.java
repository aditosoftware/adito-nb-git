package de.adito.git.impl.ssh;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Extends the simple UsernamePasswordCredentialsProvider by JGit. Instead of relying on the intially set username and password,
 * this class uses suppliers and re-evaluates the result of the supplier when the values are retrieved and any of them is null up to that time
 *
 * @author m.kaspera, 20.05.2020
 */
public class AditoUsernamePasswordCredentialsProvider extends UsernamePasswordCredentialsProvider
{
  private Supplier<String> usernameSupplier;
  private Supplier<char[]> passwordSupplier;

  /**
   * @param pUsernameSupplier Supplier for the username. Will be re-evaluated once get(..) is called and if the value on initalization is null
   * @param pPasswordSupplier Supplier for the password. Will be re-evaluated once get(..) is called and if the value on initalization is null
   */
  public AditoUsernamePasswordCredentialsProvider(Supplier<String> pUsernameSupplier, Supplier<char[]> pPasswordSupplier)
  {
    super(pUsernameSupplier.get(), pPasswordSupplier.get());
    usernameSupplier = pUsernameSupplier;
    passwordSupplier = pPasswordSupplier;
  }

  @Override
  public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem
  {
    super.get(uri, items);
    // if any of the values of the credentialItems is null, try and re-evaluate the supplier, maybe the value has been set by now
    for (CredentialItem i : items)
    {
      if (i instanceof CredentialItem.Username)
      {
        if (((CredentialItem.Username) i).getValue() == null)
          ((CredentialItem.Username) i).setValue(usernameSupplier.get());
      }
      else if (i instanceof CredentialItem.Password)
      {
        if (((CredentialItem.Password) i).getValue() == null)
          ((CredentialItem.Password) i).setValue(passwordSupplier.get());
      }
      else if (i instanceof CredentialItem.StringType && "Password: ".equals(i.getPromptText()))
      {
        if (((CredentialItem.StringType) i).getValue() == null)
          ((CredentialItem.StringType) i).setValue(new String(passwordSupplier.get()));
      }
      else
      {
        throw new UnsupportedCredentialItem(uri, i.getClass().getName() + ":" + i.getPromptText()); //$NON-NLS-1$
      }
    }
    return true;
  }

  @Override
  public void clear()
  {
    super.clear();
    // discard all info about username and password
    usernameSupplier = () -> "";
    Arrays.fill(passwordSupplier.get(), '0');
    passwordSupplier = () -> new char[0];
  }
}
