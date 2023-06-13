package com.org.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
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

public class ListConversationsActivity extends AppCompatActivity implements TDLibManager.Callback{
    public Client client;
    RecyclerView recyclerView_conversation;
    public ArrayList<TdApi.Chat> chatList = new ArrayList<>();
    ConversationAdapter conversationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_conversations);

        recyclerView_conversation = (RecyclerView) findViewById(R.id.recyclerview_conversation);
        recyclerView_conversation.setLayoutManager(new LinearLayoutManager(this));
        conversationAdapter = new ConversationAdapter(chatList);
        recyclerView_conversation.setAdapter(conversationAdapter);
        client = TDLibManager.getClient(this);
//        client.send(new TdApi.GetChats(Long.MAX_VALUE,0,1000),this,null);
    }

    @Override
    public void onSetTdlibParametersSuccess() {

    }

    @Override
    public void onSetTdlibParametersError() {

    }

    @Override
    public void onResult(TdApi.Object object) {

    }

}

class ConversationAdapter extends RecyclerView.Adapter<ConversationViewHolder>{
    ArrayList<TdApi.Chat> chatList;

    ConversationAdapter(ArrayList<TdApi.Chat> chatList){
        this.chatList = chatList;
    }
    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_conversation,parent,false);
        return new ConversationViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.name.setText(chatList.get(position).title);
        if (chatList.get(position).photo != null) {
            File imgFile = new File(chatList.get(position).photo.small.local.path);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.profile.setImageBitmap(myBitmap);

            }

        }else {
            holder.profile.setImageResource(R.drawable.ic_launcher_background);
        }
        /*holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
class ConversationViewHolder extends RecyclerView.ViewHolder{
    LinearLayout layout;
    TextView name;
    ImageView profile;
    public ConversationViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = (LinearLayout) itemView;
    }
}