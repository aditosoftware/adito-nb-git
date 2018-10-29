package de.adito.git.gui.dialogs;

import org.jetbrains.annotations.Nullable;

public class DialogResult {

    private boolean pressedOk;

    private String message;

    DialogResult(boolean pPressedOk, @Nullable String pMessage){
        pressedOk = pPressedOk;
        message = pMessage;
    }

    public boolean isPressedOk(){
        return pressedOk;
    }

    public String getMessage() {
        return message;
    }

}
