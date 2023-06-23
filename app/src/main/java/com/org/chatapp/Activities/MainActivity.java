package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MainActivity extends AppCompatActivity implements TDLibManager.Callback {
    private static Client client;
    boolean haveAuthorization = false;
    //    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static final String newLine = System.getProperty("line.separator");
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static TdApi.AuthorizationState authorizationState = null;
    private static volatile boolean needQuit = false;
    private static volatile boolean canQuit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TdApi.GetAuthorizationState AuthState = new TdApi.GetAuthorizationState();
        client = TDLibManager.getClient(this);
        client.send(AuthState, this);
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

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {

    }

    private void onAuthStateUpdated(TdApi.AuthorizationState authorizationState) {
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                Log.d("onResult-->", "Closed");
                if (!needQuit) {
                    client = Client.create(this, null, null); // recreate client after previous has closed
                } else {
                    canQuit = true;
                }
                break;
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
                Log.d("Main", "Gọi ListConversationsActivity: ");
                Intent conversationIntent = new Intent(MainActivity.this, ListConversationsActivity.class);
                startActivity(conversationIntent);
                finish();
                break;
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                Intent RegistrationIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(RegistrationIntent);
                finish();
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
}
