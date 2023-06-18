package com.org.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*
 * SĐT Test: +84911479322
 * */
public class ConversationActivity extends AppCompatActivity implements TDLibManager.Callback {
    public Client client;
    List<TdApi.Chat> listChat;

    public ArrayList<TdApi.Chat> chatList = new ArrayList<>();
    private final static String TAG = "Converstation Activity";

    //Khai báo các thành phần trong UI

    //Top_bar
    private ConstraintLayout lout_bartop;
    private ImageView img_back, img_avt;
    private TextView tv_chat_title, tv_status;

    //Recycle View - Conversation Chat
    private RecyclerView recyclerViewConversation;
    ConversationChatAdapter conversationChatAdapter;

    //Input Layout
    private LinearLayout layout_inputMess;
    private EditText edt_message;
    private ImageView img_send;

    //Cac bien can thiet
    private static final boolean haveFullMainChatList = false;
    private final String chatId = "5547360308"; // Thay thế YOUR_CHAT_ID bằng chatId tương ứng
    private List<TdApi.Message> historyMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_conversation);

        //Lấy data từ itent trước đó là (ListConversation)
        long id = getIntent().getLongExtra("AppChatID", 192);
        client = TDLibManager.getClient(this);
      /*
//        getTitle(chatId);
        long lastMessageId = getLastMessage(chatId);*/
//        Log.d("getChatLichSu", "ID lastmessage: " + lastMessageId);
        getLichSuChat(chatId, 46137344);
        //GetLastMessage from_id= 0
        /*long lastMessageId = getLastMessage(chatId); // trả về lastmessage ID
//        if (lastMessageId != -1) {
            getLichSuChat(chatId, lastMessageId);
        } else {
            Toast.makeText(this, "Ngược lại", Toast.LENGTH_SHORT).show();
        }*/

        AnhXaId();
        findSDT();
        SearchPublicChat();
        img_send.setOnClickListener(v -> sendMessage());
       /* client.send(new TdApi.GetChatHistory(
                        chatIddLong,
                        0,
                        0,
                        20,
                        false),
                this,
                null);
*/
        Log.d("Client:", "Client khởi tạo:" + client.toString());
        Log.d("Nhận:", "Client nhận tin nhắn của chatID:" + chatId);
        // getChatHistoryBindingToListView(chatId, 0);

        //Phương thức
//        getChatLichSu(chatId, 0);

       /* img_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SearchPublicChat();
//                GetMainListChatUtil.getMainChatList(20,client);
                sendMessage();
//                findSDT();
            }
        });*/
    }

    private long getLastMessage(String chatId) {
        getLichSuChat(chatId, 0);
//        if (!historyMessages.isEmpty()) {
        TdApi.Message lastMessage = historyMessages.get(0);
        return lastMessage.id;
//        }
//        return -1;
    }

    public void AnhXaId() {
        //Top_bar: 5 thanh phan
        lout_bartop = findViewById(R.id.layout_bar_top);
        img_back = findViewById(R.id.img_back);
        img_avt = findViewById(R.id.img_user_avt);
        tv_chat_title = findViewById(R.id.txt_chat_tit);
        tv_status = findViewById(R.id.txt_status);

        //Recycle View - Conversation Chat
        recyclerViewConversation = findViewById(R.id.rcl_conversations_chat);
        recyclerViewConversation.setLayoutManager(new LinearLayoutManager(this));
        conversationChatAdapter = new ConversationChatAdapter(historyMessages);
        recyclerViewConversation.setAdapter(conversationChatAdapter);

        //Input Layout
        layout_inputMess = findViewById(R.id.layout_inputMessage);
        edt_message = findViewById(R.id.edt_message);
        img_send = findViewById(R.id.img_send_message);
        img_send.setClickable(true);

        /*LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        layout_inputMess.setLayoutParams(params);*/

    }


    public void getTitle(String chatId) {
        //Truyền ChatId(long) vào đối tượng(function) getChat để khởi tạo yêu cầu GetChat theo ChatId
        long lngChatid = Long.parseLong(chatId);
        TdApi.GetChat getChat = new TdApi.GetChat(lngChatid);
        client.send(getChat, object -> {
            if (object instanceof TdApi.Chats) {
                TdApi.Chat chat1 = (TdApi.Chat) object;
                tv_chat_title.setText(chat1.title);
                Log.d(TAG, "Chat tile: " + chat1.title);
                return;
            }
        });
    }

    public void getLichSuChat(String chatId, long fromMessageId) {
        Log.d("getChatLichSu", "Gọi hàm --> size: " + historyMessages.size());
        // ...
        //khởi tạo các biến cho phương thức getChatHistory()
        int limit = 100; // Maximum number of messages to be returned
        long chatIdd = Long.parseLong(chatId);
//        Log.d("getChatLichSu", "Get lịch sử tin nhắn chatId: " + chatId);
        TdApi.GetChatHistory getChatHistory = new TdApi.GetChatHistory(chatIdd, fromMessageId, 0, limit, false);

        client.send(getChatHistory, this, null);

    }

    @Override
    public void onResult(TdApi.Object object) {
  /*      Log.d("onResult", "Show tin nhắn");
        getChatLichSu(chatId, 0);
        if (object instanceof TdApi.UpdateNewMessage) {
            TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) object;
            TdApi.Message message = updateNewMessage.message;
            Log.d(TAG, "Received new message: " + message.content.toString());
            String messageText = "Received message: " + message.content.toString();
//            appendMessageToTextView(messageText);
            runOnUiThread(() -> {
                // Add the new message to the top of the list
//                appendMessageToTextView2(messageText);
                messageList.add(0, message);
                conversationChatAdapter.notifyItemInserted(0);
                // Scroll RecyclerView to the top to display the new message
                recyclerViewConversation.scrollToPosition(0);
            });
        } else {
            Log.e(TAG, "Unexpected result: " + object);
        }*/
        Log.d("getChatLichSu", "Size: " + historyMessages.size());
        switch (object.getConstructor()) {
            case TdApi.Messages.CONSTRUCTOR:
                TdApi.Messages m = (TdApi.Messages) object;
                Collections.addAll(historyMessages, m.messages);
                Collections.reverse(historyMessages);
                Log.d("getChatLichSu", "Size after AddAll: " + historyMessages.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        conversationChatAdapter.refresh();
                    }
                });

            case TdApi.UpdateChatLastMessage.CONSTRUCTOR:
                TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) object;
                TdApi.Message message = updateNewMessage.message;
                Log.d("getChatLichSu", "Size after update Chat message: " + historyMessages.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        historyMessages.add(0, message);
                        conversationChatAdapter.notifyItemInserted(0);
                        // Scroll RecyclerView to the top to display the new message
                        recyclerViewConversation.scrollToPosition(0);
                    }
                });


        }
    }

    public void sendMessage() {
        String message = edt_message.getText().toString();
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
            Log.d(TAG, "strMes: " + strMes);
            client.send(sendMessage, object -> {
                if (object instanceof TdApi.Message) {
                    // Tin nhắn đã được gửi thành công
                    TdApi.Message sentMessage = (TdApi.Message) object;
                    Log.d(TAG, "Message sent: " + sentMessage.content.toString());
                    // Hiển thị tin nhắn đã gửi trong TextView
                    String message1 = "Sent message: " + sentMessage.content.toString();
//                    appendMessageToTextView(message1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            conversationChatAdapter.refresh();
                        }
                    });
                } else {
                    // Xử lý các trạng thái, lỗi hoặc kết quả không mong đợi khác từ TDLib
                    Log.e(TAG, "Unexpected result: " + object);
                }
            });
        } else
            Toast.makeText(this, "Nhập tin nhắn", Toast.LENGTH_SHORT).show();
        edt_message.setText(""); // Xóa nội dung văn bản đã nhập
    }

    public void SearchPublicChat() {
        String phone = "+84707992695";
        String usrName = "huyriddle";
        String usrName2 = "dinhoai";
        TdApi.SearchPublicChat searchPublicChat = new TdApi.SearchPublicChat(usrName);
        Log.d("SearchPublicChat", "Tìm chat theo +usrName: " + searchPublicChat.username);

        client.send(searchPublicChat, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.Chat) {
                    TdApi.Chat publicChat = (TdApi.Chat) object;
                    // Xử lý thông tin của cuộc trò chuyện công cộng tìm thấy ở đây
                    Log.d("publicChat", "publicChat: " + publicChat);
/*
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


                    });*/
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
    }

    private void populateListView(List<TdApi.Chat> body) {
        ConversationAdapter conversationAdapter = new ConversationAdapter((ArrayList<TdApi.Chat>) body);
        recyclerViewConversation.setAdapter(conversationAdapter);
    }


    public void findSDT() {
        TdApi.Contact[] contact = new TdApi.Contact[1];
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
                Log.d("TdApi.ImportedContacts", "Kết quả import: " + object);
            }
        });
    }

    public void receiveMessage(String chatId, TdApi.Object object) {
        long[] chatIDs = ((TdApi.Chats) object).chatIds;
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
//                        appendMessageToTextView(message);
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

    //Get Lịch sử Chat và get Title Chat

    //Phân biệt MessageSender với MessageReceive

    @Override
    public void onSetTdlibParametersSuccess() {
        Log.d("onSetTdlibParametersSuccess", " Success");
    }

    @Override
    public void onSetTdlibParametersError() {
        Log.d("onSetTdlibParametersError", " Error");
    }
}

class ConversationChatAdapter extends RecyclerView.Adapter<ConversationChatAdapter.ConversationChatHolder> {
    private List<TdApi.Message> messageList;

    ConversationChatAdapter(List<TdApi.Message> messageList) {
        this.messageList = messageList;
    }


    @NonNull
    @Override
    public ConversationChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_chat, parent, false);
        return new ConversationChatHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationChatHolder holder, int position) {

        // Get the message's date
        long date = messageList.get(position).date;

// Format the date
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = format.format(date);
        Log.d("onBindViewHolder", "Content: " + messageList.get(position).content + " Time: " + formattedDate);
// Set the text of the TextView

        // Set the text and layout for TextView
        // Set the text and layout for TextView
        if (messageList.get(position).content instanceof TdApi.MessageText) {
            try {
                if (messageList.get(position).isOutgoing) { // Tin nhắn gửi đi
                    holder.linearLayout.setBackgroundResource(R.drawable.sender_message_bg);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.addRule(RelativeLayout.ALIGN_PARENT_END); // Đặt lề phải
                    holder.linearLayout.setLayoutParams(params);
                } else { // Tin nhắn nhận được
                    holder.linearLayout.setBackgroundResource(R.drawable.receiver_message_bg);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.addRule(RelativeLayout.ALIGN_PARENT_START); // Đặt lề trái
                    holder.linearLayout.setLayoutParams(params);
                }
                holder.tvMessage.setText(((TdApi.MessageText) messageList.get(position).content).text.text);
                holder.tvTime.setText(formattedDate);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void refresh() {
        notifyDataSetChanged();
    }


    static class ConversationChatHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView tvMessage, tvTime;


        ConversationChatHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            linearLayout = itemView;
            tvMessage = linearLayout.findViewById(R.id.txt_message_item);
            tvTime = linearLayout.findViewById(R.id.txt_time_message);
        }
    }
}

/*
class ConversationChatHolder extends RecyclerView.ViewHolder {

    //khai báo layout của item of RecycleView
    LinearLayout layout;
    TextView tvMessage, tvTime;

    public ConversationChatHolder(@NonNull View itemView) {
        super(itemView);
        layout = (LinearLayout) itemView;
        tvMessage = layout.findViewById(R.id.txt_message_item);
        tvTime = layout.findViewById(R.id.txt_time_message);
    }
}*/
