package cn.ittiger.im.bean;

import org.jivesoftware.smackx.disco.packet.DiscoverItems;

import cn.ittiger.database.annotation.Column;
import cn.ittiger.database.annotation.PrimaryKey;
import cn.ittiger.database.annotation.Table;

/**
 * 当前用户加入过的群聊
 */
@Table(name = "MultiChatRoom")
public class MultiChatRoom {
    @PrimaryKey(isAutoGenerate = true)
    private long mId;
    @Column(columnName = "roomJid")
    private String mRoomJid;

    public MultiChatRoom() {

    }

    public MultiChatRoom(String roomJid) {

        mRoomJid = roomJid;
    }

    public String getRoomJid() {

        return mRoomJid;
    }

    public void setRoomJid(String roomJid) {

        mRoomJid = roomJid;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof DiscoverItems.Item) {
            return ((DiscoverItems.Item) obj).getEntityID().equals(mRoomJid);
        }
        if(obj instanceof MultiChatRoom) {
            return ((MultiChatRoom) obj).mRoomJid.equals(mRoomJid);
        }
        return false;
    }
}
