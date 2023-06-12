package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MainActivity extends AppCompatActivity implements TDLibManager.Callback {
    private static Client client;
    boolean haveAuthorization = false;
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static final String newLine = System.getProperty("line.separator");
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TdApi.GetAuthorizationState AuthState = new TdApi.GetAuthorizationState();
        client = TDLibManager.getClient(this);
        client.send(AuthState, this);

        // Khởi tạo client và gửi yêu cầu GetAuthorizationState
        client = TDLibManager.getClient(this);
        TdApi.GetAuthorizationState request = new TdApi.GetAuthorizationState();
        client.send(request, this);
        Log.d("MainActivity", "client.send(request, this); at line 38");

    }


    @Override
    public void onSetTdlibParametersSuccess() {
        // Xử lý khi SetTdlibParameters thành công
        Log.d("MainActivity", "onSetTdlibParametersSuccess");
    }

    @Override
    public void onSetTdlibParametersError() {
        // Xử lý khi SetTdlibParameters gặp lỗi
        Log.d("MainActivity", "onSetTdlibParametersError");
    }

    @Override
    public void onResult(TdApi.Object object) {
        // Xử lý kết quả nhận được từ TDLib
        if (object.getConstructor() == TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
            onAuthStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
        }
    }

    private void onAuthStateUpdated(TdApi.AuthorizationState authorizationState) {
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                // Xử lý trạng thái chờ thông số TDLib
                Log.d("Main", "AuthorizationStateWaitTdlibParameters");
                String directoryPath = MainActivity.this.getExternalFilesDir(null) + File.separator + "AppChat" + File.separator;
                TDLibManager.setTdlibParameters(client, this, directoryPath);
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                // Xử lý trạng thái chờ khóa mã hóa
                Log.d("Main", "AuthorizationStateWaitEncryptionKey");
                client.send(new TdApi.CheckDatabaseEncryptionKey(), this);
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                // Đã xác thực, chuyển hướng đến LoginActivity
                Log.d("Main", "chuyển hướng đến LoginActivity");
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                // Xử lý trạng thái chờ mã xác thực
                Log.d("Main", "trạng thái chờ mã xác thực");
                Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(authIntent);
                finish();
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                // Đã đăng nhập trước đó, chuyển hướng đến ConversationActivity
                Log.d("Main", "trạng thái chuyển hướng đến ConversationActivity");

                Log.d("AuthorizationStateReady", "Gọi getMainListChat: ");
                getMainChatList(1);
                Intent conversationIntent = new Intent(MainActivity.this, ConversationActivity.class);
                startActivity(conversationIntent);
                finish();
                break;
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                Intent RegistrationIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(RegistrationIntent);
                finish();

//                client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                System.out.println("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                System.out.println("Closing");
                break;
            default:
                System.err.println("Unsupported authorization state:" + authorizationState);
        }

    }
    private static void getMainChatList(final int limit) {
        synchronized (mainChatList) {
            Log.d("onResult", String.valueOf(mainChatList.size()));
            if ( limit > mainChatList.size()) {
                // send LoadChats request if there are some unknown chats and have not enough known chats
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        Log.d("onResult","getMainChatList");
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                if (((TdApi.Error) object).code == 404) {
                                    synchronized (mainChatList) {
                                        Log.d("Xác thực","TdApi.ConnectionStateReady.CONSTRUCTOR:"+ TdApi.ConnectionStateReady.CONSTRUCTOR);
                                        Log.d("onResult","1 .mainChatList");
                                    }
                                } else {
                                    Log.d("onResult","2 .mainChatList");
                                    System.err.println("Receive an error for LoadChats:" + newLine + object);
                                }
                                break;
                            case TdApi.Ok.CONSTRUCTOR:
                                Log.d("onResult","3 .TdApi.Ok.CONSTRUCTOR");
                                // chats had already been received through updates, let's retry request
                                getMainChatList(limit);
                                break;
                            default:
                                Log.d("onResult","4 .default");
                                System.err.println("Receive wrong response from TDLib:" + newLine + object);
                        }
                    }
                });
                return;
            }

            java.util.Iterator<OrderedChat> iter = mainChatList.iterator();
            System.out.println();
            System.out.println("First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
            Log.d("MainListChat","Size: "+mainChatList.size());
            for (int i = 0; i < limit && i < mainChatList.size(); i++) {
                long chatId = iter.next().chatId;
                TdApi.Chat chat = chats.get(chatId);
                synchronized (chat) {
                    System.out.println(chatId + ": " + chat.title);
                    Log.d("MainListChat",chatId + ": " + chat.title);
                }
            }

        }
    }

    private static class OrderedChat implements Comparable<OrderedChat> {
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
}

