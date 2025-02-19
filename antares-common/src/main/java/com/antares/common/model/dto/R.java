package com.antares.common.model.dto;

import java.io.Serializable;

import com.antares.common.model.enums.HttpCodeEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class R<T> implements Serializable {
	private static final long serialVersionUID = 6682215287252208284L;

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

	public static R<Void> error() {
		return new R<>(HttpCodeEnum.INTERNAL_SERVER_ERROR.getCode(), HttpCodeEnum.INTERNAL_SERVER_ERROR.getMsg());
	}


	public static R<Void> error(HttpCodeEnum HttpCodeEnum) {
		return new R<>(HttpCodeEnum.code, HttpCodeEnum.msg);
	}

	public static R<Void> error(int code, String msg) {
		return new R<>(code, msg);
	}
}