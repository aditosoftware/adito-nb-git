package de.adito.git.impl.http;

import com.mashape.unirest.http.Unirest;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.prefs.IPrefStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author m.kaspera, 17.04.2019
 */
class GitHttpUtilTest
{

  @BeforeEach
  void setUp() throws NoSuchAlgorithmException, KeyManagementException
  {
    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(new KeyManager[0], new TrustManager[]{new X509TrustManager()
    {
      @Override
      public void checkClientTrusted(X509Certificate[] pX509Certificates, String pS)
      {

      }

      @Override
      public void checkServerTrusted(X509Certificate[] pX509Certificates, String pS)
      {

      }

      @Override
      public X509Certificate[] getAcceptedIssuers()
      {
        return new X509Certificate[0];
      }
    }}, new SecureRandom());
    Unirest.setHttpClient(HttpClientBuilder.create()
                              .disableAuthCaching()
                              .setSSLContext(ctx)
                              .build());
  }

  @Test
  void testGitLab()
  {
    String repoURL = "https://gitlab.adito.de/xrm/basic.git";
    assertEquals("GitLab", GitHttpUtil.getRealmName(repoURL, new DummyPrefStore(), new DummyKeyStore()));
  }

  @Test
  void testInvalidGithub()
  {
    String repoURL = "https://githu99b.com/aditosoftware/adito-nb-git.git";
    assertEquals("", GitHttpUtil.getRealmName(repoURL, new DummyPrefStore(), new DummyKeyStore()));
  }

  @Test
  void testGithub()
  {
    String repoURL = "https://github.com/aditosoftware/adito-nb-git.git";
    assertEquals("GitHub", GitHttpUtil.getRealmName(repoURL, new DummyPrefStore(), new DummyKeyStore()));
  }

  class DummyPrefStore implements IPrefStore
  {

    @Override
    public @Nullable String get(@NotNull String pKey)
    {
      return null;
    }

    @Override
    public @Nullable String get(@NotNull String pModulePath, @NotNull String pKey)
    {
      return null;
    }

    @Override
    public void put(@NotNull String pKey, @Nullable String pValue)
    {

    }
  }

  class DummyKeyStore implements IKeyStore
  {

    @Override
    public void save(@NotNull String pKey, @NotNull char[] pPassword, @Nullable String pDescription)
    {

    }

    @Override
    public void delete(@NotNull String pKey)
    {

    }

    @Override
    public char[] read(@NotNull String pKey)
    {
      return new char[0];
    }
  }
}
