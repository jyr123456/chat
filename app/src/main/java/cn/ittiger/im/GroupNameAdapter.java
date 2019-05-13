package cn.ittiger.im;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.im.bean.FriendRooms;

public class GroupNameAdapter extends BaseAdapter {
    private List<FriendRooms> roomsList;

    private Context context;
    private LayoutInflater layoutInflater;

    public GroupNameAdapter(Context context, List<FriendRooms> roomsList) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.roomsList = roomsList;
    }

    @Override
    public int getCount() {
        return roomsList.size();
    }

    @Override
    public FriendRooms getItem(int position) {
        return roomsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.contact_item_view, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews(getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(FriendRooms bean, ViewHolder holder) {
        holder.contactAvatar.setBackgroundResource(R.drawable.vector_contact_focus);
        holder.contactName.setText(bean.getName());
    }

    static class ViewHolder {
        @BindView(R.id.contact_avatar)
        ImageView contactAvatar;
        @BindView(R.id.contact_name)
        TextView contactName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
