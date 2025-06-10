package com.antares.common.core.dto;

import java.io.Serializable;

import com.antares.common.core.enums.HttpCodeEnum;

import lombok.Data;

@Data
public class R<T> implements Serializable {
	private int code;
	private String msg;
	private T data;

	public R(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public static R<Void> ok() {
		return new R<>(HttpCodeEnum.SUCCESS.getCode(), HttpCodeEnum.SUCCESS.getMsg());
	}


	public static <T> R<T> ok(T data) {
		R<T> r = new R<>(HttpCodeEnum.SUCCESS.getCode(), HttpCodeEnum.SUCCESS.getMsg());
		r.setData(data);
		return r;
	}

	public static R<Void> error(HttpCodeEnum httpCodeEnum) {
		return new R<>(httpCodeEnum.getCode(), httpCodeEnum.getMsg());
	}

	public static R<Void> error(int code, String msg) {
		return new R<>(code, msg);
	}
}