package de.adito.git.impl.ssh;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 30.11.2021
 */
@SuppressWarnings("squid:S3011") // Wir verwenden hier reflections -> setAccessible call ist notwendig
class ClearHttpCacheHandler
{

  private static Class<?> authCacheValue;
  private static Method getPath;
  private static Method removeFromCache;

  private ClearHttpCacheHandler()
  {
  }

  /**
   * Leert den Base Java Authentication cache für eine gegebene URL, es werden nur Einträge entfernt, die der URL und dem Pfad entsprechen.
   * Dies muss getan werden, da JGit bei NONE Authentifizierung davon ausgeht, dass kein Auth Header mitgegeben wird. Gibt es im besagten Cache aber einen Eintrag für die
   * URL, so wird der Auth Header gesetzt und JGit meint, es bräuchte keine Authentifizierung, weshalb der nächste Request fehlschlagen kann
   *
   * @param pURL URL, für die die Cache Einträge entfernt werden sollen
   */
  static void clearCache(@NotNull String pURL)
  {
    try
    {
      if (removeFromCache == null)
        _populateMethodCalls();
      URL url = URI.create(pURL).toURL();

      Field cacheField = authCacheValue.getDeclaredField("cache");
      cacheField.setAccessible(true);
      Object cacheValue = cacheField.get(null);
      Field hashTableField = cacheValue.getClass().getDeclaredField("hashtable");
      hashTableField.setAccessible(true);
      @SuppressWarnings("unchecked") // Cast auf HashMap um an die Authenticators zu kommen
      HashMap<String, List<?>> hashtable = (HashMap<String, List<?>>) hashTableField.get(cacheValue);
      List<?> matchingAuthenticators = hashtable.entrySet()
          .stream()
          .filter(pEntry -> pEntry.getKey().contains(url.getHost()))
          .flatMap(pEntry -> pEntry.getValue().stream())
          .collect(Collectors.toList());
      matchingAuthenticators.stream()
          .filter(pAuthenticator -> _isPathEqual(url, pAuthenticator))
          .forEach(ClearHttpCacheHandler::_removeFromCache);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private static void _removeFromCache(@NotNull Object obj)
  {
    try
    {
      removeFromCache.invoke(obj);
    }
    catch (IllegalAccessException | InvocationTargetException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  private static boolean _isPathEqual(@NotNull URL url, @NotNull Object pAuthenticator)
  {
    try
    {
      return ((String) getPath.invoke(pAuthenticator)).startsWith(url.getPath());
    }
    catch (IllegalAccessException | InvocationTargetException pE)
    {
      throw new RuntimeException(pE);
    }
  }


  private static void _populateMethodCalls() throws NoSuchMethodException, ClassNotFoundException
  {
    authCacheValue = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
    Class<?> authenticationInfo = Class.forName("sun.net.www.protocol.http.AuthenticationInfo");
    getPath = authenticationInfo.getDeclaredMethod("getPath");
    getPath.setAccessible(true);
    removeFromCache = authenticationInfo.getDeclaredMethod("removeFromCache");
    removeFromCache.setAccessible(true);
  }

}
