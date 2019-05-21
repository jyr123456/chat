package cn.ittiger.im.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.green.hand.library.EasyDialog;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.im.R;
import cn.ittiger.im.activity.base.IMBaseActivity;
import cn.ittiger.im.bean.ChatMessage;
import cn.ittiger.im.bean.ChatRecord;
import cn.ittiger.im.bean.ChatUser;
import cn.ittiger.im.constant.MessageType;
import cn.ittiger.im.fragment.ContactFragment;
import cn.ittiger.im.fragment.MessageFragment;
import cn.ittiger.im.smack.SmackListenerManager;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.im.ui.FragmentSaveStateTabHost;
import cn.ittiger.im.util.DBHelper;
import cn.ittiger.im.util.DBQueryHelper;
import cn.ittiger.im.util.IntentHelper;
import cn.ittiger.im.util.NotificationUtils;
import cn.ittiger.util.ActivityUtil;
import cn.ittiger.util.UIUtil;
import cn.ittiger.util.ValueUtil;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static cn.ittiger.im.smack.SmackManager.parseBareAddress;
import static cn.ittiger.im.smack.SmackManager.parseName;

/**
 * 主页面
 *
 * @author: laohu on 2016/12/24
 * @site: http://ittiger.cn
 */
public class MainActivity extends IMBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TabHost.OnTabChangeListener, Toolbar.OnMenuItemClickListener {
    static {
        /**
         * 此方法必须必须引用appcompat-v7:23.4.0
         *
         * Button类控件使用vector必须使用selector进行包装才会起作用，不然会crash
         * 并且使用selector时必须调用下面的方法进行设置，否则也会crash
         * */
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final Class[] TABBAR_CLASSES = {MessageFragment.class, ContactFragment.class};
    private static final int[] TABBAR_DRAWABLES = {R.drawable.ic_tabbar_message, R.drawable.ic_tabbar_contact};
    private static final int[] TABBAR_NAMES = {R.string.text_message, R.string.text_contact};
    private static final int[] TABBAR_TAGS = {R.string.text_message, R.string.text_contact};

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbarTitle)
    TextView mToolbarTitle;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_container)
    NavigationView mNavigationView;
    @BindView(android.R.id.tabhost)
    FragmentSaveStateTabHost mTabHost;

    private MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        checkPermissions();
        initToolbar();
        initTabHost();

        //注册广播
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.ittiger.im.activity.main");
        registerReceiver(receiver, intentFilter);

        //普通消息接收监听
        SmackListenerManager.addGlobalListener();
        SmackManager.getInstance().RosterListener();

//        SmackManager.getInstance().addFriendLister();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 已经申请过权限，做想做的事
        } else {
            // 没有申请过权限，现在去申请
            EasyPermissions.requestPermissions(this, "申请",
                    1, perms);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void initToolbar() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_avatar);
        mToolbar.setOnMenuItemClickListener(this);
        mDrawerLayout.addDrawerListener(new NavDrawerListener());
    }

    /**MyReceiver
     * 主页底部Tab
     */
    private void initTabHost() {

        mTabHost.setup(this, getSupportFragmentManager(), R.id.tabItemContent);
        mTabHost.getTabWidget().setDividerDrawable(new ColorDrawable(Color.TRANSPARENT));
        mTabHost.setOnTabChangedListener(this);

        for (int i = 0; i < TABBAR_CLASSES.length; i++) {
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(TABBAR_TAGS[i]))
                    .setIndicator(getTabHostIndicator(i));
            mTabHost.addTab(tabSpec, TABBAR_CLASSES[i], null);
        }
    }

    private View getTabHostIndicator(int tabIndex) {

        View view = LayoutInflater.from(this).inflate(R.layout.tabbar_item_view, null);

        TextView tabName = ButterKnife.findById(view, R.id.tabbar_name);
        tabName.setText(TABBAR_NAMES[tabIndex]);

        ImageView tabIcon = ButterKnife.findById(view, R.id.tabbar_icon);
        tabIcon.setBackgroundResource(TABBAR_DRAWABLES[tabIndex]);
        return view;
    }

    @Override
    public void onTabChanged(String tabId) {

        mToolbarTitle.setText(tabId);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        if (item.isChecked()) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.change_use: //修改资料
                ActivityUtil.startActivity(mActivity, ChangeInfoActivity.class);
                break;
            case R.id.login_out://退出登录
                new EasyDialog(this)
                        .builder()
                        .setTitle("提示")
                        .setContent("是否退出登录！")
                        .setSureListener("确定", new EasyDialog.onSureListener() {
                            @Override
                            public void onClick(View v, Dialog dialog) {
                                logout();
                                dialog.dismiss();
                            }
                        })
                        .setCancelListener("取消", new EasyDialog.onCancelListener() {
                            @Override
                            public void onClick(View v, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.nav_share:
                ActivityUtil.startActivity(mActivity, AccountMngActivity.class);
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.toolbar_add_contact:
                ActivityUtil.startActivity(mActivity, SearchUserActivity.class);
                break;
            case R.id.toolbar_create_multi_chat:
                ActivityUtil.startActivity(mActivity, CreateMultiChatActivity.class);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean doubleExitAppEnable() {

        return true;
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    class NavDrawerListener implements DrawerLayout.DrawerListener {

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

            int size = mNavigationView.getMenu().size();
            for (int i = 0; i < size; i++) {
                if (mNavigationView.getMenu().getItem(i).isChecked()) {
                    mNavigationView.getMenu().getItem(i).setChecked(false);
                    break;
                }
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        SmackListenerManager.getInstance().destroy();
        SmackManager.getInstance().logout();
        SmackManager.getInstance().disconnect();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    public void logout() {
        final Map<String, String> attributes = new HashMap<>();

        Observable.just(attributes)
                .subscribeOn(Schedulers.io())
                .map(attribute -> SmackManager.getInstance().disconnect())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        UIUtil.showToast(MainActivity.this, "注销成功");
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        UIUtil.showToast(MainActivity.this, "注销失败");
                    }
                });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSwitchTabFragmentEvent(Integer index) {
        if (index == IntentHelper.CONTACT_TAB_INDEX || index == IntentHelper.MESSAGE_TAB_INDEX) {
            mTabHost.setCurrentTab(index);
        }
    }

    //广播接收器
    public class MyReceiver extends BroadcastReceiver {
        String alertSubName;

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收传递的字符串response
            Bundle bundle = intent.getExtras();
            String response = bundle.getString("response");
            if (response == null) {
                //获取传递的字符串及发送方JID
                String acceptAdd = bundle.getString("acceptAdd");
                String alertName = bundle.getString("fromName");
                if (alertName != null) {
                    //裁剪JID得到对方用户名
                    alertSubName = alertName.substring(0, alertName.indexOf("@"));
                }
                if (acceptAdd.equals("收到添加请求！")) {
                    //弹出一个对话框，包含同意和拒绝按钮
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("添加好友请求");
                    builder.setMessage("用户" + alertSubName + "请求添加你为好友");
                    //同意按钮监听事件，发送同意Presence包及添加对方为好友的申请
                    builder.setPositiveButton("同意", (dialog, arg1) -> {
                        Presence presenceRes = new Presence(Presence.Type.subscribed);
                        presenceRes.setTo(alertName);
                        SmackManager.getInstance().sendPacket(presenceRes);

                        addFriend(alertSubName);
                    });
                    //拒绝按钮监听事件，发送拒绝Presence包
                    builder.setNegativeButton("拒绝", (dialog, arg1) -> {
                        Presence presenceRes = new Presence(Presence.Type.unsubscribe);
                        presenceRes.setTo(alertName);
                        SmackManager.getInstance().sendPacket(presenceRes);
                    });
                    builder.show();
                }
            } else if (response.equals("恭喜，对方同意添加好友！")) {
                String alertName = bundle.getString("fromName");
                if (alertName != null) {
                    //裁剪JID得到对方用户名
                    alertSubName = alertName.substring(0, alertName.indexOf("@"));
                }
                getFriend(alertName);
            }
        }
    }

    private void startNotif(RosterEntry entrys) {
        //普通通知栏消息
        Intent intent = startChatIntent(this, entrys);
        NotificationUtils notificationUtils = new NotificationUtils(this, R.mipmap.ic_launcher, "好友申请",
                "对方已同意添加好友，赶快聊天吧！");
        notificationUtils.notify(intent);
    }

    private Intent startChatIntent(Context context, RosterEntry rosterEntry) {
        ChatUser chatUser = DBQueryHelper.queryChatUser(rosterEntry);

        ChatRecord chatRecord = DBQueryHelper.queryChatRecord(chatUser.getUuid());
        if (chatRecord == null) {
            chatRecord = new ChatRecord(chatUser);
        }
        EventBus.getDefault().post(chatRecord);//发起聊天时，发送一个事件到消息列表界面进行处理，如果不存在此聊天记录则创建一个新的
        Intent intent;
        if (chatUser.isMulti()) {
            intent = new Intent(context, MultiChatActivity.class);
        } else {
            intent = new Intent(context, ChatActivity.class);
        }
        intent.putExtra(IntentHelper.KEY_CHAT_DIALOG, chatUser);

        return intent;
    }

    RosterEntry entrys = null;

    public void getFriend(final String jid) {
        final Map<String, String> attributes = new HashMap<>();
        Observable.just(attributes)
                .subscribeOn(Schedulers.io())
                .map(attribute -> {
                    if (SmackManager.getInstance().getFriend(jid) != null) {
                        entrys = SmackManager.getInstance().getFriend(jid);
                        return true;
                    } else {
                        return false;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        startNotif(entrys);
                    } else {

                    }
                });
    }

    public void addFriend(String alertSubName) {
        Observable.create((Observable.OnSubscribe<RosterEntry>) subscriber -> {
            String name = parseName(alertSubName);
            String jid = null;
            if (name == null || name.trim().length() == 0) {
                jid = alertSubName + "@" + SmackManager.SERVER_NAME;
            } else {
                jid = parseBareAddress(alertSubName);
            }

            boolean flag = SmackManager.getInstance().addFriend(jid, alertSubName, null);
            if (flag) {
                RosterEntry entry = SmackManager.getInstance().getFriend(jid);
                subscriber.onNext(entry);
                subscriber.onCompleted();
            } else {
                subscriber.onError(new IllegalArgumentException());
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RosterEntry>() {
                    @Override
                    public void onCompleted() {
                        UIUtil.showToast(mActivity, R.string.hint_add_friend_success);

                    }

                    @Override
                    public void onError(Throwable e) {

                        UIUtil.showToast(mActivity, R.string.hint_add_friend_failure);
                    }

                    @Override
                    public void onNext(RosterEntry rosterEntry) {
//                        EventBus.getDefault().post(new ContactEntity(rosterEntry));
//                        send(rosterEntry, "你们已经成为好友");
                    }
                });
    }


    /**
     * 聊天窗口对象
     */
    private Chat mChat;

    /**
     * 发送消息
     *
     * @param rosterEntry
     * @param message
     */
    public void send(RosterEntry rosterEntry, final String message) {
        ChatUser mChatUser = DBQueryHelper.queryChatUser(rosterEntry);
        mChat = SmackManager.getInstance().createChat(mChatUser.getChatJid());
        if (ValueUtil.isEmpty(message)) {
            return;
        }
        Observable.just(message)
                .observeOn(Schedulers.io())
                .subscribe(message1 -> {
                    try {
                        JSONObject json = new JSONObject();
                        json.put(ChatMessage.KEY_FROM_NICKNAME, mChatUser.getMeNickname());
                        json.put(ChatMessage.KEY_MESSAGE_CONTENT, message1);
                        // json.toString()
                        mChat.sendMessage(message1);

                        ChatMessage msg = new ChatMessage(MessageType.MESSAGE_TYPE_TEXT.value(), true);
                        msg.setFriendNickname(mChatUser.getFriendNickname());
                        msg.setFriendUsername(mChatUser.getFriendUsername());
                        msg.setMeUsername(mChatUser.getMeUsername());
                        msg.setMeNickname(mChatUser.getMeNickname());
                        msg.setContent(message1);

                        DBHelper.getInstance().getSQLiteDB().save(msg);
                        EventBus.getDefault().post(msg);
                    } catch (Exception e) {
                        Logger.e(e, "send message failure");
                    }
                });
    }

}
