package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ListChatsActivity extends AppCompatActivity implements TDLibManager.Callback {
    public static Client client;
    Button btnOpen;
    TextView txt_Count;
    RecyclerView rcl_List;
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static boolean haveFullMainChatList = false;
    private static final String newLine = System.getProperty("line.separator");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_chats);

        client = TDLibManager.getClient(this);

        Log.d("Client:", "1. Client khởi tạo:" + client.toString());
        txt_Count = findViewById(R.id.txt_trochuyencount2);
//        rcl_List = findViewById(R.id.recyclerview_conversation2);
//        rcl_List.setLayoutManager(rcl_List);
        getMainChatList(100);
        Log.d("ListChatsActivity", "6. onCreate  retrrnn: " + mainChatList.size());
        synchronized (mainChatList) {
            Log.d("ListChatsActivity", "6. onCreate  synchronized (mainChatList): " + mainChatList.size());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ListChatsActivity", "List Chats");
        Toast.makeText(this, "List Chats", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ListChatsActivity", "onDestroy");
        client.close();
    }

    private static void getChatPostion() {
        Log.d("getChatPostion", "getChatPostion.1 Khởi chạy ");
        TdApi.UpdateChatPosition updateChatPosition = new TdApi.UpdateChatPosition();

    }

    public static void getMainChatList(final int limit) {
        Log.d("ListChatsActivity", "2. getMainChatList");
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                Log.d("ListChatsActivity", "3.  if (!haveFullMainChatList && limit > mainChatList.size())");
                // Gửi yêu cầu LoadChats nếu có một số chat chưa được xác định và không đủ chat đã biết
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), object -> {
                    Log.d("ListChatsActivity", "4.  Result: " + object);
                    switch (object.getConstructor()) {
                        case TdApi.Error.CONSTRUCTOR:
                            Log.d("ListChatsActivity", "7.  Error");
                            if (((TdApi.Error) object).code == 404) {
                                synchronized (mainChatList) {
                                    haveFullMainChatList = true;
                                    Log.d("ListChatsActivity", "First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
                                }
                            } else {
                                Log.e("ListChatsActivity", "Receive an error for LoadChats:" + object);
                            }
                            break;
                        case TdApi.Ok.CONSTRUCTOR:
                            Log.d("ListChatsActivity", "First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
                            Log.d("ListChatsActivity", "7. case:ok");
                            // Chats đã được nhận thông qua các cập nhật, hãy thử lại yêu cầu
                            getMainChatList(limit);
                            break;
                        default:
                            Log.d("ListChatsActivity", "7. Default");
                            Log.e("ListChatsActivity", "Receive wrong response from TDLib:" + object);
                    }
                });
                return;
            }
            Log.d("ListChatsActivity", "5. out of return");
            Iterator<OrderedChat> iter = mainChatList.iterator();
            Log.d("ListChatsActivity", "First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
            for (int i = 0; i < limit && i < mainChatList.size(); i++) {
                long chatId = iter.next().chatId;
                TdApi.Chat chat = chats.get(chatId);
                synchronized (chat) {
                    Log.d("ListChatsActivity", chatId + ": " + chat.title);
                }
            }
        }
    }


    static class OrderedChat implements Comparable<OrderedChat> {
        final long chatId;
        final TdApi.ChatPosition position;

        OrderedChat(long chatId, TdApi.ChatPosition position) {
            this.chatId = chatId;
            this.position = position;
        }

        @Override
        public int compareTo(OrderedChat o) {
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
            OrderedChat o = (OrderedChat) obj;
            return this.chatId == o.chatId && this.position.order == o.position.order;
        }
    }

    private static class DefaultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            System.out.println(object.toString());
        }
    }

    @Override
    public void onSetTdlibParametersSuccess() {
        Log.d("onSetTdlibParametersSuccess", "Success");
    }

    @Override
    public void onSetTdlibParametersError() {
        Log.d("onSetTdlibParametersError", "Error");
    }

    @Override
    public void onResult(TdApi.Object object) {
        Log.d("onResult", "onResult: " + object);
    }
}