package com.org.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.org.chatapp.Local.TokenStorage;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
/*

public class LoadActivity extends AppCompatActivity implements TDLibManager.Callback {
    public final String TAG = "ListConversationsActivity";
    RecyclerView recyclerView_conversation_listChat;
    //    public ArrayList<TdApi.Chat> chatListArray = new ArrayList<>();
//    private static final NavigableSet<LoadActivity.OrderedChat> mainChatList = new TreeSet<LoadActivity.OrderedChat>();
    private static boolean haveFullMainChatList = false;
    private static final String newLine = System.getProperty("line.separator");
    public int countChats;
    private TextView txt_count;

    public Client client;
    RecyclerView recyclerView_conversation;
    public ArrayList<TdApi.Chat> chatListArray = new ArrayList<>();
    public TdApi.ChatList chatList;
    ListChatsAdapter listChatsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_conversations);


        txt_count = findViewById(R.id.txt_countNumber);
        recyclerView_conversation_listChat = findViewById(R.id.recyclerview_conversation);
        recyclerView_conversation_listChat.setLayoutManager(new LinearLayoutManager(this));
        listChatsAdapter = new ListChatsAdapter(chatListArray);
        recyclerView_conversation_listChat.setAdapter(listChatsAdapter);

        client = TDLibManager.getClient(this);
//        client.send(new TdApi.GetChats( , 100), this);

        recyclerView_conversation_listChat.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                int position = rv.getChildAdapterPosition(childView);
                if (childView != null && e.getAction() == MotionEvent.ACTION_UP) {
                    Intent chatIntent = new Intent(LoadActivity.this, ConversationActivity.class);
                    Log.d("ChatID_onClick", "" + chatListArray.get(position).id);
                    chatIntent.putExtra("ChatID", chatListArray.get(position).id);
                    startActivity(chatIntent);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
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
        switch (object.getConstructor()) {
            case TdApi.Chats.CONSTRUCTOR:
                long chatIDs[] = ((TdApi.Chats) object).chatIds;
                for (long chatID : chatIDs) {
                    client.send(new TdApi.GetChat(chatID), LoadActivity.this, new ExceptionHandler());
                }
                runOnUiThread(() -> txt_count.setText(String.valueOf(chatIDs.length)));
                //Log.d("lol", "onResult: "+chatIDs.toString());
                break;
            case TdApi.Chat.CONSTRUCTOR:
                TdApi.Chat myChat = (TdApi.Chat) object;
                int fileId = myChat.photo.small.id; // Identifier của tệp cần tải xuống
                int priority = 1; // Ưu tiên của quá trình tải xuống (1-32)
                int offset = 0; // Vị trí bắt đầu từ vị trí nào trong tệp cần tải xuống
                int limit = 0; // Số byte cần tải xuống từ vị trí "offset" trước khi quá trình tải
                // xuống tự động bị hủy; sử dụng 0 để tải xuống mà không có giới hạn

                boolean synchronous = false; // Nếu là false, yêu cầu này trả về trạng thái tệp ngay
                // sau khi quá trình tải xuống đã bắt đầu. Nếu là true, yêu cầu này chỉ trả về trạng
                // thái tệp sau khi quá trình tải xuống thành công, thất bại, bị hủy hoặc một yêu cầu
                // downloadFile mới với các tham số offset/limit khác đã được gửi.
                client.send(new TdApi.DownloadFile(fileId, priority, offset, limit, synchronous), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        // Xử lý kết quả tải xuống tại đây
                    }
                }, null);
//                runOnUiThread(() -> listChatsAdapter.refresh());
                break;
            case TdApi.UpdateUser.CONSTRUCTOR:
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                TdApi.User user = updateUser.user;

        }
    }

    public class ExceptionHandler implements Client.ExceptionHandler {

        @Override
        public void onException(Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {

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

class LoadChatsAdapter extends RecyclerView.Adapter<ListChatsAdapter.ViewHolder> {
    @NonNull
    @Override
    public ListChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ListChatsAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
   */
/* ArrayList<TdApi.Chat> chatList;

    public LoadChatsAdapter(ArrayList<TdApi.Chat> chatList) {
        this.chatList = chatList;
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LoadChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemview_chats, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ListChatsAdapter.ViewHolder holder, int position) {

        holder.txt_title.setText(chatList.get(position).title);
        if (chatList.get(position).photo != null) {
            File imgFile = new File(chatList.get(position).photo.small.local.path);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.img_avatar.setImageBitmap(myBitmap);
            }
        } else {
            holder.img_avatar.setImageResource(R.drawable.ic_launcher_background);
        }

       */

/* if (isChatListEmpty()) {
            // Hiển thị thông báo hoặc xử lý khi chatListArray rỗng
            Toast.makeText(holder.itemView.getContext(), "Không có cuộc trò chuyện.", Toast.LENGTH_SHORT).show();
        } else {
            // Tiếp tục xử lý như thông thường
            TdApi.Chat chat = chatList.get(position);
            long timestamp = chatList.get(position).lastMessage.date;

// Convert the timestamp to an Instant
            Instant instant = Instant.ofEpochSecond(timestamp);

// Convert the Instant to a LocalDateTime in the Vietnam time zone
            ZoneId vietnamTimeZone = ZoneId.of("Asia/Ho_Chi_Minh");
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, vietnamTimeZone);

// Format the LocalDateTime as a String
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedDateTime = localDateTime.format(formatter);

            holder.txt_title.setText(chat.title);
            holder.txt_last_message_content
                    .setText(((TdApi.MessageText) chat.lastMessage.content).text.text);
            holder.txt_time.setText(formattedDateTime);
//        holder.txt_title.setText((TdApi.Chat) mChatsList.get(position).title.toString());
        }
*//*
*/
/*

    }

    @Override
    public int getItemCount() {
        return *//*
*/
/*isChatListEmpty() ? 0 : *//*
*/
/*chatList.size();
    }

    private boolean isChatListEmpty() {
        return chatList == null || chatList.isEmpty();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_title, txt_time, txt_last_message_content;
        private ImageView img_avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_title = itemView.findViewById(R.id.txt_chats_title_item);
            txt_time = itemView.findViewById(R.id.txt_chats_time_item);
            txt_last_message_content = itemView.findViewById(R.id.txt_chats_last_message_content_item);
            img_avatar = itemView.findViewById(R.id.img_chats_avt_item);
        }
    }*/
/*

}
*/
