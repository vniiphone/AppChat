package com.org.chatapp.Utils;

import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

public class ChatsManager implements TDLibManager.Callback {
    private Client client;
    private ArrayList<TdApi.Chat> chatList = new ArrayList<>();
    private static ChatsManager chatsManager;

    public static ChatsManager getInstance() {
        if (chatsManager == null) {
            chatsManager = new ChatsManager();
        }
        return chatsManager;
    }

    public void getChats() {
        Log.d("lol", "getChats: ");
        TDLibManager.getClient(this).send(new TdApi.GetChats(), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                switch (object.getConstructor()) {
                    case TdApi.Chats.CONSTRUCTOR:
                        long chatIDs[] = ((TdApi.Chats) object).chatIds;
                        for (long chatID : chatIDs) {
                            Log.d("lol", "onResult: " + chatID);
                            client.send(new TdApi.GetChat(chatID), this, null);
                        }
                        Log.d("lol", "onResult: " + chatIDs.toString());
                        break;
                    case TdApi.Chat.CONSTRUCTOR:
                        Log.d("lol", "Chat.CONSTRUCTOR: " + object);
                        TdApi.Chat myChat = ((TdApi.Chat) object);
                        chatList.add(myChat);
                        break;
                }
            }
        });
    }

    @Override
    public void onSetTdlibParametersSuccess() {

    }

    @Override
    public void onSetTdlibParametersError() {

    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.UpdateUser.CONSTRUCTOR:
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                TdApi.User user = updateUser.user;
        }
    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {

    }
}
