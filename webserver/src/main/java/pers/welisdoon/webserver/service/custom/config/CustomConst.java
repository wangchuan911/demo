package pers.welisdoon.webserver.service.custom.config;

import java.util.*;
import java.util.stream.Collectors;

import pers.welisdoon.webserver.service.custom.entity.TacheVO;
import pers.welisdoon.webserver.service.custom.entity.UserVO;

public class CustomConst {

    /*all*/
    public final static int ADD = 0;
    public final static int DELETE = 1;
    public final static int MODIFY = 2;
    public final static int GET = 3;
    public final static int LIST = 4;

    public static class USER {
        public final static int GET_WORKERS = 100;
    }

    public static class ORDER {
        public final static int GET_WORK_NUMBER = 100;
        public final static int APPIONT_WORKER = 101;

        public static class STATE {
            public final static int RUNNING = 0;
            public final static int WAIT_NEXT = 1;
            public final static int END = 2;
        }
    }

    public static class TACHE {
        public final static int GET_WORK_NUMBER = 100;

        public static class STATE {
            public final static int END = -2;
            public final static int WAIT = -1;
        }

        public static final Map<Integer, TacheVO> TACHE_MAP = new HashMap<>();
        public static final List<TacheVO> TACHE_LIST = new LinkedList<>();
        public static TacheVO FIRST_TACHE;

        static void initTacheMapValue(List<TacheVO> tacheVOList) {
            TACHE_LIST.clear();
            TACHE_LIST.addAll(tacheVOList);
            FIRST_TACHE = tacheVOList.get(0);
            ergodic(tacheVOList, null);
        }

        private static void ergodic(List<TacheVO> tacheVOList, TacheVO superTache) {
            Iterator<TacheVO> iterator = tacheVOList.iterator();
            TacheVO pre = null;
            TacheVO cur = null;
            while (iterator.hasNext()) {
                cur = iterator.next();
                synchronized (TACHE_MAP) {
                    TACHE_MAP.put(cur.getTacheId(), cur);
                }
                if (pre != null) {
                    pre.setNextTache(cur.getTacheId());
                }
                TacheVO currnet = cur;
                if (cur.getTacheRelas() != null && cur.getTacheRelas().size() > 0) {
                    cur.getTacheRelas().forEach(tacheRela -> {
                        ergodic(tacheRela.getChildTaches(), currnet);
                    });
                }
                if (superTache != null) {
                    cur.setSuperTache(superTache.getTacheId());
                }
                pre = cur;
            }
            if (cur != null) {
                cur.setNextTache(STATE.END);
            }
        }


    }

    public static class CAR {
    }

    public static class ROLE {

        public final static int GUEST = -1;
        public final static int CUSTOMER = 0;
        public final static int WOCKER = 1;
        public final static int DISTRIBUTOR = 2;

        public static Map<Integer, Integer> ROLE_MAP;

        static void initRoleMapValue(List<UserVO.RoleConfig> roleConfigs) {
            ROLE_MAP = roleConfigs.stream()
                    .collect(Collectors
                            .toMap(UserVO.RoleConfig::getRoleId
                                    , UserVO.RoleConfig::getLevel));
        }
    }

    public static class FUNCTION {
        public final static String DISTRIBUTE = "distribute";
        public final static String PAYMENT = "payment";
        public final static String FINISH = "finish";
    }


}

