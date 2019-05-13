package cn.ittiger.im.activity;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ittiger.im.R;
import cn.ittiger.im.activity.base.IMBaseActivity;
import cn.ittiger.im.bean.User;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.im.util.LoginHelper;
import cn.ittiger.util.ActivityUtil;
import cn.ittiger.util.UIUtil;
import cn.ittiger.util.ValueUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ChangeInfoActivity extends IMBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbarTitle)
    TextView mToolbarTitle;

    //用户名
    @BindView(R.id.til_username)
    TextInputLayout mUserTextInput;
    @BindView(R.id.acet_username)
    AppCompatEditText mUserEditText;

    //昵称
    @BindView(R.id.til_nickname)
    TextInputLayout mNicknameTextInput;
    @BindView(R.id.acet_nickname)
    AppCompatEditText mNicknameEditText;

    //密码
    @BindView(R.id.til_password)
    TextInputLayout mPasswordTextInput;
    @BindView(R.id.acet_password)
    AppCompatEditText mPasswordEditText;

    //重复密码
    @BindView(R.id.til_repassword)
    TextInputLayout mRePasswordTextInput;
    @BindView(R.id.acet_repassword)
    AppCompatEditText mRePasswordEditText;

    /**
     * 注册
     */
    @BindView(R.id.btn_register_ok)
    Button mBtnRegisterOk;
    /**
     * 注册取消
     */
    @BindView(R.id.btn_register_cancel)
    Button mBtnRegisterCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);
        ButterKnife.bind(this);

        initViews();
        initUserInfo();
    }

    private void initUserInfo() {
    }

    private void initViews() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTitle.setText("修改信息");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }
    @OnClick(R.id.btn_register_ok)
    public void onRegisterOk(View v) {
//        final String username = mUserEditText.getText().toString();
//        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");//a-z,A-Z,0-9,_,3～16位
//        if(!pattern.matcher(username).matches()) {
//            mUserTextInput.setError(getString(R.string.error_register_input_username_invalid));
//            return;
//        }
        final String nickname = mNicknameEditText.getText().toString();
        if (ValueUtil.isEmpty(nickname)) {
            mNicknameTextInput.setError(getString(R.string.error_register_input_nickname));
            return;
        }
        User user =  LoginHelper.getUser();
        register(user.getUsername(), nickname, user.getPassword());
    }

    public void register(final String username, String nickname, final String password) {

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("name", nickname);

        Observable.just(attributes)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Map<String,String>, Boolean>() {
                    @Override
                    public Boolean call(Map<String, String> attribute) {
                        return SmackManager.getInstance().registerUser(username, password, attribute);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {

                            UIUtil.showToast(ChangeInfoActivity.this, R.string.hint_register_success);
                            ActivityUtil.finishActivity(ChangeInfoActivity.this);
                        } else {
                            UIUtil.showToast(ChangeInfoActivity.this, R.string.hint_register_failure);
                        }
                    }
                });
    }

    private File avatarImageFile;
    private byte[] getAvatarBytes() {
        if (!avatarImageFile.exists()) return null;

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(avatarImageFile);
        } catch (FileNotFoundException e) {
        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }



    @OnClick(R.id.btn_register_cancel)
    public void onRegisterCancel(View v) {
        ActivityUtil.finishActivity(this);
    }
}
