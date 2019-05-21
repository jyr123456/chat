package cn.ittiger.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ittiger.im.R;
import cn.ittiger.im.activity.ChatActivity;
import cn.ittiger.im.activity.base.IMBaseActivity;
import cn.ittiger.im.activity.task.Response;
import cn.ittiger.im.activity.task.SearchUserTask;
import cn.ittiger.im.bean.ContactEntity;
import cn.ittiger.im.bean.UserProfile;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.util.ActivityUtil;
import cn.ittiger.util.UIUtil;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static cn.ittiger.im.bean.UserProfile.TYPE_CONTACT;
import static cn.ittiger.im.bean.UserProfile.TYPE_NOT_CONTACT;
import static cn.ittiger.im.bean.UserProfile.TYPE_UNKNOWN;
import static cn.ittiger.im.smack.SmackManager.parseBareAddress;
import static cn.ittiger.im.smack.SmackManager.parseName;

public class UserInfoActivity extends IMBaseActivity implements Response.Listener<UserProfile>{
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbarTitle)
    TextView mToolbarTitle;

    @BindView(R.id.iamge_photo)
    ImageView iamgePhoto;
    @BindView(R.id.tv_nickname)
    TextView tvNickname;
    @BindView(R.id.tv_add)
    TextView tvAdd;

    String user;
    String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        initViews();
    }

    private void initViews() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTitle.setText("好友信息");
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        user = getIntent().getStringExtra("user");
        nickname = getIntent().getStringExtra("nickname");
        tvNickname.setText(nickname+"");
        register(user);
    }


    @OnClick(R.id.tv_add)
    public void onViewClicked() {
        detelFriend(user);
        if ((int) tvAdd.getTag() == 1) {
            //detelFriend(user);
        } else {
            //addFriend(user);
        }
    }

    private SearchUserTask task;
    RosterEntry entrys = null;
    public void register(final String user) {
        task = new SearchUserTask(this, UserInfoActivity.this, user);
        task.execute();

//        final Map<String, String> attributes = new HashMap<>();
//        Observable.just(attributes)
//                .subscribeOn(Schedulers.io())
//                .map(attribute -> {
//                    String name = parseName(user);
//                    String jid = null;
//                    if (name == null || name.trim().length() == 0) {
//                        jid = user + "@" + SmackManager.SERVER_NAME;
//                    } else {
//                        jid = parseBareAddress(user);
//                    }
//                    if (SmackManager.getInstance().getFriend(jid) != null) {
//                        entrys = SmackManager.getInstance().getFriend(jid);
//                        return true;
//                    } else {
//                        return false;
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(aBoolean -> {
//                    tvNickname.setText(nickname);
//                    if (aBoolean) {
//                        tvAdd.setTag(1);
//                        tvAdd.setText("删除");
//                    } else {
//                        tvAdd.setTag(0);
//
//                    }
//                });
    }

    public void detelFriend(final String user) {
        final Map<String, String> attributes = new HashMap<>();
        Observable.just(attributes)
                .subscribeOn(Schedulers.io())
                .map(attribute -> {
                    return SmackManager.getInstance().detelFreind(SmackManager.getInstance().getFriend(user));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        UIUtil.showToast(mActivity, "删除成功");
                        ChatActivity.instance.finish();
                        finish();
                    } else {
                        UIUtil.showToast(mActivity, "删除失败");
                    }
                });
    }

    public void addFriend(final String user) {
        Observable.create((Observable.OnSubscribe<RosterEntry>) subscriber -> {
            entrys = SmackManager.getInstance().getFriend(user);
            boolean flag = SmackManager.getInstance().addFriend(user, nickname, null);
            if (flag) {
                RosterEntry entry = SmackManager.getInstance().getFriend(user);
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

    @Override
    public void onResponse(UserProfile result) {
        if (result != null) {
            if (result.getType() == TYPE_UNKNOWN){
                Toast.makeText(this, R.string.search_contact_no_result, Toast.LENGTH_SHORT).show();
            }else {
                tvNickname.setText(result.getNickname());
                if (result.getType() == TYPE_CONTACT) {
                    tvAdd.setTag(1);
                    tvAdd.setText("删除");
                } else {
                    tvAdd.setTag(0);

                }

            }
        } else {
            Toast.makeText(this, R.string.search_contact_no_result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(Exception exception) {
        Toast.makeText(this, R.string.search_user_error, Toast.LENGTH_SHORT).show();
    }


}
