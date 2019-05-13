package cn.ittiger.im.smack;

import android.content.Intent;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.ittiger.app.AppContext;
import cn.ittiger.im.app.App;
import cn.ittiger.im.bean.ChatMessage;
import cn.ittiger.im.constant.Constant;
import cn.ittiger.im.constant.MessageType;
import cn.ittiger.im.util.DBHelper;
import cn.ittiger.im.util.LoginHelper;

/**
 * Smack好友消息监听处理
 *
 * @author: laohu on 2017/1/18
 * @site: http://ittiger.cn
 */
public class SmackFriendChangeManagerListener implements StanzaListener {

    private String mMeNickName = LoginHelper.getUser().getNickname();
    private String response,acceptAdd;

    /**
     * Process the next stanza(/packet) sent to this stanza(/packet) listener.
     * <p>
     * A single thread is responsible for invoking all listeners, so
     * it's very important that implementations of this method not block
     * for any extended period of time.
     * </p>
     *
     * @param packet the stanza(/packet) to process.
     */
    @Override
    public void processPacket(Stanza packet) {
        if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            String fromId = presence.getFrom();
            String from = presence.getFrom().split("@")[0];//我这里只为了打印去掉了后缀
            Logger.d("getType:" + presence.getType());
            if (presence.getType().equals(Presence.Type.subscribe)) {
                Logger.d("yangbinnew请求添加好友" + from);
                //发送广播传递发送方的JIDfrom及字符串
                acceptAdd = "收到添加请求！";
                Intent intent = new Intent();
                intent.putExtra("fromName", from);
                intent.putExtra("acceptAdd", acceptAdd);
                intent.setAction("cn.ittiger.im.activity.AddFriendActivity");
                AppContext.getInstance().sendBroadcast(intent);
            } else if (presence.getType().equals(Presence.Type.subscribed)) {//对方同意订阅
                Logger.d("yangbinnew同意订阅" + from);
                //发送广播传递response字符串
                response = "恭喜，对方同意添加好友！";
                Intent intent = new Intent();
                intent.putExtra("response", response);
                intent.setAction("cn.ittiger.im.activity.AddFriendActivity");
                AppContext.getInstance().sendBroadcast(intent);
            } else if (presence.getType().equals(Presence.Type.unsubscribe)) {//取消订阅
                Logger.d("yangbinnew取消订阅" + from);
                //发送广播传递response字符串
                response = "抱歉，对方拒绝添加好友，将你从好友列表移除！";
                Intent intent = new Intent();
                intent.putExtra("response", response);
                intent.setAction("cn.ittiger.im.activity.AddFriendActivity");
                AppContext.getInstance(). sendBroadcast(intent);
            } else if (presence.getType().equals(Presence.Type.unsubscribed)) {//拒绝订阅
                Logger.d("yangbinnew拒绝订阅" + from);
            } else if (presence.getType().equals(Presence.Type.unavailable)) {//离线
                Logger.d("yangbinnew离线" + from);
            } else if (presence.getType().equals(Presence.Type.available)) {//上线
                Logger.d("yangbinnew上线" + from);
            }
        }
    }
}
