package cn.ittiger.im.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.im.R;
import cn.ittiger.im.activity.base.BaseChatActivity;
import cn.ittiger.im.bean.ChatMessage;
import cn.ittiger.im.constant.KeyBoardMoreFunType;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.im.util.DBQueryHelper;
import cn.ittiger.im.util.LoginHelper;
import cn.ittiger.util.UIUtil;
import cn.ittiger.util.ValueUtil;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static cn.ittiger.im.smack.SmackManager.parseBareAddress;
import static cn.ittiger.im.smack.SmackManager.parseName;

/**
 * 多人聊天
 *
 * @author: laohu on 2017/2/3
 * @site: http://ittiger.cn
 */
public class MultiChatActivity extends BaseChatActivity {
    /**
     * 多人聊天对象
     */
    private MultiUserChat mMultiUserChat;

    @BindView(R.id.image_info)
    ImageView imageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multichat_layout);
        ButterKnife.bind(this);

        imageInfo.setVisibility(View.VISIBLE);
        imageInfo.setOnClickListener(v -> {
            //弹出一个对话框，包含同意和拒绝按钮
            AlertDialog.Builder builder = new AlertDialog.Builder(MultiChatActivity.this);
            builder.setTitle("提示");
            builder.setMessage("是否退出" + mMultiUserChat.getRoom().split("@")[0] + "群");
            //同意按钮监听事件，发送同意Presence包及添加对方为好友的申请
            builder.setPositiveButton("同意", (dialog, arg1) -> {
                SmackManager.getInstance().kickChatRoom(mMultiUserChat.getRoom().split("@")[0], SmackManager.getInstance().getAccountName(), null);
                UIUtil.showToast(MultiChatActivity.this, "退出成功");
                String name = parseName(LoginHelper.getUser().getUsername());
                String jid = null;
                if (name == null || name.trim().length() == 0) {
                    jid = LoginHelper.getUser().getUsername() + "@" + SmackManager.SERVER_NAME;
                } else {
                    jid = parseBareAddress(LoginHelper.getUser().getUsername());
                }
                DBQueryHelper.detelChatRecord(DBQueryHelper.queryChatRecord(DBQueryHelper.queryChatUser(mMultiUserChat).getUuid()));
                finish();
            });

            //拒绝按钮监听事件，发送拒绝Presence包
            builder.setNegativeButton("我再想想", (dialog, arg1) -> {
            });
            builder.show();
        });

        mMultiUserChat = SmackManager.getInstance().getMultiChat(mChatUser.getFriendUsername());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveChatMessageEvent(ChatMessage message) {

        if (mChatUser.getFriendUsername().equals(message.getFriendUsername()) && message.isMulti()) {
            addChatMessageView(message);
        }
    }

    @Override
    public void send(String message) {

        if (ValueUtil.isEmpty(message)) {
            return;
        }
        Observable.just(message)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String message) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put(ChatMessage.KEY_MESSAGE_CONTENT, message);
                            json.put(ChatMessage.KEY_MULTI_CHAT_SEND_USER, mChatUser.getMeUsername());
                            mMultiUserChat.sendMessage(json.toString());
                        } catch (Exception e) {
                            Logger.e(e, "send message failure");
                        }
                    }
                });
    }

    @Override
    public void sendVoice(File audioFile) {

    }

    @Override
    public void functionClick(KeyBoardMoreFunType funType) {

    }
}
