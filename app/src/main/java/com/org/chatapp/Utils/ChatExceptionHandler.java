package com.org.chatapp.Utils;

import org.drinkless.td.libcore.telegram.Client;

public class ChatExceptionHandler implements Client.ExceptionHandler {
    @Override
    public void onException(Throwable e) {
        e.printStackTrace();
    }
}
