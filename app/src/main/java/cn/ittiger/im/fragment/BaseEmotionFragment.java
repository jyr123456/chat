package cn.ittiger.im.fragment;

import android.support.v4.app.Fragment;
import android.widget.EditText;

import cn.ittiger.im.constant.EmotionType;

/**
 */
public class BaseEmotionFragment extends Fragment {
    protected EmotionType mEmotionType;
    protected EditText mEditText;

    public BaseEmotionFragment() {

    }

    public void setEmotionType(EmotionType emotionType) {

        mEmotionType = emotionType;
    }

    public void bindToEditText(EditText editText) {

        mEditText = editText;
    }
}
