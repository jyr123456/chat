package cn.ittiger.im.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ittiger.base.BaseActivity;
import cn.ittiger.im.R;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.util.ActivityUtil;
import cn.ittiger.util.UIUtil;

/**
 * 账号管理
 */
public class AccountMngActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbarTitle)
    TextView mToolbarTitle;


    /**
     * 注销登陆
     */
    @BindView(R.id.btn_logout)
    Button mBtnLogout;
    /**
     * 用户状态修改
     */
    @BindView(R.id.rg_user_state)
    RadioGroup mUserState;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_mng_layout);
        //
        ButterKnife.bind(this);

        //initViews();
        mUserState.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_user_online://在线
                    changeState(0);
//                    new UserIsOnlineTool(chatUser,AccountMngActivity.this);
                    break;
                case R.id.rb_user_busy://忙碌
                    changeState(2);
                    break;
                case R.id.rb_disconnect://断开连接
                    changeState(5);
                    break;
            }
        });
    }
    private void initViews() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTitle.setText("修改状态信息");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }
    public void changeState(int code) {
        if (SmackManager.getInstance().updateUserState(code)) {
            UIUtil.showToast(this, "修改状态成功");
        } else {
            UIUtil.showToast(this, "修改状态失败");
        }
    }

    public void disconnect() {
        if (SmackManager.getInstance().disconnect()) {
            ActivityUtil.finishActivity(this);
        } else {
            UIUtil.showToast(this, "断开连接失败");
        }
    }

    /**
     * 注销登陆
     *
     * @param v
     */
    @OnClick(R.id.btn_logout)
    public void onLogoutClick(View v) {
        if (SmackManager.getInstance().logout()) {
            ActivityUtil.finishActivity(this);
        } else {
            UIUtil.showToast(this, "注销失败");
        }
    }
}
