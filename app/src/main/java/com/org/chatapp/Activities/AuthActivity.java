package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import org.xml.sax.helpers.DefaultHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class AuthActivity extends AppCompatActivity implements TDLibManager.Callback {
    private static TdApi.AuthorizationState authorizationState = null;
    private static Client client = null;
    private Button btn_checkauth;
    private EditText edt_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        AnhXa();
// Khởi tạo TDLib client
        // client = Client.create(this, this, this);
        client = TDLibManager.getClient(this);
        btn_checkauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = edt_code.getText().toString();
                if (!code.equals("")) {
                    TdApi.CheckAuthenticationCode authCode = new TdApi.CheckAuthenticationCode(code);
                    //client = Client.create(AuthActivity.this,null,null);
                    client.send(authCode, AuthActivity.this);
                }
            }
        });

        Client.ResultHandler updateHandler = new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                System.out.println("Receive Update: " + object.toString());
            }
        };
        Client.ExceptionHandler updateExceptionHandler = new Client.ExceptionHandler() {
            @Override
            public void onException(Throwable e) {
                System.out.println("Receive ExceptionHandler: " + e.getMessage());
            }
        };

        Client.ExceptionHandler defaultExceptionHandler = new Client.ExceptionHandler() {
            @Override
            public void onException(Throwable e) {
                System.out.println("Receive defaultExceptionHandler: " + e.getMessage());
            }
        };

        Client client = Client.create(updateHandler, updateExceptionHandler, defaultExceptionHandler);
        // Kiểm tra xem đối tượng Client đã được tạo thành công hay không
        if (client != null) {
            System.out.println("Client được tạo thành công.");
            // Thực hiện các hoạt động khác với đối tượng Client
        } else {
            System.out.println("Không thể tạo Client.");
            // Xử lý trường hợp không thể tạo Client
        }

    }

    public void AnhXa() {
        edt_code = findViewById(R.id.authCode);
        btn_checkauth = findViewById(R.id.checkBtn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.close();
    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                switch (((TdApi.UpdateAuthorizationState) object).authorizationState.getConstructor()) {
                    case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                        Log.d("AuthActivity", "onResult: TDlibParams");
                        TdApi.TdlibParameters authStateRequest = new TdApi.TdlibParameters();
                        // Gửi yêu cầu khởi tạo TDLib parameters
                        TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                        parameters.apiId = 25729812;
                        parameters.apiHash = "8285aa99a082199b9884819998f28c94";
                        parameters.databaseDirectory = getFilesDir().getAbsolutePath();
                        parameters.useMessageDatabase = true;
                        parameters.useSecretChats = true;
                        parameters.systemLanguageCode = "en";
                        parameters.deviceModel = "Pixel";
                        authStateRequest.systemVersion = "12.0";
                        parameters.applicationVersion = "0.0.1";
                        authStateRequest.enableStorageOptimizer = true;
                        client.send(new TdApi.SetTdlibParameters(parameters), null);
                        break;
                    case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                        client.send(new TdApi.CheckDatabaseEncryptionKey(), this);
                        break;
                    case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                        Intent conversationIntent = new Intent(AuthActivity.this, MainActivity.class);
                        conversationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(conversationIntent);
                        //client.send(new TdApi.UpdateAuthorizationState(new TdApi.AuthorizationStateReady()),null,null);
                        //finish();
                }
            case TdApi.UpdateConnectionState.CONSTRUCTOR:
                switch (((TdApi.UpdateConnectionState) object).state.getConstructor()) {
                    case TdApi.ConnectionStateReady.CONSTRUCTOR:
                        Log.d("AuthActivity", "onResult: Successfully loginned! ");
                        Toast.makeText(AuthActivity.this, "Successfully loginned!", Toast.LENGTH_SHORT).show();
                        break;
                }
        }


        // Xử lý updateAuthorizationState
        TdApi.AuthorizationState authorizationState = ((TdApi.UpdateAuthorizationState) object).authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            Toast.makeText(this, "Authorite", Toast.LENGTH_SHORT).show();
            // User đã được authorize thành công
            // Tiến hành các yêu cầu thông thường
        } else if (authorizationState instanceof TdApi.AuthorizationStateWaitPhoneNumber) {
            // Yêu cầu nhập số điện thoại
            // Gửi yêu cầu nhập số điện thoại
            // client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), null);
        } else if (authorizationState instanceof TdApi.AuthorizationStateWaitCode) {
            // Yêu cầu nhập mã xác nhận
            // Gửi yêu cầu nhập mã xác nhận
            // client.send(new TdApi.CheckAuthenticationCode(code, null, null), null);
        }
        // Xử lý các trạng thái authorizationState khác tương tự
        else {
            // Xử lý các response khác từ TDLib client
        }


    }


    @Override
    public void onSetTdlibParametersSuccess() {

    }

    @Override
    public void onSetTdlibParametersError() {

    }
}
