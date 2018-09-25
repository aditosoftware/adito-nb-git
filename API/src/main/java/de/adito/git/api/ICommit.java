package de.adito.git.api;

/**
 * @author m.kaspera 25.09.2018
 */
public interface ICommit {

    String getAuthor();

    String getEmail();

    String getCommiter();

    long getTime();

    String getMessage();

    String getShortMessage();
}
