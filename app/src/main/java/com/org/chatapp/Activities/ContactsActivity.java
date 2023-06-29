package com.org.chatapp.Activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements TDLibManager.Callback {
    RecyclerView rcl_contacts;
    public static ArrayList<TdApi.User> contactListArray;
    public static Client client;

    ContactsAdapter contactsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        client = TDLibManager.getClient(this);
        contactListArray = new ArrayList<>();

        danhBaUsers();
        anhXa();

    }

    public void danhBaUsers() {
        Log.d("onResult-->", "khởi tạo danhBaUsers()");
        TdApi.Function function = new TdApi.GetContacts();
        client.send(function, this::onResult);
    }

    private void anhXa() {

//        rcl_newgroup_contacts.setHasFixedSize(true);
        rcl_contacts = findViewById(R.id.recyclerview_contact);
        rcl_contacts.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new ContactsAdapter(contactListArray, this);
        rcl_contacts.setAdapter(contactsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public void onSetTdlibParametersSuccess() {

    }

    @Override
    public void onSetTdlibParametersError() {

    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                Toast.makeText(this, "Lỗi cmnr", Toast.LENGTH_SHORT).show();
                Log.d("onResult-->", "Error: " + object);
                break;
            case TdApi.Users.CONSTRUCTOR:
                long userIds[] = ((TdApi.Users) object).userIds;
                Log.d("onResult-->", "Users.Contructor: " + object + " Users[] size: " + Arrays.stream(userIds).count());
                for (long userId : userIds) {
                    Log.d("onResult-->", "userID in Users.Contructor: " + userId);
                    client.send(new TdApi.GetUser(userId), this::onResult);
                }
                Log.d("onResult-->", "Users.CONSTRUCTOR: " + userIds.toString());
                break;
            case TdApi.File.CONSTRUCTOR:
                Log.d("onResult-->", "File: " + object);
                break;
            case TdApi.User.CONSTRUCTOR:
                TdApi.User user = ((TdApi.User) object);
                client.send(new TdApi.DownloadFile(user.profilePhoto.small.id, 1, 0, 0, false), this::onResult);
                runOnUiThread(() -> contactsAdapter.refresh());
                Log.d("onResult-->", "User.CONSTRUCTOR: " + user.username);
                contactListArray.add(user);
                runOnUiThread(() -> contactsAdapter.refresh());
                Log.d("onResult-->", "User.CONSTRUCTOR: " + contactListArray.size());
                break;

            default:
                Log.d("onResult-->", "default: " + object);
        }
    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {

    }
}


class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<TdApi.User> userList;
    private Context context;

    public ContactsAdapter(List<TdApi.User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemview_contacts, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TdApi.User user = userList.get(position);

        if (user instanceof TdApi.User) {
            holder.txt_name.setText(user.firstName + " " + user.lastName);
            if (user.phoneNumber.length() != 0) {
                holder.txt_phone.setText("+" + user.phoneNumber);
            } else {
                holder.txt_phone.setText(" ");
            }
            if (userList.get(position).profilePhoto != null) {
                File imgFile = new File(userList.get(position).profilePhoto.small.local.path);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    holder.img_avatar.setImageBitmap(myBitmap);
                }
            } else {
                holder.img_avatar.setImageResource(R.drawable.unknowavt);
            }
        } else {
            holder.txt_name.setText("Unknown");
            holder.img_avatar.setImageResource(R.drawable.unknowavt);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_name, txt_phone;
        private ImageView img_avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_name = itemView.findViewById(R.id.txt_name_contact_item);
            txt_phone = itemView.findViewById(R.id.txt_sdt_contact_item);
            img_avatar = itemView.findViewById(R.id.img_contacts);
        }
    }
}
