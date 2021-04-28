package de.adito.git.nbm.wizard;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author m.kaspera, 27.04.2021
 */
public class CloneWizardPanel1Test
{

  /**
   * Tests if the Regex URL_PATTERN returns the wanted results for defined inputs
   *
   * @param pInuput   Input that should be tested in the regex
   * @param pExpected expected result of the regex match
   */
  @ParameterizedTest
  @MethodSource("_provideUrls")
  void testCorrectUrls(@NotNull String pInuput, boolean pExpected)
  {
    assertEquals(pExpected, Pattern.compile(CloneWizardPanel1.URL_PATTERN).matcher(pInuput).matches());
  }

  /**
   * @return Arguments for the test
   */
  @NotNull
  private static Stream<Arguments> _provideUrls()
  {
    return Stream.of(Arguments.of("https://gitintern.aditosoftware.local/devs/aditoonline.git", true),
                     Arguments.of("https://gitlab.adito.de/xrm/basic.git", true),
                     Arguments.of("git@gitlab.adito.de:xrm/basic.git", true),
                     Arguments.of("git@github.com:aditosoftware/adito-nb-nodejs.git", true),
                     Arguments.of("https://github.com/aditosoftware/adito-nb-nodejs.git", true),
                     Arguments.of("https://gitintern.aditosoftware.local/devs/aditoonline.com", false),
                     Arguments.of("www.heise.de", false),
                     Arguments.of("https://www.google.com", false));
  }
}
