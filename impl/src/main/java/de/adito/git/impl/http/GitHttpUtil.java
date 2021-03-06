package de.adito.git.impl.http;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

  /**
   * @param pUrl Address of the git repo for which the realm should be determined
   * @return name of the realm, or empty String if it can't be determined
   */
  @NotNull
  public static String getRealmName(@NotNull String pUrl)
  {
    try
    {

      // ignore certificate errors, we're not concerned about them because we are only interested in obtaining the realm here, not establishing a connection
      SSLContext sslcontext = SSLContexts.custom()
          .loadTrustMaterial(null, new TrustSelfSignedStrategy())
          .build();

      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      CloseableHttpClient httpclient = HttpClients.custom()
          .setSSLSocketFactory(sslsf)
          .build();
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
      Logger.getLogger(GitHttpUtil.class.getName()).log(Level.SEVERE, exception, () -> "Git: error while trying to determine the Realm for the given url " + pUrl);
      return "";
    }
  }

}
