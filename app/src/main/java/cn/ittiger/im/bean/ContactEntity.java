package cn.ittiger.im.bean;

import org.jivesoftware.smack.roster.RosterEntry;

import cn.ittiger.indexlist.entity.BaseEntity;

/**
 * 联系人实体
 */
public class ContactEntity implements BaseEntity {
    private RosterEntry mRosterEntry;

    public ContactEntity(RosterEntry rosterEntry) {

        mRosterEntry = rosterEntry;
    }

    @Override
    public String getIndexField() {

        return mRosterEntry.getUser();
    }

    public RosterEntry getRosterEntry() {

        return mRosterEntry;
    }
}
