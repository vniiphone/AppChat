package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.org.chatapp.Local.TokenStorage;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

public class LoadActivity extends AppCompatActivity  implements TDLibManager.Callback {
    private TokenStorage tokenStorage;
    private static final String TAG = "MainActivity";
    private Button btn_check;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);


    }

    private void authenticateWithTelegram(String accessToken) {
        // Thực hiện xác thực với server Telegram bằng mã thông báo truy cập
        // TODO: Triển khai xác thực với TDLib 1.8.0
    }

    private void redirectToLoginScreen() {
        // Chuyển đến giao diện đăng nhập
        Intent intent = new Intent(LoadActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResult(TdApi.Object object) {

    }

    @Override
    public void onSetTdlibParametersSuccess() {
        // Xử lý thành công SetTdlibParameters
        // Chuyển đến trang đăng nhập
        goToLoginActivity();
    }
    private void goToLoginActivity() {
        // Code để chuyển đến trang đăng nhập
        // Ví dụ:
        Intent loginIntent = new Intent(LoadActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public void onSetTdlibParametersError() {
        // Xử lý lỗi SetTdlibParameters
        Log.d("LoadActivity", "Lỗi SetTdlibParameters");
    }
}