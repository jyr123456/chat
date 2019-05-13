package cn.ittiger.im.util;

import cn.ittiger.im.bean.ChatMessage;
import cn.ittiger.im.bean.ChatRecord;
import cn.ittiger.im.bean.ChatUser;
import cn.ittiger.im.bean.UserProfile;
import cn.ittiger.im.constant.Constant;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.List;

/**
 * 数据库查询帮助类
 *
 * @author: laohu on 2017/1/20
 * @site: http://ittiger.cn
 */
public class DBQueryHelper {

    /**
     * 注册存储JID，如果数据库中不存在则创建新的
     *
     * @param user  注册用户
     * @return
     */
    public static UserProfile queryUserProfile(UserProfile user) {

        return queryUserProfile(user.getJid(), user.getUserName());
    }

    /**
     * 根据好友信息查询对应的ChatUser，如果数据库中不存在则创建新的
     *
     * @param friendRoster  好友信息
     * @return
     */
     public static ChatUser queryChatUser(RosterEntry friendRoster) {
         return queryChatUser(friendRoster.getName(), friendRoster.getName());
     }


    /**
     * 是否存在注册，如果数据库中不存在则创建新的
     *
     * @param jid
     * @param nickname
     * @return
     */
    public static boolean isRegister(String jid, String nickname) {
        String whereClause = "jid=? and nickname=?";
        String[] whereArgs = {jid, nickname};
        UserProfile chatUser = DBHelper.getInstance().getSQLiteDB().queryOne(UserProfile.class, whereClause, whereArgs);
        if(chatUser == null) {
           return false;
        }
        return true;
    }



    /**
     * 注册，如果数据库中不存在则创建新的
     *
     * @param jid
     * @param nickname
     * @return
     */
    public static UserProfile queryUserProfile(String jid, String nickname) {
        String whereClause = "jid=? and nickname=?";
        String[] whereArgs = {jid, nickname};
        UserProfile chatUser = DBHelper.getInstance().getSQLiteDB().queryOne(UserProfile.class, whereClause, whereArgs);
        if(chatUser == null) {
            chatUser = new UserProfile(jid, nickname);
            DBHelper.getInstance().getSQLiteDB().save(chatUser);
        }
        return chatUser;
    }


    /**
     * 根据多人聊天信息查询对应的ChatUser，如果数据库中不存在则创建新的
     *
     * @param multiUserChat
     * @return
     */
    public static ChatUser queryChatUser(MultiUserChat multiUserChat) {
        String friendUserName = multiUserChat.getRoom();
        int idx = friendUserName.indexOf(Constant.MULTI_CHAT_ADDRESS_SPLIT);
        String friendNickName = friendUserName.substring(0, idx);
        String whereClause = "meUserName=? and friendUserName=? and isMulti=?";
        String[] whereArgs = {LoginHelper.getUser().getUsername(), friendUserName, "true"};
        ChatUser chatUser = DBHelper.getInstance().getSQLiteDB().queryOne(ChatUser.class, whereClause, whereArgs);
        if(chatUser == null) {
            chatUser = new ChatUser(friendUserName, friendNickName, true);
            DBHelper.getInstance().getSQLiteDB().save(chatUser);
        }
        return chatUser;
    }


    /**
     * 根据好友信息查询对应的ChatUser，如果数据库中不存在则创建新的
     *
     * @param friendUserName
     * @param friendNickName
     * @return
     */
    public static ChatUser queryChatUser(String friendUserName, String friendNickName) {
        String whereClause = "meUserName=? and friendUserName=?";
        String[] whereArgs = {LoginHelper.getUser().getUsername(), friendUserName};
        ChatUser chatUser = DBHelper.getInstance().getSQLiteDB().queryOne(ChatUser.class, whereClause, whereArgs);
        if(chatUser == null) {
            chatUser = new ChatUser(friendUserName, friendNickName);
            DBHelper.getInstance().getSQLiteDB().save(chatUser);
        }
        return chatUser;
    }

    /**
     * 查询登陆用户的所有聊天用户记录
     *
     * @return
     */
    public static List<ChatRecord> queryChatRecord() {
        String whereClause = "meUserName=?";
        String[] whereArgs = {LoginHelper.getUser().getUsername()};
        return DBHelper.getInstance().getSQLiteDB().query(ChatRecord.class, whereClause, whereArgs);
    }

    /**
     * 删除登陆用户的群组聊天
     *
     * @return
     */
    public static void detelChatRecord(ChatRecord chatRecord) {
        DBHelper.getInstance().getSQLiteDB().delete(chatRecord);
    }

    /**
     * 根据主键查询ChatRecord
     *
     * @param jid
     * @return
     */
    public static ChatRecord queryChatRecord(String jid) {
        return DBHelper.getInstance().getSQLiteDB().query(ChatRecord.class, jid);
    }

    public static List<ChatMessage> queryChatMessage(ChatUser chatUser) {
        String whereClause = "meUserName=? and friendUserName=?";
        String[] whereArgs = {chatUser.getMeUsername(), chatUser.getFriendUsername()};
        return DBHelper.getInstance().getSQLiteDB().query(ChatMessage.class, whereClause, whereArgs);
    }
}
