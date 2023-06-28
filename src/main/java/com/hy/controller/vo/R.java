package com.hy.controller.vo;

import lombok.Data;

/**
 * ==================
 * 统一前端返回格式    ｜
 * ==================
 *
 * @author shiwentao
 * @package com.hy.controller.vo
 * @create 2023/6/21 16:30
 **/
@Data
public class R<T> {

    private int code;
    private String result;
    private T msg;

    public R(int code, String result, T msg) {
        this.code = code;
        this.result = result;
        this.msg = msg;
    }

    public static <T> R<T> success(T msg) {
        return new R<>(200, "ok", msg);
    }

    public static <T> R<T> failure(T msg) {
        return new R<>(500, "fail", msg);
    }


}

