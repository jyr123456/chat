package cn.ittiger.im.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import cn.ittiger.im.R;
import cn.ittiger.im.adapter.viewholder.ContactIndexViewHolder;
import cn.ittiger.im.adapter.viewholder.ContactViewHolder;
import cn.ittiger.im.bean.ContactEntity;
import cn.ittiger.im.smack.SmackManager;
import cn.ittiger.indexlist.adapter.IndexStickyViewAdapter;

import static cn.ittiger.im.smack.SmackManager.parseBareAddress;
import static cn.ittiger.im.smack.SmackManager.parseName;

/**
 * 联系人列表数据适配器
 */
public class ContactAdapter extends IndexStickyViewAdapter<ContactEntity> {
    private Context mContext;

    public ContactAdapter(Context context, List<ContactEntity> originalList) {

        super(originalList);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateIndexViewHolder(ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item_index_view, parent, false);
        return new ContactIndexViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item_view, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindIndexViewHolder(RecyclerView.ViewHolder holder, int position, String indexName) {

        ContactIndexViewHolder viewHolder = (ContactIndexViewHolder) holder;
        if (indexName == null) {
            viewHolder.getTextView().setText("");
        } else
            viewHolder.getTextView().setText(indexName);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position, ContactEntity itemData) {
        ContactViewHolder viewHolder = (ContactViewHolder) holder;
        viewHolder.getImageView().setImageResource(R.drawable.vector_contact_focus);
        if (itemData.getRosterEntry().getName() == null) {
            viewHolder.getTextView().setText("null");
        } else
            viewHolder.getTextView().setText(itemData.getRosterEntry().getName());


        String name = parseName(itemData.getRosterEntry().getName());
        String jid = null;
        if (name == null || name.trim().length() == 0) {
            jid = itemData.getRosterEntry().getName() + "@" + SmackManager.SERVER_NAME;
        } else {
            jid = parseBareAddress(itemData.getRosterEntry().getName());
        }

        Roster roster = Roster.getInstanceFor(SmackManager.getInstance().getConnection());
        Presence pr1 = roster.getPresence(jid);
        Presence pr2 = roster.getPresence(jid + "/Smack");
        String str1 = roster.getPresence(jid).getStatus();
        String str2 = roster.getPresence(jid + "/Smack").getStatus();
        if (roster.getPresence(jid + "/Smack").getStatus() == null){
            viewHolder.getTextViewStatus().setText("离线" );
        }else {
            viewHolder.getTextViewStatus().setText(roster.getPresence(jid + "/Smack").getStatus() );
        }

        viewHolder.getTextViewStatus().setVisibility(View.VISIBLE);

    }

    public static int IsUserOnLine(String strUrl) {
        int state = 0;
        //返回值 : 0 - 用户不存在; 1 - 用户在线; 2 - 用户离线
        try {
            URL oUrl = new URL(strUrl);
            URLConnection oConn = oUrl.openConnection();
            if (oConn != null) {
                BufferedReader oIn = new BufferedReader(new InputStreamReader(oConn.getInputStream()));
                if (null != oIn) {
                    String strFlag = oIn.readLine();
                    //System.out.println(strFlag);
                    oIn.close();
                    if (strFlag.indexOf("type=\"unavailable\"") >= 0) {
                        state = 2;
                    }
                    if (strFlag.indexOf("type=\"error\"") >= 0) {
                        state = 0;
                    } else if (strFlag.indexOf("priority") >= 0 || strFlag.indexOf("id=\"") >= 0) {
                        state = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

}
