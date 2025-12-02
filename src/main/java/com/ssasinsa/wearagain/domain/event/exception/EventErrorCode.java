package com.ssasinsa.wearagain.domain.event.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum EventErrorCode implements ErrorCode {
    MISSING_REQUIRED_VALUE("E1001", "필수 입력 값이 누락되었습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_EVENT_PERIOD("E1002", "행사 기간이 유효하지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    OPTION_DEPTH_LIMIT_EXCEEDED("E1003", "옵션 트리 깊이가 허용 범위를 초과했습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_OPTION_STRUCTURE("E1004", "옵션 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_IMAGE_INFORMATION("E1005", "이미지 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    DUPLICATE_OPTION("E1006", "중복된 옵션이 존재합니다.", HttpStatus.CONFLICT.value()),
    EVENT_REGISTRATION_FORBIDDEN("E1007", "행사 등록 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    EVENT_REGISTRATION_FAILED("E1008", "행사 등록 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    IMAGE_UPLOAD_FAILED("E1009", "이미지 업로드 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    EVENT_NOT_FOUND("E1010", "행사를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    EVENT_NOT_OPEN("E1011", "행사를 신청할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST.value()),
    EVENT_OPTION_NOT_FOUND("E1012", "행사 옵션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    EVENT_ALREADY_APPLIED("E1013", "이미 신청한 옵션입니다.", HttpStatus.CONFLICT.value()),
    EVENT_CAPACITY_EXCEEDED("E1014", "신청 가능 인원이 초과되었습니다.", HttpStatus.CONFLICT.value()),
    EVENT_APPLICATION_NOT_FOUND("E1015", "신청 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    EVENT_APPLICATION_NOT_CANCELABLE("E1016", "취소할 수 없는 신청 상태입니다.", HttpStatus.CONFLICT.value()),
    INVALID_EVENT_QUERY("E1017", "행사 조회 요청이 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    EVENT_ALREADY_ARCHIVED("E1018", "이미 보관 처리된 행사입니다.", HttpStatus.CONFLICT.value()),
    EVENT_APPLICATION_ALREADY_PROCESSED("E1019", "이미 처리된 신청입니다.", HttpStatus.CONFLICT.value()),
    EVENT_ADMIN_NOT_FOUND("E1020", "행사 담당 관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    EVENT_UPDATE_FORBIDDEN("E1021", "행사 수정 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    EVENT_STATUS_UPDATE_FORBIDDEN("E1022", "행사 상태 변경 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    EVENT_STATUS_UPDATE_INVALID("E1023", "허용되지 않은 상태 전환입니다.", HttpStatus.CONFLICT.value()),
    EVENT_STAFF_CODE_FORBIDDEN("E1024", "스태프 코드를 발급할 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    EVENT_STAFF_CODE_NOT_ISSUED("E1025", "스태프 코드가 발급되지 않았습니다.", HttpStatus.NOT_FOUND.value()),
    EVENT_STAFF_CODE_INVALID("E1026", "유효하지 않은 스태프 코드입니다.", HttpStatus.FORBIDDEN.value()),
    EVENT_CHECKIN_TOKEN_NOT_FOUND("E1027", "체크인 토큰이 만료되었거나 존재하지 않습니다.", HttpStatus.GONE.value()),
    EVENT_CHECKIN_TOKEN_INVALID("E1028", "체크인 토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_EVENT_STATUS("E1029", "해당 행사 상태는 변경할 수 없습니다", HttpStatus.CONFLICT.value()),
    EVENT_APPROVAL_REQUEST_NOT_FOUND("E1030", "승인 요청 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    EVENT_CHECKIN_NOT_AVAILABLE("E1031", "종료된 행사에서는 체크인할 수 없습니다.", HttpStatus.CONFLICT.value()),
    EVENT_APPLICATION_CANCELED("E1032", "취소된 신청입니다.", HttpStatus.CONFLICT.value()),
    EVENT_APPLICATION_REJECTED("E1033", "승인 거절된 신청입니다.", HttpStatus.CONFLICT.value()),
    EVENT_OPTION_NOT_LEAF("E1034", "하위 옵션을 모두 선택한 경우에만 신청할 수 있습니다.", HttpStatus.BAD_REQUEST.value());

    private final String code;
    private final String message;
    private final int status;

    EventErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }
}
