package de.adito.git.nbm;

import com.google.common.base.Strings;
import de.adito.git.api.INotifyUtil;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.*;

import javax.swing.*;

public class NotifyUtilImpl implements INotifyUtil
{
  @Override
  public void notify(@Nullable String pTitle, @Nullable String pMessage, boolean pAutoDispose)
  {
    Icon icon = NotificationDisplayer.Priority.NORMAL.getIcon();
    Notification n = NotificationDisplayer.getDefault().notify(Strings.nullToEmpty(pTitle), icon,
                                                               Strings.nullToEmpty(pMessage), null);
    if (pAutoDispose)
    {
      Timer timer = new Timer(6500, e -> n.clear());
      timer.setRepeats(false);
      timer.start();
    }
  }
}
