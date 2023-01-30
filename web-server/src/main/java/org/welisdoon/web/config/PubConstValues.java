package org.welisdoon.web.config;

import org.welisdoon.web.WebserverApplication;

public class PubConstValues {
    final static public Class MAIN_CLASS;
    final static public String BASE_PACKGE_NAME;


    static {
        MAIN_CLASS = WebserverApplication.class;
        BASE_PACKGE_NAME = MAIN_CLASS.getPackageName();
    }
}
