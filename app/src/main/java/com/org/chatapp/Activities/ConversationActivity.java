package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.org.chatapp.Adapters.ConversationAdapter;
import com.org.chatapp.R;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

public class ConversationActivity extends AppCompatActivity {
    public Client client;
    RecyclerView recyclerView_conversation;
    public ArrayList<TdApi.Chat> chatList = new ArrayList<>();
    ConversationAdapter conversationAdapter;
    private final static String TAG = "Converstation Activity";
    private TextView textViewMessage;
    private EditText editTextInput;
    private Button buttonSend;
    private static boolean haveFullMainChatList = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        textViewMessage = findViewById(R.id.text_view_message);
        editTextInput = findViewById(R.id.edit_text_input);
        buttonSend = findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ConverstationActivity", "Converstation");
        Toast.makeText(this, "Converstation", Toast.LENGTH_SHORT).show();
    }

    public void sendMessage() {
        String chatId = "12022001"; // Thay thế YOUR_CHAT_ID bằng chatId tương ứng
        String message = editTextInput.getText().toString().trim();
        if (!message.isEmpty()) {
            // Tạo đối tượng SendMessage
            TdApi.SendMessage sendMessage = new TdApi.SendMessage();
            sendMessage.chatId = Long.parseLong(chatId);
            sendMessage.inputMessageContent = new TdApi.InputMessageText();
            ((TdApi.InputMessageText) sendMessage.inputMessageContent).text = new TdApi.FormattedText();
            ((TdApi.InputMessageText) sendMessage.inputMessageContent).text.text = message;
            ((TdApi.InputMessageText) sendMessage.inputMessageContent).text.entities = new TdApi.TextEntity[0];
            sendMessage.replyToMessageId = 0;

            // Gửi yêu cầu SendMessage đến TDLib
            client.send(sendMessage, object -> {
                if (object instanceof TdApi.Message) {
                    // Tin nhắn đã được gửi thành công
                    TdApi.Message sentMessage = (TdApi.Message) object;
                    Log.d(TAG, "Message sent: " + sentMessage.content.toString());
                    // Hiển thị tin nhắn đã gửi trong TextView
                    String message1 = "Sent message: " + sentMessage.content.toString();
                    appendMessageToTextView(message1);
                } else {
                    // Xử lý các trạng thái, lỗi hoặc kết quả không mong đợi khác từ TDLib
                    Log.e(TAG, "Unexpected result: " + object);
                }
            });
        }
        editTextInput.setText(""); // Xóa nội dung văn bản đã nhập
    }

    public void receiveMessage(String chatId, TdApi.Object object) {
        long chatIDs[] = ((TdApi.Chats) object).chatIds;
        for (long chatID : chatIDs) {
            client.send(new TdApi.GetChat(chatID), new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (object instanceof TdApi.Chat) {
                        TdApi.Chat chat = (TdApi.Chat) object;
                        String chatTitle = chat.title;
                        Log.d(TAG, "Received chat: " + chatTitle);
                        // Xử lý và hiển thị tin nhắn trong TextView
                        String message = "Received message from " + chatTitle;
                        appendMessageToTextView(message);
                    } else {
                        Log.e(TAG, "Unexpected result: " + object);
                    }
                }
            }, new Client.ExceptionHandler() {
                @Override
                public void onException(Throwable e) {
                    // Xử lý ngoại lệ (nếu có)
                }
            });
        }
    }

    private void appendMessageToTextView(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentText = textViewMessage.getText().toString();
                String newText = currentText + "\n" + message;
                textViewMessage.setText(newText);
            }
        });

    }
}