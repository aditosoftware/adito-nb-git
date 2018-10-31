package de.adito.git.gui.dialogs;

import org.jetbrains.annotations.Nullable;

public class DialogResult {

    private boolean pressedOk;
    private String message;
    private Object information;

    DialogResult(boolean pPressedOk, @Nullable String pMessage){
        this(pPressedOk, pMessage, null);
    }

    DialogResult(boolean pPressedOk, @Nullable String pMessage, @Nullable Object pInformation) {
        pressedOk = pPressedOk;
        message = pMessage;
        information = pInformation;
    }

    public boolean isPressedOk(){
        return pressedOk;
    }

    public String getMessage() {
        return message;
    }

    public Object getInformation() {
        return information;
    }
}
