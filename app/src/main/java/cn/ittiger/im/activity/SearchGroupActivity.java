package cn.ittiger.im.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;

import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.base.BaseActivity;
import cn.ittiger.im.GroupNameAdapter;
import cn.ittiger.im.R;
import cn.ittiger.im.bean.FriendRooms;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.im.util.IMUtil;
import cn.ittiger.util.UIUtil;

public class SearchGroupActivity extends BaseActivity {
    @BindView(R.id.title_Bar)
    TitleBar titleBar;
    @BindView(R.id.btn_search)
    Button btnSearch;
    @BindView(R.id.line_search)
    LinearLayout lineSearch;
    @BindView(R.id.edit_search)
    EditText edit_search;
    @BindView(R.id.listview)
    ListView listview;
    GroupNameAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_group);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        titleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onLeftClick(View v) {
                finish();
            }

            @Override
            public void onTitleClick(View v) {

            }

            @Override
            public void onRightClick(View v) {

            }
        });

        btnSearch.setOnClickListener(v -> {
            if (edit_search.getText().toString().trim() == null || edit_search.getText().toString().trim().equals("")) {
                UIUtil.showToast(this, "请输入关键字进行搜索");
            } else {
                getGroupName(edit_search.getText().toString().trim());
            }
        });
    }


    public void getGroupName(String trim) {
        List<FriendRooms> roomsList = new ArrayList<>();
        try {
            List<FriendRooms> list = SmackManager.getInstance().getConferenceRoom();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getName().contains(trim)) {
                    roomsList.add(list.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new GroupNameAdapter(this, roomsList);
        listview.setAdapter(adapter);


        listview.setOnItemClickListener((parent, view, position, id) -> {
            int index = 0;
            MultiUserChat multiUserChat = SmackManager.getInstance().getMultiChat(roomsList.get(position).getJid());
            List<String> mdata = SmackManager.getInstance().findMulitUser(multiUserChat);
            for (int j = 0; j < mdata.size(); j++) {
                Log.i("------", SmackManager.getInstance().getAccountName());
                if (mdata.get(j).split("/")[1].equals(SmackManager.getInstance().getAccountName())) {
                    index++;
                }
                Log.i("--------", mdata.get(j));
            }
            if (index > 0) {
                IMUtil.startMultiChatActivity(SearchGroupActivity.this, multiUserChat);
            } else {
                //弹出一个对话框，包含同意和拒绝按钮
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchGroupActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否加入" + roomsList.get(position).getName() + "群组");
                //同意按钮监听事件，发送同意Presence包及添加对方为好友的申请
                builder.setPositiveButton("同意", (dialog, arg1) -> {
                    SmackManager.getInstance().joinChatRoom(roomsList.get(position).getName(), SmackManager.getInstance().getAccountName(), null);
                });
                //拒绝按钮监听事件，发送拒绝Presence包
                builder.setNegativeButton("我再想想", (dialog, arg1) -> {
                });
                builder.show();
            }
        });
    }
}
