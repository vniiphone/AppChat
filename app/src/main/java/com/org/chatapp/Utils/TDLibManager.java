package com.org.chatapp.Utils;

import android.content.Intent;
import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
//import org.drinkless.td.libcore.telegram.T

public class TDLibManager {
    private static Client client;
    private static Callback callback;

    private TDLibManager() {
    }

    public static Client getClient(Callback activity) {
        if (client == null) {
            client = Client.create(activity, null, null);
        }
        callback = activity;

        return client;
    }

    public interface Callback extends Client.ResultHandler {
        void onSetTdlibParametersSuccess();

        void onSetTdlibParametersError();

        @Override
        void onResult(TdApi.Object object);

        void onUonUpdatesReceived(TdApi.Object update);

    }

    public void onUonUpdatesReceived(TdApi.Object update) {
        // Xử lý cập nhật
        switch (update.getConstructor()) {
            case TdApi.UpdateNewMessage.CONSTRUCTOR:
                // Xử lý cập nhật tin nhắn mới
                TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) update;
                TdApi.Message message = updateNewMessage.message;
                // ...
                break;
            case TdApi.UpdateChatLastMessage.CONSTRUCTOR:
                // Xử lý cập nhật tin nhắn cuối cùng trong cuộc trò chuyện
                TdApi.UpdateChatLastMessage updateChatLastMessage = (TdApi.UpdateChatLastMessage) update;
                // ...
                break;
            case TdApi.UpdateChatReadInbox.CONSTRUCTOR:
                // Xử lý cập nhật trạng thái tin nhắn đã đọc trong cuộc trò chuyện
                TdApi.UpdateChatReadInbox updateChatReadInbox = (TdApi.UpdateChatReadInbox) update;
                // ...
                break;
            default:
                Log.d("TDLibManager", "Default " + update);
        }
    }

    public static void setTdlibParameters(Client client, Callback callback, String directoryPath) {
        TdApi.TdlibParameters tdlibParameters = new TdApi.TdlibParameters();
        tdlibParameters.apiId = 25729812;
        tdlibParameters.apiHash = "8285aa99a082199b9884819998f28c94";
        tdlibParameters.useMessageDatabase = true;
        tdlibParameters.useFileDatabase = true;
        tdlibParameters.useChatInfoDatabase = true;
        tdlibParameters.useSecretChats = true;
        tdlibParameters.getConstructor();
        tdlibParameters.systemLanguageCode = "en";
        tdlibParameters.databaseDirectory = directoryPath;
        tdlibParameters.deviceModel = "Moto";
        tdlibParameters.systemVersion = "12.0.0";
        tdlibParameters.applicationVersion = "0.1";
        tdlibParameters.enableStorageOptimizer = true;
        client.send(new TdApi.SetTdlibParameters(tdlibParameters),
                object -> handleSetTdlibParametersResult(object, callback));
    }

    private static void handleSetTdlibParametersResult(TdApi.Object result, Callback callback) {
        if (result.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
            if (callback != null) {
                callback.onSetTdlibParametersSuccess();
            }
        } else {
            if (callback == null) {
                callback.onSetTdlibParametersError();
            }
        }
    }

}
