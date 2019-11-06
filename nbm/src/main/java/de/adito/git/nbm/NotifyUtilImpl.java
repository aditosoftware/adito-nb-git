package de.adito.git.nbm;

import com.google.common.base.Strings;
import de.adito.git.api.INotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;

import javax.swing.*;
import java.awt.event.ActionListener;

public class NotifyUtilImpl implements INotifyUtil
{
  @Override
  public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose)
  {
    notify(pTitle, pMessage, pAutoDispose, null);
  }

  public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener)
  {
    Icon icon = NotificationDisplayer.Priority.NORMAL.getIcon();
    notify(icon, pTitle, pMessage, pAutoDispose, pActionListener);
  }

  @Override
  public void notify(@NotNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose)
  {
    notify(pEx, pMessage, pAutoDispose, null);
  }

  @Override
  public void notify(@NotNull Exception pEx, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener)
  {
    notify("Encountered Exception: " + pEx.getClass().getSimpleName(), pMessage + "\n\nConsult the IDE log for further details",
           pAutoDispose, pActionListener);
    throw new RuntimeException(pEx);
  }

  private void notify(@NotNull Icon pIcon, @Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose, @Nullable ActionListener pActionListener)
  {
    Notification n = NotificationDisplayer.getDefault().notify(Strings.nullToEmpty(pTitle), pIcon,
                                                               Strings.nullToEmpty(pMessage), pActionListener);
    if (pAutoDispose)
    {
      Timer timer = new Timer(6500, e -> n.clear());
      timer.setRepeats(false);
      timer.start();
    }
  }
}
