package cn.ittiger.im.util;

import java.io.File;

import cn.ittiger.app.AppContext;
import cn.ittiger.im.constant.MessageType;
import cn.ittiger.util.ValueUtil;

/**
 * 应用相关文件帮助类
 */
public class AppFileHelper {

    public static String getAppRoot() {

        return AppContext.getInstance().getExternalCacheDir().getAbsolutePath();
    }

    public static String getAppImageCacheDir() {

        return getAppRoot() + "/image";
    }

    public static String getAppDBDir() {

        return getAppRoot() + "/db";
    }

    public static String getAppCrashDir() {

        return getAppRoot() + "/crash";
    }

    public static String getAppChatDir() {

        return getAppRoot() + "/chat";
    }

    public static File getAppChatMessageDir(int type) {

        String root = getAppChatDir();
        String child = "";
        if(type == MessageType.MESSAGE_TYPE_IMAGE.value()) {
            child = "chatImage";
        } else if(type == MessageType.MESSAGE_TYPE_VOICE.value()) {
            child = "chatAudio";
        }
        File file;
        if(ValueUtil.isEmpty(child)) {
            file = new File(root);
        } else {
            file = new File(root, child);
        }
        if(!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
