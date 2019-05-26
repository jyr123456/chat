package cn.ittiger.im.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.im.R;
import cn.ittiger.im.ui.recyclerview.ViewHolder;

/**
 */
public class ChatRecordViewHolder extends ViewHolder {
    @BindView(R.id.chat_friend_avatar)
    public ImageView avatar;
    @BindView(R.id.chat_friend_nickname)
    public TextView nickName;
    @BindView(R.id.chat_message)
    public TextView message;
    @BindView(R.id.chat_time)
    public TextView chatTime;
    @BindView(R.id.chat_message_count)
    public TextView messageCount;

    public ChatRecordViewHolder(View itemView) {

        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
