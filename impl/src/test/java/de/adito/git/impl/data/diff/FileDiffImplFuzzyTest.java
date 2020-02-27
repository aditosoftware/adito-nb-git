package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.EChangeType;
import de.adito.git.api.data.diff.IFileContentInfo;
import de.adito.git.impl.StandAloneDiffProviderImpl;
import de.adito.git.impl.data.diff.fuzzing.IRandomGenerator;
import de.adito.git.impl.data.diff.fuzzing.ProbabilityEventGenerator;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.diff.EditList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exmaple Distribution of x - (int) Math.sqrt(random.nextInt(x * x)) for n = 100000
 * x = 5:  {0=0, 1=36010, 2=28181, 3=19870, 4=11927, 5=4012}
 * x = 10: {0=0, 1=18991, 2=17173, 3=14971, 4=12969, 5=11096, 6=8843, 7=6967, 8=4983, 9=3002, 10=1005}
 * x = 20: {0=0, 1=9825, 2=9167, 3=8635, 4=8195, 5=7655, 6=7328, 7=6876, 8=6225, 9=5697, 10=5334, 11=4642, 12=4253, 13=3726, 14=3335, 15=2816, 16=2273, 17=1809, 18=1231, 19=750, 20=228}
 *
 * @author m.kaspera, 26.02.2020
 */
public class FileDiffImplFuzzyTest
{

  private static final int NUM_FUZZ_TESTS = 100000;
  private static final int MAX_NUM_CHUNKS_PER_FUZZ = 100;
  private static final int MIN_NUM_CHUNKS_PER_FUZZ = 3;
  private static final int MAX_LINES_PER_DELETED_CHUNK = 10;
  private static final int MAX_LINES_PER_ADDED_CHUNK = 10;
  private static final int MAX_LINES_PER_MODIFIED_CHUNK = 5;
  private static final int MAX_LINES_PER_SAME_CHUNK = 5;
  private static final int MAX_WORDS_PER_LINE = 10;

  private enum TYPE
  {SAME, ADD, DELETE, MODIFY}

  private ProbabilityEventGenerator<TYPE> randomTypeGenerator;
  private int seed;

  @BeforeEach
  void initRandomGenerator()
  {
    seed = new Random().nextInt();
    randomTypeGenerator = new ProbabilityEventGenerator.Builder<TYPE>().setRandomGenerator(new IRandomGeneratorDefaultImpl(seed))
        .addEvent(new ProbabilityEventGenerator.EventShares<>(5, TYPE.SAME))
        .addEvent(new ProbabilityEventGenerator.EventShares<>(2, TYPE.MODIFY))
        .addEvent(new ProbabilityEventGenerator.EventShares<>(2, TYPE.ADD))
        .addEvent(new ProbabilityEventGenerator.EventShares<>(1, TYPE.DELETE)).create();
  }


  /**
   * Creates a given amount of random files with random modifications and tests if, after accepting all changes, the old and new version match
   */
  @Test
  void testRandomStrings()
  {
    Random random = new Random(seed);
    StandAloneDiffProviderImpl diffProvider = new StandAloneDiffProviderImpl(new FileSystemUtilTestStub());
    for (int index = 0; index < NUM_FUZZ_TESTS; index++)
    {
      Pair<String, String> versions = _createVersions(random);
      EditList editList = diffProvider.diff(versions.getLeft(), versions.getRight(), true);
      FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE, "filea", "fileb");
      IFileContentInfo oldFileContent = new FileContentInfoImpl(versions::getLeft, () -> StandardCharsets.UTF_8);
      IFileContentInfo newFileContent = new FileContentInfoImpl(versions::getRight, () -> StandardCharsets.UTF_8);
      FileDiffImpl fileDiff = new FileDiffImpl(fileDiffHeader, editList, oldFileContent, newFileContent);
      List<Integer> shuffledIntegerList = _createShuffledIntegerList(fileDiff.getChangeDeltas().size());
      shuffledIntegerList.forEach(pIndex -> fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(pIndex)));
      assertEquals(versions.getLeft(), fileDiff.getText(EChangeSide.NEW), () ->
          _getFailInfoString(seed, versions, shuffledIntegerList, fileDiff.getText(EChangeSide.NEW)));
    }
  }

  /**
   * Creates a string that gives information about all the parameters used for a test, such that the test can be replicated (all random events are based on a seed)
   *
   * @param pSeed             Seed used for the test
   * @param pVersions         The two texts as "original" and "changed"
   * @param pAcceptDeltaOrder order in which the changeDeltas were accepted
   * @param pResult           result of accepting all changeDeltas
   * @return Information about a single fuzz test
   */
  private String _getFailInfoString(int pSeed, Pair<String, String> pVersions, List<Integer> pAcceptDeltaOrder, String pResult)
  {
    return "Seed: " + pSeed
        + "\n----------------------------------\nVersions:\n" + pVersions.getLeft()
        + "\n.--.--.--.--.--.--.--.--.--.--.--.--.--.--.--.--.--.--.--.--\n" + pVersions.getRight()
        + "\n#######################################\nAcceptance order:" + pAcceptDeltaOrder
        + "\n\n-------------------------------------\nResult:\n" + pResult;
  }

  /**
   * Create a list of ascending integers from 0 to pSize, and then shuffles it
   *
   * @param pSize number of elements in the list
   * @return Shuffled list of unique integers
   */
  private List<Integer> _createShuffledIntegerList(int pSize)
  {
    List<Integer> list = new ArrayList<>();
    for (int index = 0; index < pSize; index++)
    {
      list.add(index);
    }
    Collections.shuffle(list);
    return list;
  }

  /**
   * creates two random texts, consisting of a random amount of ADDED, CHANGED, MODIFIED and SAME chunks
   *
   * @param pRandom random used to create the texts
   * @return Pair of two strings, left being the "original" version of a text and right being the "changed" version of the text
   */
  private Pair<String, String> _createVersions(Random pRandom)
  {
    int numChunks = MIN_NUM_CHUNKS_PER_FUZZ + MAX_NUM_CHUNKS_PER_FUZZ - (int) Math.sqrt(pRandom.nextInt(MAX_NUM_CHUNKS_PER_FUZZ * MAX_NUM_CHUNKS_PER_FUZZ));
    StringBuilder oldVersion = new StringBuilder();
    StringBuilder newVersion = new StringBuilder();
    for (int index = 0; index < numChunks; index++)
    {
      TYPE type = randomTypeGenerator.randomEvent();
      Pair<String, String> chunkPair;
      switch (type)
      {
        case SAME:
          chunkPair = _createSameChunk(pRandom);
          break;
        case DELETE:
          chunkPair = _createDeletedChunk(pRandom);
          break;
        case ADD:
          chunkPair = _createAddedChunk(pRandom);
          break;
        default:
          chunkPair = _createModifiedChunk(pRandom);
      }
      oldVersion.append(chunkPair.getLeft());
      newVersion.append(chunkPair.getRight());
    }
    return new MutablePair<>(oldVersion.toString(), newVersion.toString());
  }

  /**
   * Creates a random number of random lines that represents "DELETE" chunk
   *
   * @param pRandom Random used to create the random lines
   * @return Pair of two Strings, left consisting of a random number of random lines and right being empty
   */
  private Pair<String, String> _createDeletedChunk(Random pRandom)
  {
    int numLines = MAX_LINES_PER_DELETED_CHUNK - (int) Math.sqrt(pRandom.nextInt(MAX_LINES_PER_DELETED_CHUNK * MAX_LINES_PER_DELETED_CHUNK));
    StringBuilder deletedString = new StringBuilder();
    for (int index = 0; index < numLines; index++)
    {
      deletedString.append(_createLine(pRandom, MAX_WORDS_PER_LINE)).append("\n");
    }
    return new MutablePair<>(deletedString.toString(), "");
  }

  /**
   * Creates a random number of random lines that represents an "ADD" chunk
   *
   * @param pRandom Random used to create the random lines
   * @return Pair of two Strings, left being empty and right consisting of a random number of random lines
   */
  private Pair<String, String> _createAddedChunk(Random pRandom)
  {
    int numLines = MAX_LINES_PER_ADDED_CHUNK - (int) Math.sqrt(pRandom.nextInt(MAX_LINES_PER_ADDED_CHUNK * MAX_LINES_PER_ADDED_CHUNK));
    StringBuilder deletedString = new StringBuilder();
    for (int index = 0; index < numLines; index++)
    {
      deletedString.append(_createLine(pRandom, MAX_WORDS_PER_LINE)).append("\n");
    }
    return new MutablePair<>("", deletedString.toString());
  }

  /**
   * Creates a random number of lines.
   *
   * @param pRandom Random used to create the lines
   * @return Pair of two identical Strings, each representing a random number of random lines
   */
  private Pair<String, String> _createSameChunk(Random pRandom)
  {
    int numLines = MAX_LINES_PER_SAME_CHUNK - (int) Math.sqrt(pRandom.nextInt(MAX_LINES_PER_SAME_CHUNK * MAX_LINES_PER_SAME_CHUNK));
    StringBuilder string = new StringBuilder();
    for (int index = 0; index < numLines; index++)
    {
      string.append(_createLine(pRandom, MAX_WORDS_PER_LINE)).append("\n");
    }
    return new MutablePair<>(string.toString(), string.toString());
  }

  /**
   * Rolls a random amount of lines, creates that number of random lines and changes all of those random lines for the second string
   *
   * @param pRandom Random used for rolling the number of lines, the lines itself and the changes to the lines
   * @return Pair of random number of lines as left, and modified lines as right
   */
  private Pair<String, String> _createModifiedChunk(Random pRandom)
  {
    int numLines = MAX_LINES_PER_MODIFIED_CHUNK - (int) Math.sqrt(pRandom.nextInt(MAX_LINES_PER_MODIFIED_CHUNK * MAX_LINES_PER_MODIFIED_CHUNK));
    StringBuilder string = new StringBuilder();
    StringBuilder changedString = new StringBuilder();
    for (int index = 0; index < numLines; index++)
    {
      String originalLine = _createLine(pRandom, MAX_WORDS_PER_LINE);
      string.append(originalLine).append("\n");
      changedString.append(_modifyLine(pRandom, originalLine)).append("\n");
    }
    return new MutablePair<>(string.toString(), changedString.toString());
  }

  /**
   * modifies a line by introducing a random change in the line
   *
   * @param pRandom Random used to roll the changes to the line and where that change occurs
   * @param pString String to randomly modify
   * @return modified line
   */
  private String _modifyLine(Random pRandom, String pString)
  {
    int startIndex = pRandom.nextInt(Math.max(1, pString.length() - 2));
    int endIndex = startIndex + pRandom.nextInt(Math.max(1, pString.length() - 1 - startIndex));
    return pString.substring(0, startIndex) + _createLine(pRandom, 4) + pString.substring(endIndex);
  }

  /**
   * creates a random line, by rolling an amount of words for the line and then creating random words. Less words are more likely than many
   *
   * @param pRandom     Random used for rolling the amount of words and the words itself
   * @param pUpperBound maximum number of words in the line
   * @return a random line
   */
  private String _createLine(Random pRandom, int pUpperBound)
  {
    // 10% change of an empty line
    boolean isEmptyLine = pRandom.nextInt(10) >= 9;
    if (isEmptyLine)
      return "";
    int numWords = pUpperBound - (int) Math.sqrt(pRandom.nextInt(pUpperBound * pUpperBound));
    StringBuilder stringBuilder = new StringBuilder();
    for (int index = 0; index < numWords; index++)
    {
      stringBuilder.append(_createWord(pRandom)).append(" ");
    }
    return stringBuilder.toString();
  }

  /**
   * Create a random word by rolling the amount of characters for the word and then the characters itself
   *
   * @param pRandom Random used for rolling the amount of characters and the charactes itself
   * @return random word
   */
  private String _createWord(Random pRandom)
  {
    int wordLen = 3 + pRandom.nextInt(12);
    char[] word = new char[wordLen];
    for (int index = 0; index < wordLen; index++)
    {
      // 33 to 126 are valid, visible ASCII chars (Capital + lowercase letters, numbers, special signs like &%$/...
      word[index] = (char) (33 + pRandom.nextInt(126 - 33));
    }
    return new String(word);
  }

  /**
   * Random Generator impl using the java.util.Random class with a given seed
   */
  private static class IRandomGeneratorDefaultImpl implements IRandomGenerator
  {

    private final Random random;

    public IRandomGeneratorDefaultImpl(int pSeed)
    {
      random = new Random(pSeed);
    }

    @Override
    public double get()
    {
      return random.nextDouble();
    }
  }
}
