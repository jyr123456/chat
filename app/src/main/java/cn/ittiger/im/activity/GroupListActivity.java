package cn.ittiger.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

public class GroupListActivity extends BaseActivity {
    @BindView(R.id.title_Bar)
    TitleBar titleBar;
    @BindView(R.id.line_search)
    LinearLayout line_search;
    @BindView(R.id.listview)
    ListView listview;
    GroupNameAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        getGroupName();
        initView();
    }

    private void initView() {
        titleBar.setTitle("我的群组");
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
        line_search.setOnClickListener(v -> {
            Intent intent =  new Intent();
            intent.setClass(GroupListActivity.this,SearchGroupActivity.class);
            startActivity(intent);
        });
    }


    public void getGroupName() {
        List<FriendRooms> roomsList = new ArrayList<>();
        try {
            List<FriendRooms> list = SmackManager.getInstance().getConferenceRoom();
            for (int i = 0; i < list.size(); i++) {
                MultiUserChat multiUserChat = SmackManager.getInstance().getMultiChat(list.get(i).getJid());
                List<String> mdata = SmackManager.getInstance().findMulitUser(multiUserChat);
                for (int j = 0; j < mdata.size(); j++) {
                    Log.i("------", SmackManager.getInstance().getAccountName());
                    if (mdata.get(j).split("/")[1].equals(SmackManager.getInstance().getAccountName())) {
                        roomsList.add(list.get(i));
                    }
                    Log.i("--------", mdata.get(j));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new GroupNameAdapter(this, roomsList);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener((parent, view, position, id) -> {
            MultiUserChat multiUserChat = SmackManager.getInstance().getMultiChat(roomsList.get(position).getJid());
            IMUtil.startMultiChatActivity(GroupListActivity.this, multiUserChat);
        });
    }
}
