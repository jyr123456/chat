package cn.ittiger.im.activity.task;

import android.content.Context;
import android.util.Log;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.search.ReportedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.ittiger.im.bean.UserProfile;
import cn.ittiger.im.smack.SmackManager;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static cn.ittiger.im.smack.SmackManager.parseBareAddress;
import static cn.ittiger.im.smack.SmackManager.parseName;

public class SearchUserTask extends BaseAsyncTask<Void, Void, UserProfile> {
    private String username;

    public SearchUserTask(Response.Listener<UserProfile> listener, Context context, String username) {
        super(listener, context);

        this.username = username;
    }

    @Override
    protected Response<UserProfile> doInBackground(Void... params) {
        Context context = getContext();
        if (context != null) {
            try {
                String name = parseName(username);
                String jid = null;
                if (name == null || name.trim().length() == 0) {
                    jid = username + "@" + SmackManager.SERVER_NAME;
                } else {
                    jid = parseBareAddress(username);
                }

                UserProfile user = new UserProfile(jid, username);
                String Accountname = parseName(SmackManager.getInstance().getAccountName());
                String Accountjid = null;

                if (Accountname == null || Accountname.trim().length() == 0) {
                    Accountjid = SmackManager.getInstance().getAccountName() + "@" + SmackManager.getInstance().getConnection().getServiceName();
                } else {
                    Accountjid = parseBareAddress(Accountname);
                }

                List<ReportedData.Row> mlist = SmackManager.getInstance().searchUsers(username);

                if (mlist != null && mlist.size() > 0) {
                    if (jid.equals(Accountjid)) {
                        user.setType(UserProfile.TYPE_MYSELF);
                    } else {
                        isFriend(user,username);
                    }
                } else {
                    user.setType(UserProfile.TYPE_UNKNOWN);
                }
                return Response.success(user);
            } catch (Exception e) {
                Log.e("--------------", String.format("search user error %s", e.toString()));
                return Response.error(e);
            }
        } else {
            return null;
        }
    }

    public void isFriend(UserProfile user, final String username) {
        Set<RosterEntry> friends = SmackManager.getInstance().getAllFriends();
        int index = 0;
        if (friends != null && friends.size() > 0) {
            for (RosterEntry entry : friends) {
                if (entry.getName().contains(username)) {
                    index++;
                }
            }
            if (index > 0) {
                user.setType(UserProfile.TYPE_CONTACT);
            } else {
               user.setType(UserProfile.TYPE_NOT_CONTACT);
            }
        } else {
           user.setType(UserProfile.TYPE_NOT_CONTACT);
        }
    }
}