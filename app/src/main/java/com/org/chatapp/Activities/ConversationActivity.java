package com.org.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/*
 * SĐT Test: +84911479322
 * */
public class ConversationActivity extends AppCompatActivity implements TDLibManager.Callback {
    public Client client;

    public ArrayList<TdApi.Chat> chatList = new ArrayList<>();
    private final static String TAG = "Converstation Activity";

    private static TdApi.AuthorizationState authorizationState = null;
    private static volatile boolean haveAuthorization = false;
    private static volatile boolean needQuit = false;
    private static volatile boolean canQuit = false;
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

    private static final boolean haveFullMainChatList = false;
    private final List<TdApi.Message> historyMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_conversation);

        //Lấy data từ itent trước đó là (ListConversation)
//        String id = "5547360308"; //
        long id = layChatIdFromPreviousIntent();

        client = TDLibManager.getClient(this);
        Log.d("onCreate-->", "ChatId from Previous Intent: " + id);
//        Log.d("getChatLichSu", "ID lastmessage: " + lastMessageId);
        layTinNhanChat(id);

        AnhXaId();

//        img_send.setOnClickListener(v -> sendMessage());
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
        Log.d("Nhận:", "Client nhận tin nhắn của chatID:" + id);
        img_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SearchPublicChat();
//                GetMainListChatUtil.getMainChatList(20,client);
                sendMessage();
//                findSDT();
            }
        });
    }

    public long layChatIdFromPreviousIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return 0;
        }
        String id = bundle.getString("chatIdFromPrevious", "Null");
        long lngId = Long.parseLong(id);
        Log.d("layChatIdFromPreviousIntent", "Chat ID: " + id);
        return lngId;
    }

   /* private long getLastMessage(long chatId) {
        getLichSuChat(chatId, 0);
        if (!historyMessages.isEmpty()) {
            TdApi.Message lastMessage = historyMessages.get(0);
            Log.d("getLastMessage", "From Msg ID: " + lastMessage);
            return lastMessage.id;
        } else {
            Log.d("getLastMessage", "From Msg ID: " + -1);
            return -1;
        }
    }*/

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

    }

    public void getLichSuChat(long chatId, long fromMessageId) {
        Log.d("getLichSuChat", "Gọi hàm --> size: " + historyMessages.size());
//        Log.d("getChatLichSu", "Get lịch sử tin nhắn chatId: " + chatId);
//        long strChatId = Long.parseLong(chatId);
        Log.d("getLichSuChatDebug", "LastMessage: " + fromMessageId);
        TdApi.GetChatHistory getChatHistory = new TdApi.GetChatHistory(chatId, fromMessageId, 0, 100, false);
        client.send(getChatHistory, this::onResult);
    }

    public void layTinNhanChat(long chatId) {
//        long strChatId = Long.parseLong(chatId);
        TdApi.GetChat getChat = new TdApi.GetChat(chatId);
        client.send(getChat, this::onResult);
    }

    public void returnBack(View view) {
        finish();
//        Intent intent = new Intent(ConversationActivity.this, ListConversationsActivity.class);
//        startActivity(intent);
    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.LogOut.CONSTRUCTOR:
                haveAuthorization = false;
                finish();
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                Log.d("onResult-->", "Logging out");
                finish();
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                Log.d("onResult-->", "Closing");
                finish();
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                Log.d("onResult-->", "Closed");
                if (!needQuit) {
                    client = Client.create(this, null, null); // recreate client after previous has closed
                } else {
                    canQuit = true;
                }
                break;
            case TdApi.Messages.CONSTRUCTOR:
                TdApi.Messages m = (TdApi.Messages) object;
                Collections.addAll(historyMessages, m.messages);
                Collections.reverse(historyMessages);
                Log.d("onResult->", "Messages.CONSTRUCTOR: Size after AddAll: " + historyMessages.size());
                runOnUiThread(() -> conversationChatAdapter.refresh());

            case TdApi.UpdateChatLastMessage.CONSTRUCTOR:
                TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) object;
                TdApi.Message message = updateNewMessage.message;
                Log.d("onResult->", "Size after update Chat message: " + historyMessages.size());
                runOnUiThread(() -> {
                    historyMessages.add(0, message);
                    conversationChatAdapter.notifyItemInserted(0);
                    // Scroll RecyclerView to the top to display the new message
                    recyclerViewConversation.scrollToPosition(0);
                });
            case TdApi.Chat.CONSTRUCTOR: {
                TdApi.Chat chat = (TdApi.Chat) object;
                Log.d("onResult->", "Chat.Lastmessage " + chat.lastMessage);
                getLichSuChat(chat.id, chat.lastMessage.id);
                runOnUiThread(() -> {
                    conversationChatAdapter.notifyItemInserted(0);
                    // Scroll RecyclerView to the top to display the new message
                    recyclerViewConversation.scrollToPosition(0);
                });
                if (chat != null) {
                    File imgFile = new File(chat.photo.small.local.path);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        img_avt.setImageBitmap(myBitmap);
                        tv_chat_title.setText(chat.title);
                    }
                } else {
                    img_avt.setImageResource(R.mipmap.ic_launcher_round);
                    tv_chat_title.setText("Chat Title");
                }
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + object.getConstructor());
        }
    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {

    }

    public void sendMessage() {
        String message = edt_message.getText().toString();
        long id = layChatIdFromPreviousIntent();
        if (!message.isEmpty()) {
            // Tạo đối tượng SendMessage
            TdApi.SendMessage sendMessage = new TdApi.SendMessage();
            sendMessage.chatId = id;
            sendMessage.inputMessageContent = new TdApi.InputMessageText();
            ((TdApi.InputMessageText) sendMessage.inputMessageContent).text = new TdApi.FormattedText();
            ((TdApi.InputMessageText) sendMessage.inputMessageContent).text.text = message;
            ((TdApi.InputMessageText) sendMessage.inputMessageContent).text.entities = new TdApi.TextEntity[0];
            sendMessage.replyToMessageId = 0;
            // Gửi yêu cầu SendMessage đến TDLib
            String strMes = sendMessage.toString();
            Log.d("sendMessage-->", "strMes: " + strMes);
            client.send(sendMessage, object -> {
                if (object instanceof TdApi.Message) {
                    // Tin nhắn đã được gửi thành công
                    TdApi.Message sentMessage = (TdApi.Message) object;
                    Log.d("sendMessage-->", "Message sent: " + sentMessage.content.toString());
                    // Hiển thị tin nhắn đã gửi trong TextView
                    String message1 = "Sent message: " + sentMessage.content.toString();
//                    appendMessageToTextView(message1);
                    historyMessages.add(sentMessage);
                    recyclerViewConversation.scrollToPosition(historyMessages.size() - 1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            conversationChatAdapter.refresh();
                        }
                    });
                } else {
                    // Xử lý các trạng thái, lỗi hoặc kết quả không mong đợi khác từ TDLib
                    Log.e("sendMessage-->", "Unexpected result: " + object);
                }
            });
        } else Toast.makeText(this, "Nhập tin nhắn", Toast.LENGTH_SHORT).show();
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

class ConversationChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<TdApi.Message> messageList;
    public static final int VIEW_TYPE_ME = 1;
    public static final int VIEW_TYPE_OTHER = 0;

    @Override
    public int getItemViewType(int position) {
        // Determine the view type based on position
        boolean isGoing = messageList.get(position).isOutgoing;
        if (isGoing) {
            return VIEW_TYPE_ME;  // Even positions are right items
        } else {
            return VIEW_TYPE_OTHER; // Odd positions are left items
        }
    }

    public ConversationChatAdapter(List<TdApi.Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_chat, parent, false);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_OTHER) {
            View rightView = inflater.inflate(R.layout.frm_receive_item_chat, parent, false);
            return new ImOtherViewHolder(rightView);
        } else {
            View leftView = inflater.inflate(R.layout.frm_send_item_chat, parent, false);
            return new ImUserViewHolder(leftView);
        }
    }

    public static ZonedDateTime convertDateTimeToGMT7(long dateTime) {
        Instant instant = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            instant = Instant.ofEpochMilli(dateTime);
        }
        ZoneId zoneId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            zoneId = ZoneId.of("GMT+7");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ZonedDateTime.ofInstant(instant, zoneId);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        long timestamp = messageList.get(position).date;

// Convert the timestamp to an Instant
        Instant instant = Instant.ofEpochSecond(timestamp);

// Convert the Instant to a LocalDateTime in the Vietnam time zone
        ZoneId vietnamTimeZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, vietnamTimeZone);

// Format the LocalDateTime as a String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedDateTime = localDateTime.format(formatter);

        if (messageList.get(position).content instanceof TdApi.MessageText) {
            int viewType = getItemViewType(position);
            if (viewType == VIEW_TYPE_ME) {
                ImUserViewHolder imUserViewHolder = (ImUserViewHolder) holder;
                imUserViewHolder.msg.setText(((TdApi.MessageText) messageList.get(position).content).text.text);
                imUserViewHolder.time.setText(formattedDateTime);
            } else {
                ImOtherViewHolder imOtherViewHolder = (ImOtherViewHolder) holder;
                imOtherViewHolder.msg.setText(((TdApi.MessageText) messageList.get(position).content).text.text);
                imOtherViewHolder.time.setText(formattedDateTime);
            }
        }
    }

    public class ImOtherViewHolder extends RecyclerView.ViewHolder {
        public TextView msg, time;

        public ImOtherViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.txt_display_message_receive);
            time = (TextView) itemView.findViewById(R.id.txt_display_time_message_receive);
        }
    }

    public class ImUserViewHolder extends RecyclerView.ViewHolder {
        public TextView msg, time;

        public ImUserViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.txt_display_message_send);
            time = (TextView) itemView.findViewById(R.id.txt_display_time_message_send);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void refresh() {
        notifyDataSetChanged();
    }


}