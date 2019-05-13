package cn.ittiger.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.roster.RosterEntry;

import cn.ittiger.im.R;
import cn.ittiger.im.activity.base.IMBaseActivity;
import cn.ittiger.im.bean.ContactEntity;
import cn.ittiger.im.bean.UserProfile;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.im.util.IMUtil;
import cn.ittiger.util.ActivityUtil;
import cn.ittiger.util.UIUtil;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static cn.ittiger.im.bean.UserProfile.TYPE_MYSELF;
import static cn.ittiger.im.bean.UserProfile.TYPE_NOT_CONTACT;

/**
 * 添加好友
 *
 * @author: laohu on 2016/12/24
 * @site: http://ittiger.cn
 */
public class AddFriendActivity extends IMBaseActivity implements View.OnClickListener {
    public static final String EXTRA_DATA_NAME_USER_PROFILE = "com.mstr.letschat.UserProfile";

    private UserProfile profile;
    private Button button;

    String avatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend_layout);
        profile = getIntent().getParcelableExtra(EXTRA_DATA_NAME_USER_PROFILE);

        ImageView imageView = (ImageView) findViewById(R.id.avatar);
        if (profile != null) {
            avatar = profile.getAvatar();
        }
        if (avatar != null) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
//            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
//            drawable.setCircular(true);
//            imageView.setImageDrawable(drawable);
        } else {
            imageView.setImageResource(R.drawable.ic_default_avatar);
        }

        ((TextView) findViewById(R.id.tv_nickname)).setText(profile.getNickname());

        button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(this);

        String status = profile.getStatus();
        if (status != null) {
            ((TextView) findViewById(R.id.tv_status)).setText(status);
        }

        setButtonText();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                if (profile.getType() == TYPE_MYSELF) {
                    onSendMessageClick();
                } else if (profile.getType() == TYPE_NOT_CONTACT) {
                    addFriend();
                } else {
                    onSendMessageClick();
                }

//            if (!isFriend(profile.getJid())) {
//                addFriend();
//            } else {
//                onSendMessageClick();
//            }
                break;
        }
    }

    private void setButtonText() {
        if (profile.getType() == TYPE_MYSELF) {
            button.setVisibility(View.GONE);
            button.setText("发送消息");
        } else if (profile.getType() == TYPE_NOT_CONTACT) {
            button.setText("添加");
        } else {
            button.setText("发送消息");
        }

//        if (isFriend(profile.getJid())) {
//            button.setText("发送消息");
//        } else {
//            button.setText("添加");
//        }
    }


    private void onSendMessageClick() {
        isFriend( profile.getJid());
    }


    public void addFriend() {
        Observable.create((Observable.OnSubscribe<RosterEntry>) subscriber -> {
            boolean flag = SmackManager.getInstance().addFriend(profile.getJid(), profile.getNickname(), null);
            if (flag) {
                RosterEntry entry = SmackManager.getInstance().getFriend(profile.getJid());
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
                        Toast.makeText(AddFriendActivity.this, R.string.contact_request_sent, Toast.LENGTH_SHORT).show();
                        ActivityUtil.finishActivity(mActivity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        UIUtil.showToast(mActivity, R.string.hint_add_friend_failure);
                    }

                    @Override
                    public void onNext(RosterEntry rosterEntry) {
                        EventBus.getDefault().post(new ContactEntity(rosterEntry));
                    }
                });
    }


    RosterEntry entrys;

    public boolean isFriend(final String jid) {
        if (SmackManager.getInstance().getFriend(jid) != null) {
            entrys = SmackManager.getInstance().getFriend(jid);
            IMUtil.startChatActivity(this,entrys);
            return true;
        } else {
            return false;
        }
    }

}
