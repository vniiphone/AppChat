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

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;


/*
 * SĐT Test: +84911479322
 * */
public class ConversationActivity extends AppCompatActivity implements TDLibManager.Callback {
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

        client = TDLibManager.getClient(this);

        Log.d("Client:", "Client khởi tạo:" + client.toString());

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
//                SearchPublicChat();
//                sendMessage();
                findSDT();
            }
        });
    }

    public void SearchPublicChat() {
        String phone = "+84707992695";
        String usrName = "huyriddle";
        String usrName2 = "dinhoai";

        TdApi.SearchPublicChat searchPublicChat = new TdApi.SearchPublicChat(usrName2);

        Log.d("SearchPublicChat", "Tìm chat theo +dinhoai: " + searchPublicChat.username);

        client.send(searchPublicChat, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.Chat) {
                    TdApi.Chat publicChat = (TdApi.Chat) object;
                    // Xử lý thông tin của cuộc trò chuyện công cộng tìm thấy ở đây
                    Log.d("publicChat", "publicChat: " + publicChat.toString());

                    //Gửi tin nhắn đến Id
                    // Tạo đối tượng SendMessage
                    TdApi.SendMessage sendMessage = new TdApi.SendMessage();
                    sendMessage.chatId = Long.parseLong(String.valueOf(publicChat.id));
                    sendMessage.inputMessageContent = new TdApi.InputMessageText();
                    ((TdApi.InputMessageText) sendMessage.inputMessageContent).text = new TdApi.FormattedText();
                    ((TdApi.InputMessageText) sendMessage.inputMessageContent).text.text = "Hello Anh, Em Vũ Nè";
                    ((TdApi.InputMessageText) sendMessage.inputMessageContent).text.entities = new TdApi.TextEntity[0];
                    sendMessage.replyToMessageId = 0;
                    // Gửi yêu cầu SendMessage đến TDLib
                    String strMes = sendMessage.toString();
                    Log.d(TAG, "strMes: " + strMes.toString());
                    client.send(sendMessage, object1 -> {
                        TdApi.Message sentMessage = (TdApi.Message) object;
                        Log.d(TAG, "Message sent: " + sentMessage.content.toString());
                        // Hiển thị tin nhắn đã gửi trong TextView
                        String message1 = "Sent message: " + sentMessage.content.toString();
                        appendMessageToTextView(message1);
                    });


                } else {

                    Log.d("publicChat", " không tìm thấy cuộc trò chuyện công cộng");
                    // Xử lý trường hợp không tìm thấy cuộc trò chuyện công cộng
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ConverstationActivity", "Converstation");
        Toast.makeText(this, "Converstation", Toast.LENGTH_SHORT).show();
    }

    public void searchSDT() {
        String phone = "+84707992695";
        String usrName = "huyriddle";

    }

    public void sendMessage() {
        String chatId = "1944687616"; // Thay thế YOUR_CHAT_ID bằng chatId tương ứng
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
            String strMes = sendMessage.toString();
            Log.d(TAG, "strMes: " + strMes.toString());
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

    public void findSDT() {
        TdApi.Contact contact[] = new TdApi.Contact[1];
//        contact.userId = Long.parseLong("1944687616");
//        contact[0].phoneNumber = "+84707992695";
//        Log.d("findSDT", "Tìm chat theo +84707992695: " + contact[0].phoneNumber.toString());
       //Tạo đối tượng ImportContact
        TdApi.Contact contact1 = new TdApi.Contact();
        contact1.phoneNumber = "+84707992695";
        contact[0] = contact1;


        TdApi.ImportContacts importContacts = new TdApi.ImportContacts(contact);
        client.send(importContacts, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                    Log.d("TdApi.ImportedContacts","Kết quả import: "+object);

            }
        });
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
                String newText = currentText + "\n" + message.toString();
//                textViewMessage.setText(newText);
                textViewMessage.setText(newText);
            }
        });

    }

    @Override
    public void onSetTdlibParametersSuccess() {
        Log.d("onSetTdlibParametersSuccess", " Success");
    }

    @Override
    public void onSetTdlibParametersError() {
        Log.d("onSetTdlibParametersError", " Error");
    }

    @Override
    public void onResult(TdApi.Object object) {
        Log.d("onResult", " onResult" + object.toString());
    }
}