package com.hubidaauto.carservice.wxapp.core.entity;

import com.hubidaauto.carservice.wxapp.increment.entity.CouponVO;
import io.vertx.core.json.JsonObject;
import org.welisdoon.webserver.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.webserver.entity.wechat.user.WeChatUser;

import java.sql.Timestamp;
import java.util.List;

public class UserVO extends WeChatUser {
	//	private String id;
	private String name;
	private Integer role;
	private Integer maxRole;
	private String phone;
	private UserAttr userAttr;
	//	private String sessionKey;
	private boolean openData = true;
	//	private String unionid;
	private List<CouponVO> coupons;
	private WorkerStatus workerStatus;

	public UserVO() {
	}

	public UserVO(WeChatUser weChatUser) {
		this.setUnionid(weChatUser.getUnionid());
		this.setId(weChatUser.getId());
		this.setSessionKey(weChatUser.getSessionKey());
	}

	public String getId() {
		return super.getId();
	}

	public UserVO setId(String id) {
		super.setId(id);
		return this;
	}

	public String getName() {
		return name;
	}

	public UserVO setName(String name) {
		this.name = name;
		return this;
	}

	public Integer getRole() {
		return role;
	}

	public UserVO setRole(Integer role) {
		this.role = role;
		return this;
	}

	public String getPhone() {
		return phone;
	}

	public UserVO setPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public String getSessionKey() {
		return this.openData ? null : super.getSessionKey();
	}

	public UserVO setSessionKey(String sessionKey) {
		super.setSessionKey(sessionKey);
		return this;
	}

	public UserVO openData(boolean sw) {
		this.openData = sw;
		return this;
	}

	public UserAttr getUserAttr() {
		return userAttr;
	}

	public UserVO setUserAttr(UserAttr userAttr) {
		this.userAttr = userAttr;
		return this;
	}

	public Integer getMaxRole() {
		return maxRole;
	}

	public UserVO setMaxRole(Integer maxRole) {
		this.maxRole = maxRole;
		return this;
	}

	public String getUnionid() {
		return this.openData ? null : super.getUnionid();
	}

	public UserVO setUnionid(String unionid) {
		super.setUnionid(unionid);
		return this;
	}

	public static class UserAttr {
		private boolean vip;
		private Integer[] regionCode;
		Integer inviteCode;

		public boolean isVip() {
			return vip;
		}

		public UserAttr setVip(boolean vip) {
			this.vip = vip;
			return this;
		}

		public Integer[] getRegionCode() {
			return regionCode;
		}

		public UserAttr setRegionCode(Integer[] regionCode) {
			this.regionCode = regionCode;
			return this;
		}

		public Integer getInviteCode() {
			return inviteCode;
		}

		public UserAttr setInviteCode(Integer inviteCode) {
			this.inviteCode = inviteCode;
			return this;
		}
	}

	public List<CouponVO> getCoupons() {
		return coupons;
	}

	public void setCoupons(List<CouponVO> coupons) {
		this.coupons = coupons;
	}

	public WorkerStatus getWorkerStatus() {
		return workerStatus;
	}

	public void setWorkerStatus(WorkerStatus workerStatus) {
		this.workerStatus = workerStatus;
	}

	public static class WorkerStatus {
		private Integer orders;
		private Double posX;
		private Double posY;
		private Timestamp lastPosDate;

		public Double getPosX() {
			return posX;
		}

		public WorkerStatus setPosX(Double posX) {
			this.posX = posX;
			return this;
		}

		public Double getPosY() {
			return posY;
		}

		public WorkerStatus setPosY(Double posY) {
			this.posY = posY;
			return this;
		}

		public Timestamp getLastPosDate() {
			return lastPosDate;
		}

		public WorkerStatus setLastPosDate(Timestamp lastLocalDate) {
			this.lastPosDate = lastLocalDate;
			return this;
		}

		public Integer getOrders() {
			return orders;
		}

		public void setOrders(Integer orders) {
			this.orders = orders;
		}
	}

	public UserVO userDecrypted(String userEncryptedData, String useriv) throws Throwable {
		String json;
		JsonObject jsonObject = new JsonObject(json = WXBizMsgCrypt.wxDecrypt(userEncryptedData, this.getSessionKey(), useriv));
		this.setUnionid(jsonObject.getString("unionId", null));
		return this;
	}

	public UserVO phoneDecrypted(String phoneEncryptedData, String phoneEncryptedIv) throws Throwable {
		JsonObject jsonObject = new JsonObject(WXBizMsgCrypt.wxDecrypt(phoneEncryptedData, this.getSessionKey(), phoneEncryptedIv));
		this.phone = (jsonObject.getString("phoneNumber", jsonObject.getString("purePhoneNumber", null)));
		return this;
	}
}
