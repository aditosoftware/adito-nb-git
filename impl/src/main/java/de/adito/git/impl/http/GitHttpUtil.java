package de.adito.git.impl.http;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
      return Arrays.stream(authHeader.split(" "))
          .map(pPart -> pPart.split("="))
          .filter(pKeyValuePair -> pKeyValuePair.length == 2 && "realm".equalsIgnoreCase(pKeyValuePair[0]))
          .map(pKeyValuePair -> pKeyValuePair[1].replace("\"", ""))
          .findFirst()
          .orElse("");
    }
    catch (UnirestException ignored)
    {
      return "";
    }
  }

}
