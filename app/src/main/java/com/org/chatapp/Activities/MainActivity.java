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

import com.org.chatapp.Local.TokenStorage;
import com.org.chatapp.R;
import com.org.chatapp.Utils.ChatsManager;
import com.org.chatapp.Utils.TDLibManager;
import com.org.chatapp.Utils.Utils;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MainActivity extends AppCompatActivity implements TDLibManager.Callback {
    private static Client client;
    boolean haveAuthorization = false;
    //    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static final String newLine = System.getProperty("line.separator");
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TdApi.GetAuthorizationState AuthState = new TdApi.GetAuthorizationState();
        client = TDLibManager.getClient(this);
        client.send(AuthState, this::onResult);
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
        // Xử lý kết quả nhận được từ TDLib
//        if (object.getConstructor() == TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
//            onAuthStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
//        }
        switch (object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                Log.d("onResult-->", "onResult: UpdateAuthorizationState.CONSTRUCTOR");
                onAuthStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                break;
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                Log.d("onResult-->", "onResult: AuthorizationStateWaitTdlibParameters");
                TdApi.TdlibParameters authStateRequest = new TdApi.TdlibParameters();
                // Gửi yêu cầu khởi tạo TDLib parameters
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.apiId = 25729812;
                parameters.apiHash = "8285aa99a082199b9884819998f28c94";
                parameters.databaseDirectory = getApplicationContext().getFilesDir().getAbsolutePath();
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Pixel 6";
                authStateRequest.systemVersion = "12.0";
                parameters.applicationVersion = "0.0.2";
                authStateRequest.enableStorageOptimizer = true;
                client.send(new TdApi.SetTdlibParameters(parameters), this::onResult);
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                Log.d("onResult-->", "onResult: AuthorizationStateWaitEncryptionKey");
                client.send(new TdApi.CheckDatabaseEncryptionKey(), this::onResult);
                TdApi.GetAuthorizationState AuthState = new TdApi.GetAuthorizationState();
                client.send(AuthState, this::onResult);
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                Log.d("onResult-->", "AuthorizationStateWaitPhoneNumber: ");
                TdApi.PhoneNumberAuthenticationSettings authenticationSettings = new TdApi.PhoneNumberAuthenticationSettings();
                authenticationSettings.allowFlashCall = true;
                authenticationSettings.isCurrentPhoneNumber = true;
                client.send(new TdApi.SetAuthenticationPhoneNumber("+917418189531", authenticationSettings), this);
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;

            // Todo: Implement methods to save or get Token
//                authenticationSettings.authenticationTokens = token;
//            client.send(new TdApi.SetAuthenticationPhoneNumber("+917418189531", ), this);
//            break;
        }
    }


    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {
        Log.d("onUpdatesReceived", "Update: " + update);
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
                String accessToken = "your_access_token_here";
                TokenStorage tokenStorage = new TokenStorage(MainActivity.this);
                tokenStorage.saveAccessToken(accessToken);
                Log.d("Main", "Gọi ListConversationsActivity: ");
                Intent conversationIntent = new Intent(MainActivity.this, ListConversationsActivity.class);
                startActivity(conversationIntent);
                finish();
                break;
                /*
//                getMainChatList(1);
//                ListChatsActivity.getMainChatList(100);
//                GetMainListChatUtil.getMainChatList(20, client);
//                ChatsManager.getInstance().getChats();


             getChats();
                synchronized (chatList) {
                    Log.d("lol", "AuthorizationStateReady.CONSTRUCTOR:: " + chatList.size());
                    for (TdApi.Chat chat : chatList) {
                        Log.d("lol2", "Chat: " + chat.title + " lastMessage: " + chat.lastMessage);
                    }
                }
//                Utils.getMainChatList(20);*/

            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                Intent RegistrationIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(RegistrationIntent);
                finish();
                break;
            }
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                TokenStorage tokenStoragelogout = new TokenStorage(MainActivity.this);
                tokenStoragelogout.clearAccessToken();
                System.out.println("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                TokenStorage tokenStorageGet = new TokenStorage(MainActivity.this);
                if (tokenStorageGet.hasAccessToken()) {
                    String accessTokenGet = tokenStorageGet.getAccessToken();
                    // Sử dụng accessToken để thực hiện các yêu cầu xác thực hoặc thao tác khác

                } else {
                    // Người dùng chưa đăng nhập, chuyển hướng đến màn hình đăng nhập hoặc thực hiện các xử lý tương ứng
                }
               /* Intent openMain = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(openMain);*/
                break;
            default:
                System.err.println("Unsupported authorization state:" + authorizationState);
        }

    }

    private ArrayList<TdApi.Chat> chatList = new ArrayList<>();

    public ArrayList<TdApi.Chat> getChats() {
        Log.d("lol", "getChats: " + chatList.size());
//        TdApi.ChatList chatList1 = new TdApi.ChatList()
        client.send(new TdApi.GetChats(new TdApi.ChatList() {
            @Override
            public int getConstructor() {
                return TdApi.ChatListMain.CONSTRUCTOR;
            }
        }, 20), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                switch (object.getConstructor()) {
                    case TdApi.Error.CONSTRUCTOR:
                        Log.d("lol", "object: " + object);
                        break;
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
                        Log.d("lol", "Chat.CONSTRUCTOR: " + chatList.size());
                        break;
                }
            }
        });
        Log.d("lol", "getChatsList: " + chatList.size());
        return chatList;
    }

    /*private static void getMainChatList(final int limit) {
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
    }*/
}

