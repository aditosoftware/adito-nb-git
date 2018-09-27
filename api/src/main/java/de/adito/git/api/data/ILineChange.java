package de.adito.git.api.data;

/**
 * @author m.kaspera 25.09.2018
 */
public interface ILineChange {

    EChangeType getType();

    String getLineContent();

}
