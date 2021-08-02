package com.hubidaauto.carservice.wxapp.mall.entity;

import com.hubidaauto.carservice.wxapp.mall.common.AbstractAutoAssign;
import com.hubidaauto.carservice.wxapp.mall.common.annotation.AutoAssign;
import io.vertx.core.json.JsonObject;
import org.reflections.Reflections;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class MallDto {
	Integer id;
	String name;
	String desc;
	Integer price;
	Boolean visable;
	String protoType;
	String protoTypeConfig;
	AssignDto assignDto;


	public Integer getId() {
		return id;
	}

	public MallDto setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public MallDto setName(String name) {
		this.name = name;
		return this;
	}

	public String getDesc() {
		return desc;
	}

	public MallDto setDesc(String desc) {
		this.desc = desc;
		return this;
	}

	public Integer getPrice() {
		return price;
	}

	public MallDto setPrice(Integer price) {
		this.price = price;
		return this;
	}

	public Boolean getVisable() {
		return visable;
	}

	public MallDto setVisable(Boolean visable) {
		this.visable = visable;
		return this;
	}

	public String getProtoType() {
		return protoType;
	}

	public MallDto setProtoType(String protoType) {
		this.protoType = protoType;
		return this;
	}

	public String getProtoTypeConfig() {
		return this.protoTypeConfig;
	}

	public MallDto setProtoTypeConfig(String protoTypeConfig) {
		this.protoTypeConfig = protoTypeConfig;
		return this;
	}

	public AssignDto assignDto() {
		if (this.assignDto != null) return this.assignDto;
		Class<?> typeClass = null;
		if (AssignDto.ASSIGN_DTO_MAP.containsKey(this.protoType)) {
			typeClass = AssignDto.ASSIGN_DTO_MAP.get(this.protoType).assign;
		} else {
			synchronized (AssignDto.ASSIGN_DTO_MAP) {
				if (AssignDto.ASSIGN_DTO_MAP.containsKey(this.protoType)) {
					typeClass = AssignDto.ASSIGN_DTO_MAP.get(this.protoType).assign;
				} else {
					Optional<Class<?>> optional = ApplicationContextProvider.getBean(Reflections.class)
							.getTypesAnnotatedWith(AutoAssign.class)
							.stream()
							.filter(aClass -> {
								AutoAssign annotation = aClass.getAnnotation(AutoAssign.class);
								return !(annotation == null || !this.protoType.equals(annotation.name()));
							}).findFirst();
					if (optional.isPresent()) {
						typeClass = assignDtoType(optional.get());
						AssignDto.Comp comp = new AssignDto.Comp();
						comp.assign = (Class<? extends AssignDto>) typeClass;
						comp.autoAssign = (Class<? extends AbstractAutoAssign>) optional.get();
						AssignDto.ASSIGN_DTO_MAP.put(this.protoType, comp);
					}
				}
			}
		}
		if (typeClass != null) {
			this.assignDto = (AssignDto) new JsonObject(protoTypeConfig)
					.mapTo(typeClass);
		}

		return this.assignDto;
	}

	private Class<?> checkType(Type type, int index) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type t = pt.getActualTypeArguments()[index];
			return checkType(t, index);
		} else {
			String className = type == null ? "null" : type.getClass().getName();
			throw new IllegalArgumentException("Expected a Class, ParameterizedType"
					+ ", but <" + type + "> is of type " + className);
		}
	}

	private Class<?> assignDtoType(Class<?> aClass) {
		Type type = aClass.getGenericSuperclass();
		if (type == null) return null;
		if (((ParameterizedType) type).getRawType() != AbstractAutoAssign.class)
			return null;
		type = ((ParameterizedType) type).getActualTypeArguments()[0];
		return checkType(type, 0);
	}
}
