package pers.welisdoon.webserver.service.custom.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

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

        public static Map<Integer, TacheVO> TACHE_MAP = new HashMap<>();
        public static TacheVO FIRST_TACHE;

        static void initTacheMapValue(List<TacheVO> tacheVOList) {
            FIRST_TACHE = tacheVOList.get(0);
            ergodic(tacheVOList);
        }

        private static void ergodic(List<TacheVO> tacheVOList) {
            Iterator<TacheVO> iterator = tacheVOList.iterator();
            TacheVO pre = null;
            TacheVO cur = null;
            while (iterator.hasNext()) {
                cur = iterator.next();
                synchronized (TACHE_MAP) {
                    TACHE_MAP.put(cur.getTacheId(), cur);
                }
                if (pre != null) {
                    pre.setNextTache(cur);
                }
                if (!CollectionUtils.isEmpty(cur.getTacheRelas())) {
                    cur.getTacheRelas().forEach(tacheRela -> {
                        ergodic(tacheRela.getChildTaches());
                    });
                }
                pre = cur;
            }
            if (cur != null) {
                cur.setNextTache(new TacheVO().setTacheId(STATE.END));
            }
        }


    }

    public static class CAR {
    }

    public static class ROLE {
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

