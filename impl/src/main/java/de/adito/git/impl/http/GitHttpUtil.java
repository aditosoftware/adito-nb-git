package de.adito.git.impl.http;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.prefs.IPrefStore;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.*;
import org.apache.http.ssl.SSLContexts;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author m.kaspera, 17.04.2019
 */
public class GitHttpUtil
{

  private static final Logger LOGGER = Logger.getLogger(GitHttpUtil.class.getName());

  /**
   * @param pUrl Address of the git repo for which the realm should be determined
   * @return name of the realm, or empty String if it can't be determined
   */
  @NotNull
  public static String getRealmName(@NotNull String pUrl, @NotNull IPrefStore pPrefStore, @NotNull IKeyStore pKeyStore)
  {
    try
    {

      // ignore certificate errors, we're not concerned about them because we are only interested in obtaining the realm here, not establishing a connection
      SSLContext sslcontext = SSLContexts.custom()
          .loadTrustMaterial(null, new TrustSelfSignedStrategy())
          .build();

      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

      String netbeansCoreModulePath = "org/netbeans/core";
      String proxyHttpHost = pPrefStore.get(netbeansCoreModulePath, "proxyHttpHost");
      String proxyHttpPort = pPrefStore.get(netbeansCoreModulePath, "proxyHttpPort");
      String proxyHttpsHost = pPrefStore.get(netbeansCoreModulePath, "proxyHttpsHost");
      String proxyHttpsPort = pPrefStore.get(netbeansCoreModulePath, "proxyHttpsPort");
      HttpClientBuilder httpClientBuilder = HttpClients.custom()
          .setSSLSocketFactory(sslsf);
      if (proxyHttpsHost != null && proxyHttpsPort != null)
      {
        httpClientBuilder.setProxy(new HttpHost(proxyHttpsHost, Integer.parseInt(proxyHttpsPort)));
      }
      else if (proxyHttpHost != null && proxyHttpPort != null)
      {
        httpClientBuilder.setProxy(new HttpHost(proxyHttpHost, Integer.parseInt(proxyHttpPort)));
      }
      boolean isUseProxyAuth = Boolean.parseBoolean(pPrefStore.get(netbeansCoreModulePath, "useProxyAuthentication"));
      if (isUseProxyAuth)
      {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        char[] authenticationPassword = pKeyStore.read("proxyAuthenticationPassword");
        String authenticationUser = pPrefStore.get(netbeansCoreModulePath, "proxyAuthenticationUsername");
        if (authenticationUser != null && authenticationPassword != null)
        {
          credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(authenticationUser, new String(authenticationPassword)));
          httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
      }
      CloseableHttpClient httpclient = httpClientBuilder.build();
      Unirest.setHttpClient(httpclient);
      // end of "ignore certificates" part
      HttpResponse<String> response = Unirest.get(pUrl + "/info/refs?service=git-receive-pack")
          .asString();
      if (response.getStatus() != 401)
        response = Unirest.get(pUrl + "/info/refs?service=git-upload-pack")
            .asString();

      if (response.getStatus() != 401)
        return "";

      String authHeader = response
          .getHeaders()
          .getFirst("WWW-Authenticate");
      // github seems to use a lowercase version since recently
      if (authHeader == null)
      {
        authHeader = response
            .getHeaders()
            .getFirst("www-authenticate");
      }
      // in case no auth header is given
      if (authHeader == null)
        return "";
      return Arrays.stream(authHeader.split(" "))
          .map(pPart -> pPart.split("="))
          .filter(pKeyValuePair -> pKeyValuePair.length == 2 && "realm".equalsIgnoreCase(pKeyValuePair[0]))
          .map(pKeyValuePair -> pKeyValuePair[1].replace("\"", ""))
          .findFirst()
          .orElse("");
    }
    // if any error occurrs, we can't determine the realm and just return an empty string
    catch (UnirestException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException exception)
    {
      LOGGER.log(Level.SEVERE, exception, () -> "Git: error while trying to determine the Realm for the given url " + pUrl);
      return "";
    }
  }

}
