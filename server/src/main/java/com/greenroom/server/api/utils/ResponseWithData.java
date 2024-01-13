package com.greenroom.server.api.utils;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class ResponseWithData {

    private ResponseBuilder response;
    private Object data;

    @Builder
    public ResponseWithData(ResponseBuilder response, Object data) {
        this.response = response;
        this.data = data;
    }

    //data가 없는 success
    public static ResponseWithData success() {
        return success(null);
    }

    //data가 있는 success
    public static ResponseWithData success(Object data) {
        return process(ResponseCodeEnum.SUCCESS, data);
    }

    //failed
    public static ResponseWithData failed(ResponseCodeEnum responseCodeEnum) {
        return process(responseCodeEnum, null);
    }

    public static ResponseWithData failed(ResponseCodeEnum responseCodeEnum, Object data) {
        return process(responseCodeEnum, data);
    }

    //return 처리
    public static ResponseWithData process(ResponseCodeEnum responseCodeEnum, Object data) {
        return ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(responseCodeEnum.getCode())
                        .result(responseCodeEnum.getMessage())
                        .build())
                .data(data)
                .build();
    }

    //메세지 추가
    public ResponseWithData message(String message) {
        this.response.setResult(message);
        return this;
    }
}
