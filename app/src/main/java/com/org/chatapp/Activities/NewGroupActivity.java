package com.org.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewGroupActivity extends AppCompatActivity implements TDLibManager.Callback, CheckBoxListener {
    public static Client client;
    private final static String TAG = "Create Basic Group Activity";
    private final List<TdApi.Message> historyMessages = new ArrayList<>();
    public static ArrayList<TdApi.User> contactListArray;
    private String tieude;
    //Khoi tao UI
    Button btnCreate, btnBack;
    TextInputLayout layout_text_input_group_name;
    TextInputEditText edt_input_name_group;
    RecyclerView rcl_newgroup_contacts;
    NewGroupAdapter newGroupAdapter;
    private List<TdApi.User> selectedUsers = new ArrayList<>();
    private long danhSachIDusers[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        client = TDLibManager.getClient(this);
        Log.d("onCreate-->onResult-->", "onCreateGroup ");

        contactListArray = new ArrayList<>();

        danhBaUsers();
        anhXa();

        btnBack.setOnClickListener(v -> {
            Toast.makeText(this, "Đóng", Toast.LENGTH_SHORT).show();
            finish();
        });
        btnCreate.setOnClickListener(v -> {


           String strNameGroup = edt_input_name_group.getText().toString().trim();
            if (strNameGroup == " ") {
                edt_input_name_group.setError("Không được trống");
                edt_input_name_group.requestFocus();
                Toast.makeText(this, "Bạn cần nhập tên group", Toast.LENGTH_SHORT).show();
            } else {
                edt_input_name_group.setError(null);
                // Kiểm tra xem danh sách selectedUsers có dữ liệu hay không
                if (selectedUsers.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn ít nhất một người dùng", Toast.LENGTH_SHORT).show();
                } else {
                    danhSachIDusers = new long[selectedUsers.size()];
                    // Gọi phương thức tạo nhóm và truyền danh sách selectedUsers vào
                    //Duuyệt qua mảng selectedUSer để thêm từng phần tử vào trong danh sách userID
                    for (int i = 0; i < selectedUsers.size(); i++) {
                        danhSachIDusers[i] = selectedUsers.get(i).id;
                    }
                    Log.d("onResult-->","Tên group: "+strNameGroup);
                    Log.d("onResult-->", "Độ dài danh sách UserId: " + danhSachIDusers.length + "User: " + danhSachIDusers.toString());
                    createBasicGroup(danhSachIDusers, strNameGroup);
                }
            }

        });
    }

    private void createGroup(List<TdApi.User> userList, String tittle) {
        // Thực hiện logic tạo nhóm với danh sách người dùng đã chọn
        // ...
        // Ví dụ: Toast ra tên người dùng trong danh sách
        Log.d("onResult-->", "Tạo Group: " + tittle + " UserList Từ CheckBox: " + userList);
//        TdApi.Function create = TdApi.CreateNewBasicGroupChat(userList, tittle);
      /*  for (TdApi.User user : userList) {
            String fullName = user.firstName + " " + user.lastName;
            Toast.makeText(this, "Người dùng: " + fullName, Toast.LENGTH_SHORT).show();
        }*/
    }

    public void anhXa() {
        edt_input_name_group = findViewById(R.id.edt_input_name_group);
        layout_text_input_group_name = findViewById(R.id.layout_input_name_group);
        btnCreate = findViewById(R.id.btn_createGroup);
        btnBack = findViewById(R.id.btn_back_to_conversations);
//        rcl_newgroup_contacts.setHasFixedSize(true);
        rcl_newgroup_contacts = findViewById(R.id.recyclerview_new_contact);
        rcl_newgroup_contacts.setLayoutManager(new LinearLayoutManager(this));
        newGroupAdapter = new NewGroupAdapter(this, contactListArray, this);
        rcl_newgroup_contacts.setAdapter(newGroupAdapter);
    }

    public void fullInfoUser(String userid) {
        long l = Long.parseLong(userid);
        TdApi.Function function = new TdApi.GetUserFullInfo(l);
        client.send(function, this::onResult);
    }

    public void danhBaUsers() {
        Log.d("onResult-->", "khởi tạo danhBaUsers()");
        TdApi.Function function = new TdApi.GetContacts();
        client.send(function, this::onResult);
    }

    // Phương thức để tạo nhóm cơ bản mới
    public void createBasicGroup(long[] userIds, String title) {
        // Tạo một đối tượng MessageBasicGroupChatCreate
        TdApi.MessageContent messageContent = new TdApi.MessageBasicGroupChatCreate();

        // Thiết lập thông tin nhóm cơ bản
        TdApi.MessageBasicGroupChatCreate basicGroupChatCreate = (TdApi.MessageBasicGroupChatCreate) messageContent;
        basicGroupChatCreate.title = title;
        basicGroupChatCreate.memberUserIds = userIds;

        // Tạo một đối tượng Message với nội dung là MessageBasicGroupChatCreate
        TdApi.Message message = new TdApi.Message();
        message.content = messageContent;

        // Gửi yêu cầu tạo nhóm cơ bản
        TdApi.Function createNewBasicGroup = new TdApi.CreateNewBasicGroupChat(basicGroupChatCreate.memberUserIds, basicGroupChatCreate.title);
        Log.d("onResult-->", "khởi tạo TdApi.CreateNewBasicGroupChat userIds = " + basicGroupChatCreate.memberUserIds + " title: " + basicGroupChatCreate.title);
        client.send(createNewBasicGroup, this::onResult);
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
            case TdApi.Chat.CONSTRUCTOR:
                TdApi.Chat chat = (TdApi.Chat) object;
                long basicGroupId = chat.id;
                // Xử lý tiếp theo với ID của nhóm cơ bản
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                Log.d("onResult-->", "TdApi.BasicgroupID: " + basicGroupId);
                finish();
                break;
            case TdApi.Messages.CONSTRUCTOR:
                TdApi.Messages messages = (TdApi.Messages) object;
                Log.d("onResult-->", "TdApi.Messages: " + messages.messages.toString());
                break;

            case TdApi.ChatTypeBasicGroup.CONSTRUCTOR:
                TdApi.ChatTypeBasicGroup chatTypeBasicGroup = (TdApi.ChatTypeBasicGroup) object;
                Log.d("onResult-->", "TdApi.ChatTypeBasicGroup: " + chatTypeBasicGroup.basicGroupId);
                break;

            case TdApi.UserFullInfo.CONSTRUCTOR:
                TdApi.UserFullInfo userFullInfo = (TdApi.UserFullInfo) object;
                Log.d("onResult-->", "UserFullInfo: " + userFullInfo.toString());
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

            case TdApi.User.CONSTRUCTOR:
                TdApi.User user = ((TdApi.User) object);
                Log.d("onResult-->", "User.CONSTRUCTOR: " + user.username);
                contactListArray.add(user);
                runOnUiThread(() -> newGroupAdapter.refresh());
                Log.d("onResult-->", "User.CONSTRUCTOR: " + contactListArray.size());
                break;

            default:
                Log.d("onResult-->", "default: " + object);
        }
    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {

    }


    @Override
    public void onItemCheckedChange(TdApi.User user, boolean isChecked) {
        if (isChecked) {
            if (!selectedUsers.contains(user)) {
                selectedUsers.add(user);
            }
        } else {
            selectedUsers.remove(user);
        }
    }
}

interface CheckBoxListener {
    void onItemCheckedChange(TdApi.User user, boolean isChecked);
}

class NewGroupAdapter extends RecyclerView.Adapter<NewGroupAdapter.ViewHolder> {
    View view;
    private Context context;
    private List<TdApi.User> userList;
    private CheckBoxListener checkBoxListener;

    private ArrayList<TdApi.User> arr_slect_0 = new ArrayList<>();

    NewGroupAdapter(Context context, List<TdApi.User> userList, CheckBoxListener checkBoxListener) {
        this.context = context;
        this.userList = userList;
        this.checkBoxListener = checkBoxListener;
    }

    public View getView() {
        return view;
    }

    @NonNull
    @Override
    public NewGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemview_contact_add_new_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TdApi.User user = userList.get(position);

        if (user instanceof TdApi.User) {
            holder.txt_name_User.setText(user.firstName + " " + user.lastName);

            holder.chk_select.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!arr_slect_0.contains(user)) {
                        arr_slect_0.add(user);
                    }
                } else {
                    arr_slect_0.remove(user);
                }
                checkBoxListener.onItemCheckedChange(user, isChecked);
            });

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
            holder.txt_name_User.setText("No name");
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
        private TextView txt_name_User;
        private ImageView img_avatar;
        private CheckBox chk_select;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_name_User = itemView.findViewById(R.id.txt_name_user_item);
            img_avatar = itemView.findViewById(R.id.img_contact_avt_newgroup);
            chk_select = itemView.findViewById(R.id.ckh_choosen_item);
        }
    }
}
