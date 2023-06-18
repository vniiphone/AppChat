package com.org.chatapp.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.TreeSet;

public class ListConversationsActivity extends AppCompatActivity implements TDLibManager.Callback {
    public static Client client;
    public final String TAG = "ListConversationsActivity";
    RecyclerView recyclerView_conversation_listChat;
    public ArrayList<TdApi.Chat> chatList = new ArrayList<>();
    ConversationAdapter conversationAdapter;
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static boolean haveFullMainChatList = false;
    private static final String newLine = System.getProperty("line.separator");
    public int countChats = 0;
    public TextView txt_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_conversations);
        AnhXa();
        Log.d("onCreate", "1. Khởi tạo client");
        client = TDLibManager.getClient(this);
//        getMainChatList(0);
        TdApi.GetChats chats = new TdApi.GetChats();
        chats.limit = 1000;

        haveFullMainChatList = false;

//        client.send(chats, this::onResult);

        Log.d(TAG, "Trước khi GetChat Tại OnCreate " + chatList.size());
        getChats();
        synchronized (chatList) {
            for (TdApi.Chat chat : chatList) {
                Log.d(TAG, "Chat: " + chat.title + " lastMessage: " + chat.title);
            }
        }
        recyclerView_conversation_listChat.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                int position = rv.getChildAdapterPosition(childView);
                if (childView != null && e.getAction() == MotionEvent.ACTION_UP) {
                    Intent chatIntent = new Intent(ListConversationsActivity.this, ConversationActivity.class);
                    Log.d("ChatID_onClick", "" + chatList.get(position).id);
                    chatIntent.putExtra("ChatID", chatList.get(position).id);
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

      /*  Log.d("onCreate", "2. Khởi tạo GetChats");
        TdApi.LoadChats loadChats = new TdApi.LoadChats();
        Log.d("onCreate", "3. Khởi tạo ChatList");
       TdApi.ChatList chatList1 = new TdApi.ChatListMain();
        client.send(new TdApi.LoadChats(chatList1, 100), this, null);
      Log.d("onCreate", "4. Yêu cầu loadChats: "+loadChats.chatList);
       client.send(new TdApi.GetChats(getChats, 100),this,null);
        Log.d("onCreate", "5. Yêu cầu  client \nĐộ dài: " + chatList1);
*/

    }

    private void AnhXa() {
        txt_count = findViewById(R.id.txt_countNumber);
        recyclerView_conversation_listChat = (RecyclerView) findViewById(R.id.recyclerview_conversation);
        recyclerView_conversation_listChat.setLayoutManager(new LinearLayoutManager(this));
        conversationAdapter = new ConversationAdapter(chatList);
        recyclerView_conversation_listChat.setAdapter(conversationAdapter);
    }


    public ArrayList<TdApi.Chat> getChats() {
        Log.d(TAG, "getChats: " + chatList.size());
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
                        Log.d(TAG, "Error.CONSTRUCTOR: " + object);
                        break;
                    case TdApi.Chats.CONSTRUCTOR:
                        long chatIDs[] = ((TdApi.Chats) object).chatIds;
                        Log.d(TAG, "Chats.CONSTRUCTOR: " + object + " Chat size: "+ Arrays.stream(chatIDs).count());
                        txt_count.setText("4");
                        for (long chatID : chatIDs) {
                            Log.d(TAG, "onResult: " + chatID);
                            client.send(new TdApi.GetChat(chatID), this, null);
                        }
                        Log.d(TAG, "onResult: " + chatIDs.toString());
                        break;
                    case TdApi.Chat.CONSTRUCTOR:
                        Log.d(TAG, "Chat.CONSTRUCTOR: " + object);
                        TdApi.Chat myChat = ((TdApi.Chat) object);
                        chatList.add(myChat);
                        Log.d(TAG, "Chat.CONSTRUCTOR: " + chatList.size());
                        countChats = chatList.size();
                        break;
                }
            }
        });
        return chatList;
    }

    private static void getMainChatList(final int limit) {
        synchronized (mainChatList) {
          /*  Log.d("getMainChatList", "3.1. synchronized (mainChatList) \n");
            TdApi.ChatList chatListMain = new TdApi.ChatListMain();
            TdApi.ChatList chatListArchive = new TdApi.ChatListArchive();

            //LoadChats
            TdApi.LoadChats loadChats1 = new TdApi.LoadChats(chatListMain, limit - mainChatList.size());
            client.send(loadChats1, object -> {
                Log.d("loadChats1", "ChatListMain: \n" + chatListMain);
//                txt_count.setText("Trò chuyện: " + chatListMain);
            }, null);

*/
            //GetChats
//            TdApi.Chats chats[] = new TdApi.Chats[100];


//            TdApi.Chat
/*
            //Send LoadChat request
            client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    switch (object.getConstructor()) {
                        case TdApi.Error.CONSTRUCTOR:
                            if (((TdApi.Error) object).code == 404) {
                                synchronized (mainChatList) {
                                    Log.d("getMainChatList", "3.2. code == 404) \n");
                                    haveFullMainChatList = true;
                                    txt_count.setText("Trò chuyện: " + mainChatList.size());
                                }
                            } else {
                                System.err.println("Receive an error for LoadChats:" + newLine + object);
                            }
                            break;
                        case TdApi.Ok.CONSTRUCTOR:
                            // chats had already been received through updates, let's retry request
                            getMainChatList(limit);
                            Log.d("getMainChatList", "3.3. chats had already been received through updates, let's retry request");
                            break;
                        default:
                            System.err.println("Receive wrong response from TDLib:" + newLine + object);
                    }
                }
            });*/
        }
    }

    @Override
    public void onSetTdlibParametersSuccess() {
        Log.d("onSetTdlibParametersSuccess", "Success");
    }

    @Override
    public void onSetTdlibParametersError() {
        Log.d("onSetTdlibParametersError", "Error");
    }

    @Override
    public void onResult(TdApi.Object object) {
        Log.d("onResult", "onResult\n");
        /*if (object instanceof TdApi.Chats) {
            TdApi.Chats chats = (TdApi.Chats) object;
            Log.d("onResul","21. if (object instanceof TdApi.Chats)");
            long[] chatIds = chats.chatIds;
            for (long chatId : chatIds) {
                Log.d("onResul","22. Chats): "+chatId);
                TdApi.GetChat getChat = new TdApi.GetChat(chatId);
                client.send(getChat, this, null);

            }
        } else if (object instanceof TdApi.Chat) {
            TdApi.Chat chat = (TdApi.Chat) object;
            Log.d("onResul","23. else if (object instanceof TdApi.Chat) ");
            chatList.add(chat);
            if (chat.photo != null && chat.photo.small != null) {
                TdApi.File smallPhoto = chat.photo.small;
                int priority = 1;
                int offset = 0;
                int limit = 0;
                boolean synchronous = false;
                TdApi.DownloadFile downloadFile = new TdApi.DownloadFile(smallPhoto.id, priority, offset, limit, synchronous);
                client.send(downloadFile, object1 -> {
                    if (object1 instanceof TdApi.File) {
                        TdApi.File downloadedFile = (TdApi.File) object1;
                        String filePath = downloadedFile.local.path;
                        if (filePath != null && !filePath.isEmpty()) {
                            File imgFile = new File(filePath);
                            if (imgFile.exists()) {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                runOnUiThread(() -> conversationAdapter.refresh());
                            }
                        }
                    }
                }, null);
            } else {
                runOnUiThread(() -> conversationAdapter.refresh());
            }
        }*/
    }


    public class ExceptionHandler implements Client.ExceptionHandler {
        @Override
        public void onException(Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.close();
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

class ConversationAdapter extends RecyclerView.Adapter<ConversationViewHolder> {
    private final String TAG = "ConversationAdapter";
    private ArrayList<TdApi.Chat> chatList;
    private Client client;

    ConversationAdapter(ArrayList<TdApi.Chat> chatList) {
        this.chatList = chatList;
        this.client = client; // Lưu trữ client
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       /* LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_conversation, parent, false);
        return new ConversationViewHolder(linearLayout);
*/
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        TdApi.Chat chat = chatList.get(position);
        holder.name.setText(chat.title);
        if (chat.photo != null && chat.photo.small != null) {
            TdApi.File smallPhoto = chat.photo.small;
            int priority = 1;
            int offset = 0;
            int limit = 0;
            boolean synchronous = false;
            TdApi.DownloadFile downloadFile = new TdApi.DownloadFile(smallPhoto.id, priority, offset, limit, synchronous);
            client.send(downloadFile, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (object instanceof TdApi.File) {
                        TdApi.File downloadedFile = (TdApi.File) object;
                        String filePath = downloadedFile.local.path;
                        if (filePath != null && !filePath.isEmpty()) {
                            File imgFile = new File(filePath);
                            if (imgFile.exists()) {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                // Gửi thông điệp cập nhật giao diện đến handler của activity

                            }
                        }
                    }
                }
            }, null);
        } else {
            holder.profile.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
    public void refresh() {
        notifyDataSetChanged();
    }
}

class ConversationViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    ImageView profile;

    public ConversationViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.textView_name);
        profile = itemView.findViewById(R.id.dp);
    }
}