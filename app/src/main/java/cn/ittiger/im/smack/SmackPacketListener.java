package cn.ittiger.im.smack;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;

import cn.ittiger.im.fragment.ContactFragment;


/**
 * create by Arkndiy on 2019/5/1 22:39
 * 类说明：
 */
public class SmackPacketListener implements PacketListener {
    private String response,acceptAdd;
    private Context ctx;
    public SmackPacketListener(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void processPacket(Stanza packet) {
        if (packet instanceof Presence) {
            System.out.println("PresenceService-"+packet.toXML());
            if(packet instanceof Presence){
                Presence presence = (Presence)packet;
                String from = presence.getFrom();//发送方
                String to = presence.getTo();//接收方
                if (presence.getType().equals(Presence.Type.subscribe)) {

                    if (SmackManager.getInstance().getFriend(from) == null){
                        //发送广播传递发送方的JIDfrom及字符串
                        acceptAdd = "收到添加请求！";
                        Intent intent = new Intent();
                        intent.putExtra("fromName", from);
                        intent.putExtra("acceptAdd", acceptAdd);
                        intent.setAction("cn.ittiger.im.activity.main");
                        ctx.sendBroadcast(intent);
                    }
                } else if (presence.getType().equals(
                        Presence.Type.subscribed)) {
                    //发送广播传递response字符串
                    response = "恭喜，对方同意添加好友！";
                    Intent intent = new Intent();
                    intent.putExtra("fromName", from);
                    intent.putExtra("response", response);
                    intent.setAction("cn.ittiger.im.activity.main");
                    ctx.sendBroadcast(intent);
                } else if (presence.getType().equals(
                        Presence.Type.unsubscribe)) {
                    //发送广播传递response字符串
                    response = "抱歉，对方拒绝添加好友，将你从好友列表移除！";
                    Intent intent = new Intent();
                    intent.putExtra("response", response);
                    intent.setAction("cn.ittiger.im.activity.main");
                    ctx.sendBroadcast(intent);
                } else if (presence.getType().equals(
                        Presence.Type.unsubscribed)){
                } else if (presence.getType().equals(
                        Presence.Type.unavailable)) {
                    System.out.println("好友下线！");
                } else {
                    System.out.println("好友上线！");
                }
            }
        }
    }

}
