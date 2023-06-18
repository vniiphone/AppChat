package com.org.chatapp.Activities;

import android.util.Log;

import com.org.chatapp.Utils.TDLibManager;
import com.org.chatapp.Utils.Utils;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GetMainListChatUtil implements TDLibManager.Callback {
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static boolean haveFullMainChatList = false;
    private static final String newLine = System.getProperty("line.separator");

    public static void getMainChatList(final int limit, Client client) {
        Log.d("getMainChatList", "1. synchronized (mainChatList) start");
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                Log.d("getMainChatList", "2.  if (!haveFullMainChatList");
                // send LoadChats request if there are some unknown chats and have not enough known chats
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        Log.d("mekosoft", mainChatList.toString());
                        if (object instanceof TdApi.ChatList)
                            Log.d("getMainChatList", "TdApi.ChatList:");
                        if (object instanceof TdApi.ChatListMain)
                            Log.d("getMainChatList", "TdApi.ChatListMain:");

                        switch(object.getConstructor())

                    {
                        case TdApi.Error.CONSTRUCTOR:
                            if (((TdApi.Error) object).code == 404) {
                                synchronized (mainChatList) {
                                    Log.d("getMainChatList", "3.  == 404");
                                    haveFullMainChatList = true;
                                }
                            } else {
                                Log.d("getMainChatList", "4.  != 404");
                                System.err.println("Receive an error for LoadChats:" + newLine + object);
                            }
                            break;
                        case TdApi.Ok.CONSTRUCTOR:
                            Log.d("getMainChatList", "5. TdApi.Ok.CONSTRUCTOR:");
                            // chats had already been received through updates, let's retry request
                            getMainChatList(limit, client);
                            break;
                        default:
                            Log.d("getMainChatList", "6.  default::");
                            System.err.println("Receive wrong response from TDLib:" + newLine + object);
                    }
                }
            });
            return;
        }

        Log.d("GetMainListChatUtil", "7. out of return");
        Iterator<OrderedChat> iterator = mainChatList.iterator();
        Log.d("GetMainListChatUtil", "8. First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
        for (int i = 0; i < limit && i < mainChatList.size(); i++) {
            long chatId = iterator.next().chatId;
            TdApi.Chat chat = chats.get(chatId);
            synchronized (chat) {
                Log.d("GetMainListChatUtil", chatId + ": " + chat.title);
            }
        }
        Utils.print("");
    }
}

    @Override
    public void onSetTdlibParametersSuccess() {

    }

    @Override
    public void onSetTdlibParametersError() {

    }

    @Override
    public void onResult(TdApi.Object object) {

    }

static class OrderedChat implements Comparable<ListChatsActivity.OrderedChat> {
    final long chatId;
    final TdApi.ChatPosition position;

    OrderedChat(long chatId, TdApi.ChatPosition position) {
        this.chatId = chatId;
        this.position = position;
    }

    @Override
    public int compareTo(ListChatsActivity.OrderedChat o) {
        if (this.position.order != o.position.order) {
            return o.position.order < this.position.order ? -1 : 1;
        }
        if (this.chatId != o.chatId) {
            return o.chatId < this.chatId ? -1 : 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        ListChatsActivity.OrderedChat o = (ListChatsActivity.OrderedChat) obj;
        return this.chatId == o.chatId && this.position.order == o.position.order;
    }
}
}
