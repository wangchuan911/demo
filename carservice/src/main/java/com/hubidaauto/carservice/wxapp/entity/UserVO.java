package com.hubidaauto.carservice.wxapp.entity;

import java.sql.Timestamp;
import java.util.List;

public class UserVO {
	private String id;
	private String name;
	private Integer role;
	private Integer maxRole;
	private String phone;
	private UserAttr userAttr;
	private String sessionKey;
	private boolean openData = true;
	private String unionid;
	private List<CouponVO> coupons;
	private WorkerStatus workerStatus;

	public String getId() {
		return id;
	}

	public UserVO setId(String id) {
		this.id = id;
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
		return this.openData ? null : sessionKey;
	}

	public UserVO setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
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
		return this.openData ? null : unionid;
	}

	public UserVO setUnionid(String unionid) {
		this.unionid = unionid;
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
}
