package de.adito.git.gui.dialogs;

import org.jetbrains.annotations.Nullable;

/**
 * a util class for the dialogs
 *
 * @author a.arnold 31.10.2018
 */
public class DialogResult {
public class DialogResult<T> {

    private boolean pressedOk;
    private String message;
    private T information;

    DialogResult(boolean pPressedOk, @Nullable String pMessage) {
    DialogResult(boolean pPressedOk, @Nullable String pMessage){
        this(pPressedOk, pMessage, null);
    }

    DialogResult(boolean pPressedOk, @Nullable String pMessage, @Nullable T pInformation) {
        pressedOk = pPressedOk;
        message = pMessage;
        information = pInformation;
    }

    public boolean isPressedOk() {
        return pressedOk;
    }

    public String getMessage() {
        return message;
    }

    public T getInformation() {
        return information;
    }
}
