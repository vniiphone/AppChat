package com.org.chatapp.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.org.chatapp.R;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestChatV1Activity extends AppCompatActivity {
    private static Client client = null;

    private static TdApi.AuthorizationState authorizationState = null;
    private static volatile boolean haveAuthorization = false;
    private static volatile boolean needQuit = false;
    private static volatile boolean canQuit = false;
    private static boolean haveFullMainChatList = false;
    private static final String newLine = System.getProperty("line.separator");
    private static final ConcurrentMap<Long, TdApi.User> users = new ConcurrentHashMap<Long, TdApi.User>();
    private static final ConcurrentMap<Long, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Long, TdApi.UserFullInfo>();
    private static final Client.ResultHandler defaultHandler = (Client.ResultHandler) new DefaultHandler();
    private static final String commandsLine = "Enter command (" +
            "gcs - GetChats, " +
            "gc <chatId> - GetChat," +
            " me - GetMe, " +
            "sm <chatId>" +
            " <message> - SendMessage," +
            " lo - LogOut, q - Quit): ";
    private static volatile String currentPrompt = null;
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();

    private static final Lock authorizationLock = new ReentrantLock();
    private static final Condition gotAuthorization = authorizationLock.newCondition();
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private static void print(String str) {
        if (currentPrompt != null) {
            System.out.println("");
        }
        System.out.println(str);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_chat_v1);

        // set log message handler to handle only fatal errors (0) and plain log messages (-1)
        // create client
        client = Client.create(new UpdateHandler(), null, null);

        // test Client.execute
        defaultHandler.onResult(Client.execute(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test")));
        // main loop
        while (!needQuit) {
            // await authorization
            Log.d("TestChatV1-onCreate: ","1. await authorization");
            authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    try {
                        Log.d("TestChatV1-onCreate: ","2. gotAuthorization");
                        gotAuthorization.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                Log.d("TestChatV1-onCreate: ","3. authorizationLock");
                authorizationLock.unlock();
            }

            while (haveAuthorization) {
                Log.d("TestChatV1-onCreate: ","4. haveAuthorization ->  getCommand()");
                getCommand();
            }
        }
        while (!canQuit) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }

    private static long getChatId(String arg) {
        long chatId = 0;
        try {
            Log.d("TestChatV1-getChatId: ","5. chatId:"+chatId);
            chatId = Long.parseLong(arg);
        } catch (NumberFormatException ignored) {
        }
        return chatId;
    }
    private static String promptString(String prompt) {
        System.out.print(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        //            str = reader.readLine();
        str = "gc";
        currentPrompt = null;
        return str;
    }
    private static int toInt(String arg) {
        int result = 0;
        try {
            result = Integer.parseInt(arg);
        } catch (NumberFormatException ignored) {
        }
        return result;
    }

    private void getCommand() {
        String command = promptString(commandsLine);
        String[] commands = command.split(" ", 2);
        try {
            switch (commands[0]) {
                case "gcs": {
                    int limit = 20;
                    if (commands.length > 1) {
                        limit = toInt(commands[1]);
                    }
                    Log.d("TestChatV1-getCommand: ","6. gcs:");
                    getMainChatList(limit);
                    break;
                }
                case "gc":
                    Log.d("TestChatV1-getCommand: ","6. gcs:");
                    client.send(new TdApi.GetChat(getChatId(commands[1])), defaultHandler);
                    break;
                case "me":
                    client.send(new TdApi.GetMe(), defaultHandler);
                    break;
                case "sm": {
                    String[] args = commands[1].split(" ", 2);
                    sendMessage(getChatId(args[0]), args[1]);
                    break;
                }
                case "lo":
                    haveAuthorization = false;
                    client.send(new TdApi.LogOut(), defaultHandler);
                    break;
                case "q":
                    needQuit = true;
                    haveAuthorization = false;
                    client.send(new TdApi.Close(), defaultHandler);
                    break;
                default:
                    System.err.println("Unsupported command: " + command);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            print("Not enough arguments");
        }
    }


    private static void sendMessage(long chatId, String message) {
        // initialize reply markup just for testing
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});

        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        client.send(new TdApi.SendMessage(chatId, 0, 0, null, replyMarkup, content), defaultHandler);
    }

    private static void setChatPositions(TdApi.Chat chat, TdApi.ChatPosition[] positions) {
        synchronized (mainChatList) {
            synchronized (chat) {
                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isRemoved = mainChatList.remove(new OrderedChat(chat.id, position));
                        assert isRemoved;
                    }
                }

                chat.positions = positions;

                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isAdded = mainChatList.add(new OrderedChat(chat.id, position));
                        assert isAdded;
                    }
                }
            }
        }
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
            return 0;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.chatId == o.chatId && this.position.order == o.position.order;
        }
    }

    private static void getMainChatList(final int limit) {
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                // send LoadChats request if there are some unknown chats and have not enough known chats
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                if (((TdApi.Error) object).code == 404) {
                                    synchronized (mainChatList) {
                                        haveFullMainChatList = true;
                                    }
                                } else {
                                    System.err.println("Receive an error for LoadChats:" + newLine + object);
                                }
                                break;
                            case TdApi.Ok.CONSTRUCTOR:
                                // chats had already been received through updates, let's retry request
                                getMainChatList(limit);
                                break;
                            default:
                                System.err.println("Receive wrong response from TDLib:" + newLine + object);
                        }
                    }
                });
                return;
            }

            java.util.Iterator<OrderedChat> iter = mainChatList.iterator();
            System.out.println();
            System.out.println("First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
            for (int i = 0; i < limit && i < mainChatList.size(); i++) {
                long chatId = iter.next().chatId;
                TdApi.Chat chat = chats.get(chatId);
                synchronized (chat) {
                    System.out.println(chatId + ": " + chat.title);
                }
            }
            print("");
        }
    }

    private static class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("Receive an error:" + newLine + object);
                    onAuthorizationStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    System.err.println("Receive wrong response from TDLib:" + newLine + object);
            }
        }

        private static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        }
    }

    private static class UpdateHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;

                case TdApi.UpdateUser.CONSTRUCTOR:
                    TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                    users.put(updateUser.user.id, updateUser.user);
                    break;
                case TdApi.UpdateUserStatus.CONSTRUCTOR: {
                    TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    TdApi.User user = users.get(updateUserStatus.userId);
                    synchronized (user) {
                        user.status = updateUserStatus.status;
                    }
                    break;
                }
                case TdApi.UpdateNewChat.CONSTRUCTOR: {
                    TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                    TdApi.Chat chat = updateNewChat.chat;
                    synchronized (chat) {
                        chats.put(chat.id, chat);

                        TdApi.ChatPosition[] positions = chat.positions;
                        chat.positions = new TdApi.ChatPosition[0];
                        setChatPositions(chat, positions);
                    }
                    break;
                }
                case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                    TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.title = updateChat.title;
                    }
                    break;
                }

                case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastMessage = updateChat.lastMessage;
                        setChatPositions(chat, updateChat.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPosition.CONSTRUCTOR: {
                    TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;
                    if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                        break;
                    }

                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        int i;
                        for (i = 0; i < chat.positions.length; i++) {
                            if (chat.positions[i].list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                                break;
                            }
                        }
                        TdApi.ChatPosition[] new_positions = new TdApi.ChatPosition[chat.positions.length + (updateChat.position.order == 0 ? 0 : 1) - (i < chat.positions.length ? 1 : 0)];
                        int pos = 0;
                        if (updateChat.position.order != 0) {
                            new_positions[pos++] = updateChat.position;
                        }
                        for (int j = 0; j < chat.positions.length; j++) {
                            if (j != i) {
                                new_positions[pos++] = chat.positions[j];
                            }
                        }
                        assert pos == new_positions.length;

                        setChatPositions(chat, new_positions);
                    }
                    break;
                }
                case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                        chat.unreadCount = updateChat.unreadCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                    TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatPermissions.CONSTRUCTOR: {
                    TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.permissions = update.permissions;
                    }
                    break;
                }
                case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR: {
                    TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.hasScheduledMessages = update.hasScheduledMessages;
                    }
                    break;
                }
                case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                    TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                    usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                    break;

                default:
                    // print("Unsupported update:" + newLine + object);
            }
        }

        private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
            if (authorizationState != null) {
                TestChatV1Activity.authorizationState = authorizationState;
            }
            switch (TestChatV1Activity.authorizationState.getConstructor()) {
                /*case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                    TdApi.SetTdlibParameters request = new TdApi.SetTdlibParameters();
                    request. = "tdlib";
                    request.useMessageDatabase = true;
                    request.useSecretChats = true;
                    request.apiId = 94575;
                    request.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                    request.systemLanguageCode = "en";
                    request.deviceModel = "Desktop";
                    request.applicationVersion = "1.0";
                    request.enableStorageOptimizer = true;

                    client.send(request, new AuthorizationRequestHandler());
                    break;*/
                case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                    String phoneNumber = promptString("Please enter phone number: ");
                    client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                    break;
                }
                case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                    String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) TestChatV1Activity.authorizationState).link;
                    System.out.println("Please confirm this login link on another device: " + link);
                    break;
                }

                case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                    String code = promptString("Please enter authentication code: ");
                    client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                    break;
                }
                case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                    String firstName = promptString("Please enter your first name: ");
                    String lastName = promptString("Please enter your last name: ");
                    client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                    break;
                }
                case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                    String password = promptString("Please enter password: ");
                    client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                    break;
                }
                case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                    haveAuthorization = true;
                    authorizationLock.lock();
                    try {
                        gotAuthorization.signal();
                    } finally {
                        authorizationLock.unlock();
                    }
                    break;
                case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                    haveAuthorization = false;
                    print("Logging out");
                    break;
                case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                    haveAuthorization = false;
                    print("Closing");
                    break;
                case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                    print("Closed");
                    if (!needQuit) {
                        client = Client.create(new UpdateHandler(), null, null); // recreate client after previous has closed
                    } else {
                        canQuit = true;
                    }
                    break;
                default:
                    System.err.println("Unsupported authorization state:" + newLine + TestChatV1Activity.authorizationState);
            }
        }
    }


}

