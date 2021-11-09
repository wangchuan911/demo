package org.welisdoon.web.common;

public interface CommonConst {
    interface WebParamsKeys {
        String SPRING_BEAN = "A1",
                BEAN_METHOD = "A2",
                REQUSET_TYPE = "A3";

        String GET_CODE = "B1",
                GET_VALUE_1 = "B2";
    }

    interface WeChatPubValues {
        String SUCCESS = "SUCCESS",
                FAIL = "FAIL";
    }

    interface WecharUrlKeys {
        String SUBSCRIBE_SEND = "subscribeSend",
                ACCESS_TOKEN = "getAccessToken",
                CODE_2_SESSION = "code2Session",
                UNIFIED_ORDER = "unifiedorder",
                USER_INFO = "userInfo",
                CUSTOM_SEND = "customSend",
                REFUND = "refund";
    }
}
