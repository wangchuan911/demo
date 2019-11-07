package pers.welisdoon.webserver.service.custom.config;

public class CustomConst {

    /*all*/
    public final static int ADD = 0;
    public final static int DELETE = 1;
    public final static int MODIFY = 2;
    public final static int GET = 3;
    public final static int LIST = 4;

    public static class USER {

    }

    public static class ORDER {
        public final static int GET_WORK_NUMBER = 10;

        public static class STATE {
            public final static int RUNNING = 0;
            public final static int WAIT_NEXT = 1;
            public final static int END = 2;
        }
    }

    public static class TACHE {
        public final static int GET_WORK_NUMBER = 10;

        public static class STATE {
            public final static int END = -2;
        }
    }

    public static class CAR {
    }

    public static class ROLE {
        public final static int CUSTOMER = 0;
        public final static int WOCKER = 1;
        public final static int DISTRIBUTOR = 2;
    }

    public static class FUNCTION {
        public final static String DISTRIBUTE = "distribute";
        public final static String PAYMENT = "payment";
        public final static String FINISH = "finish";
    }
}

