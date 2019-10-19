package pers.welisdoon.webserver.config;

import pers.welisdoon.webserver.WebserverApplication;

public class PubConstValues {
    final static public Class MAIN_CLASS;
    final static public String BASE_PACKGE_NAME;


    static {
        MAIN_CLASS = WebserverApplication.class;
        BASE_PACKGE_NAME = MAIN_CLASS.getPackageName();
    }
}
