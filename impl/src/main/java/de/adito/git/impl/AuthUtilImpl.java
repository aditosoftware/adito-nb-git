package de.adito.git.impl;

import de.adito.git.api.IAuthUtil;
import org.jetbrains.annotations.*;

import java.net.PasswordAuthentication;

/**
 * @author p.neub, 22.08.2022
 */
public class AuthUtilImpl implements IAuthUtil
{
  private ThreadLocal<Boolean> shouldReuseAuth = ThreadLocal.withInitial(() -> false);
  private ThreadLocal<PasswordAuthentication> rememberedAuth = new ThreadLocal<>();

  @Override
  public void auth(@NotNull PasswordAuthentication pAuth)
  {
    if(shouldReuseAuth.get())
      rememberedAuth.set(pAuth);
  }

  @Override
  @Nullable
  public PasswordAuthentication getAuth()
  {
    return rememberedAuth.get();
  }

  @Override
  public void reuseAuthIfNeededMoreThanOnce(Runnable pCode)
  {
    shouldReuseAuth.set(true);
    pCode.run();
    rememberedAuth.set(null);
    shouldReuseAuth.set(false);
  }
}
