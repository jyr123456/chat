package cn.ittiger.im.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ess.filepicker.FilePicker;
import com.ess.filepicker.model.EssFile;
import com.ess.filepicker.util.Const;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.im.R;
import cn.ittiger.im.activity.base.BaseChatActivity;
import cn.ittiger.im.bean.ChatMessage;
import cn.ittiger.im.constant.FileLoadState;
import cn.ittiger.im.constant.KeyBoardMoreFunType;
import cn.ittiger.im.constant.MessageType;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.im.util.AppFileHelper;
import cn.ittiger.im.util.DBHelper;
import cn.ittiger.im.util.FileUtils;
import cn.ittiger.util.ActivityUtil;
import cn.ittiger.util.BitmapUtil;
import cn.ittiger.util.DateUtil;
import cn.ittiger.util.FileUtil;
import cn.ittiger.util.UIUtil;
import cn.ittiger.util.ValueUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 单聊窗口
 */
public class ChatActivity extends BaseChatActivity {
    @BindView(R.id.image_info)
    ImageView imageInfo;



    //长按后显示的 Item
    final String[] items = new String[] { "保存图片"};
    //图片转成Bitmap数组
    final Bitmap[] bitmap = new Bitmap[1];

    /**
     * 聊天窗口对象
     */
    private Chat mChat;

    public static ChatActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_layout);
        ButterKnife.bind(this);
        initView();
        instance = this;
        mChat = SmackManager.getInstance().createChat(mChatUser.getChatJid());
        addReceiveFileListener();
    }

    public void initView() {
        imageInfo.setVisibility(View.VISIBLE);
        imageInfo.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("ChatJid", mChatUser.getChatJid());
            bundle.putString("nickname", mChatUser.getFriendNickname());
            ActivityUtil.startActivity(ChatActivity.this, UserInfoActivity.class, bundle);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveChatMessageEvent(ChatMessage message) {
        if (mChatUser.getMeUsername().equals(message.getMeUsername()) && !message.isMulti()) {
            addChatMessageView(message);
        }
    }

    /**
     * 发送消息
     *
     * @param message
     */
    @Override
    public void send(final String message) {
        //判断该好友是否在线 ，true在线， flase不在线
        Boolean b = SmackManager.getInstance().isonline(mChatUser.getFriendNickname()+"@www.jyr.com");
        if(!b){
            UIUtil.showToast(ChatActivity.this, "该好友已离线，无法发送！");
            return;
        }
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
                            json.put(ChatMessage.KEY_FROM_NICKNAME, mChatUser.getMeNickname());
                            json.put(ChatMessage.KEY_MESSAGE_CONTENT, message);
                            // json.toString()
                            mChat.sendMessage(message);

                            ChatMessage msg = new ChatMessage(MessageType.MESSAGE_TYPE_TEXT.value(), true);
                            msg.setFriendNickname(mChatUser.getFriendNickname());
                            msg.setFriendUsername(mChatUser.getFriendUsername());
                            msg.setMeUsername(mChatUser.getMeUsername());
                            msg.setMeNickname(mChatUser.getMeNickname());
                            msg.setContent(message);

                            DBHelper.getInstance().getSQLiteDB().save(msg);
                            EventBus.getDefault().post(msg);
                        } catch (Exception e) {
                            Logger.e(e, "send message failure");
                        }
                    }
                });
    }

    /**
     * 发送文件
     *
     * @param file
     */
    public void sendFile(final File file, int messageType) {
        //判断该好友是否在线 ，true在线， flase不在线
        Boolean b = SmackManager.getInstance().isonline(mChatUser.getFriendNickname()+"@www.jyr.com");
        if(!b){
            UIUtil.showToast(ChatActivity.this, "该好友已离线，无法发送！");
            return;
        }
        final OutgoingFileTransfer transfer = SmackManager.getInstance().getSendFileTransfer(mChatUser.getFileJid());
        String sendType = null;
        try {
            if (messageType == 1) {
                sendType = "image";
            } else if (messageType == 2) {
                sendType = "video";
            } else if (messageType == 3) {
                sendType = "file";
            }
            transfer.sendFile(file, sendType);
            checkTransferStatus(transfer, file, messageType, true);
        } catch (SmackException e) {
            Logger.e(e, "send file failure");
        }
    }



    /**
     * 接收文件
     */
    public void addReceiveFileListener() {
        SmackManager.getInstance().addFileTransferListener(request -> {
            // Accept it
            IncomingFileTransfer transfer = request.accept();
            IncomingFileTransfer transfer2 = request.accept();
            try {
                int messageType = 0;
                if (request.getMimeType().contains("image")) {
                    messageType = MessageType.MESSAGE_TYPE_IMAGE.value();
                } else if (request.getMimeType().contains("video")) {
                    messageType = MessageType.MESSAGE_TYPE_VOICE.value();
                } else if (request.getFileName().contains("xls") || request.getFileName().contains("doc") ||
                        request.getFileName().contains("docx") || request.getFileName().contains("xlsx") ||
                        request.getFileName().contains("pdf") || request.getFileName().contains("pps")
                        || request.getFileName().contains("ppt") || request.getFileName().contains("txt")) {
                    messageType = MessageType.MESSAGE_TYPE_FILE.value();
                }
                String fileName = String.valueOf(System.currentTimeMillis());
                File file = new File(FileUtils.getReceivedImagesDir(ChatActivity.this), request.getFileName());
                transfer.recieveFile(file);

                if (request.getMimeType().contains("image")) {
                    //保存图片到本地图库
                    File file2 = new File("/storage/emulated/0/Pictures/", request.getFileName());
                    transfer.recieveFile(file2);
                } else if (request.getMimeType().contains("video")) {

                }else{
                    //保存文件到本地文件夹根目录
                    File file2 = new File("/storage/emulated/0/", request.getFileName());
                    transfer.recieveFile(file2);
                }

                transfer.recieveFile(file);
                checkTransferStatus(transfer, file, messageType, false);


            } catch (Exception e) {
                Logger.e(e, "receive file failure");
            }
        });
    }

    /**
     * 检查发送文件、接收文件的状态
     *
     * @param transfer
     * @param file        发送或接收的文件
     * @param messageType 文件类型，语音或图片
     * @param isMeSend    是否为发送
     */
    private void checkTransferStatus(final FileTransfer transfer, final File file, final int messageType, final boolean isMeSend) {
        final ChatMessage msg = new ChatMessage(messageType, isMeSend);
        msg.setFriendNickname(mChatUser.getFriendNickname());
        msg.setFriendUsername(mChatUser.getFriendUsername());
        msg.setMeUsername(mChatUser.getMeUsername());
        msg.setMeNickname(mChatUser.getMeNickname());
        msg.setFilePath(file.getAbsolutePath());
        DBHelper.getInstance().getSQLiteDB().save(msg);

        Observable.create((Observable.OnSubscribe<ChatMessage>) subscriber -> {
            addChatMessageView(msg);
            subscriber.onNext(msg);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(chatMessage -> {
                    long startTime = -1;
                    while (!transfer.isDone()) {
                        double progress = transfer.getProgress();
                        if (progress > 0.0 && startTime == -1) {
                            startTime = System.currentTimeMillis();
                        }
                        progress *= 100;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("---------", "used " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds  ");
                    return chatMessage;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatMessage -> {
                    if (FileTransfer.Status.complete.toString().equals(transfer.getStatus())) {//传输完成
                        chatMessage.setFileLoadState(FileLoadState.STATE_LOAD_SUCCESS.value());
                        mAdapter.update(chatMessage);
                    } else {
                        chatMessage.setFileLoadState(FileLoadState.STATE_LOAD_ERROR.value());
                        mAdapter.update(chatMessage);
                    }
                });
    }

    /**
     * 发送语音消息
     *
     * @param audioFile
     */
    @Override
    public void sendVoice(File audioFile) {
        //判断该好友是否在线 ，true在线， flase不在线
        /*Boolean b = SmackManager.getInstance().isonline(mChatUser.getFriendNickname()+"@www.jyr.com");
        if(!b){
            UIUtil.showToast(ChatActivity.this, "该好友已离线，无法发送！");
            return;
        }*/
        sendFile(audioFile, MessageType.MESSAGE_TYPE_VOICE.value());
    }

    /**
     * 选择图片
     */
    private static final int REQUEST_CODE_GET_IMAGE = 1;
    /**
     * 拍照
     */
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    /**
     * 文件
     */
    private static final int REQUEST_CODE_CHOOSE = 23;

    @Override
    public void functionClick(KeyBoardMoreFunType funType) {
        switch (funType) {
            case FUN_TYPE_IMAGE://选择图片
                selectImage();
                break;
            case FUN_TYPE_TAKE_PHOTO://拍照
                takePhoto();
                break;
            case FUN_TYPE_TAKE_FILE://文件
                pickDocClicked();
                break;
        }
    }

    public void pickDocClicked() {
        onPickDoc();
    }

    public void onPickDoc() {
        FilePicker
                .from(this)
                .chooseForBrowser()
                .setMaxCount(2)
                .setTheme(R.style.FilePicker_Dracula)
                .setFileTypes("xls", "doc", "docx", "pdf", "pps", "ppt", "txt", "xlsx")
                .requestCode(REQUEST_CODE_CHOOSE)
                .start();
    }


    /**
     * 从图库选择图片
     */
    public void selectImage() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_GET_IMAGE);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_GET_IMAGE);
        }
    }

    private String mPicPath = "";

    /**
     * 拍照
     */
    public void takePhoto() {
        String dir = AppFileHelper.getAppChatMessageDir(MessageType.MESSAGE_TYPE_IMAGE.value()).getAbsolutePath();
        mPicPath = dir + "//" + DateUtil.formatDatetime(new Date(), "yyyyMMddHHmmss") + ".png";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri fileUri;
        //Android7.0以上使用 content://来替代file://Uri。
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(mActivity, "cn.ittiger.im.fileprovider", new File(mPicPath));
        } else {
            fileUri = Uri.fromFile(new File(mPicPath));
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_TAKE_PHOTO) {//拍照成功
                takePhotoSuccess();
            } else if (requestCode == REQUEST_CODE_GET_IMAGE) {//图片选择成功
                Uri dataUri = data.getData();
                if (dataUri != null) {
                    File file = FileUtil.uri2File(this, dataUri);
                    sendFile(file, MessageType.MESSAGE_TYPE_IMAGE.value());
                }
            } else if (requestCode == REQUEST_CODE_CHOOSE) {
                ArrayList<EssFile> essFileList = data.getParcelableArrayListExtra(Const.EXTRA_RESULT_SELECTION);
                if (essFileList.size() > 1) {
                    UIUtil.showToast(ChatActivity.this, "最多选择一个文件");
                } else {
                    StringBuilder builder = new StringBuilder();
                    for (EssFile file : essFileList) {
                        sendFile(new File(file.getAbsolutePath()), MessageType.MESSAGE_TYPE_FILE.value());
                        builder.append(file.getMimeType()).append(" | ").append(file.getAbsolutePath()).append("\n\n");
                    }
                    Log.i("--------------", builder.toString());
                }

            }
        }
    }

    /**
     * 照片拍摄成功
     */
    public void takePhotoSuccess() {
        Bitmap bitmap = BitmapUtil.createBitmapWithFile(mPicPath, 320);
        BitmapUtil.createPictureWithBitmap(mPicPath, bitmap, 80);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        sendFile(new File(mPicPath), MessageType.MESSAGE_TYPE_IMAGE.value());
    }
}
