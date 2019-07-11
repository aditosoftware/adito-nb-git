package de.adito.git.nbm;

import com.google.common.base.Strings;
import de.adito.git.api.INotifyUtil;
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
    Notification n = NotificationDisplayer.getDefault().notify(Strings.nullToEmpty(pTitle), icon,
                                                               Strings.nullToEmpty(pMessage), pActionListener);
    if (pAutoDispose)
    {
      Timer timer = new Timer(6500, e -> n.clear());
      timer.setRepeats(false);
      timer.start();
    }
  }
}
