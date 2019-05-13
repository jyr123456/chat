package cn.ittiger.im.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.UUID;

import cn.ittiger.database.annotation.Column;
import cn.ittiger.database.annotation.PrimaryKey;
import cn.ittiger.database.annotation.Table;
import cn.ittiger.im.smack.SmackVCardHelper;

@Table(name = "UserProfile")
public class UserProfile implements Parcelable {
	public static final int TYPE_CONTACT = 1;   //好友
	public static final int TYPE_NOT_CONTACT = 2; //不是好友
	public static final int TYPE_MYSELF = 3; //自己
	public static final int TYPE_UNKNOWN = 4; //不存在

	@PrimaryKey(isAutoGenerate = true)
	private long id;
	@Column(columnName = "jid")
	private String jid;
	@Column(columnName = "nickname")
	private String nickname;
	@Column(columnName = "status")
	private String status;
	@Column(columnName = "avatar")
	private String avatar;
	
	private int type;

	public UserProfile() {

	}

	public UserProfile(String jid, String nickname) {
		this.jid = jid;
		this.nickname = nickname;
		type = TYPE_UNKNOWN;
	}
	
	
	public UserProfile(String jid, int type) {
		this.type = type;
		
	}
	private UserProfile(Parcel in) {
		nickname = in.readString();
		jid = in.readString();
		status = in.readString();
		type = in.readInt();
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUserName() {
		if (jid == null) {
			return null;
		} else {
			int var1 = jid.lastIndexOf("@");
			return var1 <= 0 ? "" : jid.substring(0, var1);
		}
	}


	public void markAsContact() {
		type = TYPE_CONTACT;
	}

	public boolean canAddToContact() {
		return type == TYPE_NOT_CONTACT;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof UserProfile)) {
			return false;
		}
		
		return jid.equals(((UserProfile)o).jid);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nickname);
		dest.writeString(jid);
		dest.writeString(status);
		dest.writeInt(type);
	}
	
	public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
		@Override
		public UserProfile createFromParcel(Parcel source) {
			return new UserProfile(source);
		}

		@Override
		public UserProfile[] newArray(int size) {
			return new UserProfile[size];
		}
	};
}