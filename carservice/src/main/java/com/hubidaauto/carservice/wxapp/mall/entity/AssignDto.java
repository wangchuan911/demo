package com.hubidaauto.carservice.wxapp.mall.entity;

import com.hubidaauto.carservice.wxapp.mall.common.AbstractAutoAssign;

import java.util.HashMap;
import java.util.Map;

public abstract class AssignDto {
	final static Map<String, Comp> ASSIGN_DTO_MAP = new HashMap<>();


	public static Class<? extends AssignDto> getAssgignType(String key) {
		return ASSIGN_DTO_MAP.get(key).assign;
	}

	public static Class<? extends AbstractAutoAssign> getAutoAssignType(String key) {
		return ASSIGN_DTO_MAP.get(key).autoAssign;
	}

	public static class Comp {
		Class<? extends AbstractAutoAssign> autoAssign;
		Class<? extends AssignDto> assign;
	}
}
