package cn.ittiger.im.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.im.R;

/**
 */
public class CreateMultiChatViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.contact_check)
    ImageView checkImageView;
    @BindView(R.id.contact_avatar)
    ImageView avatar;
    @BindView(R.id.contact_name)
    TextView name;

    public CreateMultiChatViewHolder(View itemView) {

        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public ImageView getCheckImageView() {

        return checkImageView;
    }

    public ImageView getImageView() {

        return avatar;
    }

    public TextView getTextView() {

        return name;
    }
}
