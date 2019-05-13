package cn.ittiger.im.smack;

import android.content.Context;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import cn.ittiger.im.R;

public class SmackVCardHelper {
	public static final String FIELD_STATUS = "status";
	
	private Context context;
	private XMPPConnection con;
	
	public SmackVCardHelper(Context context, XMPPConnection con) {
		this.context = context;
		this.con = con;
	}
	
	public void save(String nickname, byte[] avatar)  {
		VCard vCard = new VCard();
			vCard.setNickName(nickname);
			if (avatar != null) {
				vCard.setAvatar(avatar);
			}
			vCard.setField(FIELD_STATUS, context.getString(R.string.default_status));
		try {
			vCard.save(con);
		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPException.XMPPErrorException e) {
			e.printStackTrace();
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		}
	}
	
	public void saveStatus(String status) throws Exception {
		VCard vCard = loadVCard();
		vCard.setField(FIELD_STATUS, status);
		
		try {
			vCard.save(con);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	public String loadStatus() throws Exception {
		return loadVCard().getField(FIELD_STATUS);
	}
	
	public VCard loadVCard(String jid) throws Exception {
		VCard vCard = new VCard();
		try {
			vCard.load(con, jid);
			return vCard;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	public VCard loadVCard() throws Exception {
		VCard vCard = new VCard();
		try {
			vCard.load(con);
			return vCard;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
 }