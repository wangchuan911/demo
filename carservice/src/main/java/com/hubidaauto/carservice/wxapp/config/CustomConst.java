package com.hubidaauto.carservice.wxapp.config;

import com.hubidaauto.carservice.wxapp.entity.TacheVO;

import java.util.*;

public final class CustomConst {
	/*all*/
	public final static int ADD = 0,
			DELETE = 1,
			MODIFY = 2,
			GET = 3,
			LIST = 4;

	public final static class USER {
		public final static int GET_WORKERS = 100, AREA_RANGE = 101;
	}

	public final static class ORDER {
		public final static int GET_WORK_NUMBER = 100,
				APPIONT_WORKER = 101;

		public final static class STATE {
			public final static int RUNNING = 0,
					WAIT_NEXT = 1,
					END = 2,
					CANCEL = -1;
		}
	}

	public final static class TACHE {
		public final static int GET_WORK_NUMBER = 100;

		public final static class STATE {
			public final static int END = -2,
					WAIT = -1;
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

		public static Integer getOptionTache(List<String> tacheIds, final TacheVO newTacheVo) {
			if (tacheIds.stream().anyMatch(s ->
					s != null ?
							s.trim().equals(newTacheVo.getTacheId().toString().trim())
							: false
			)) {
				if (newTacheVo.getNextTache() >= 0)
					return getOptionTache(tacheIds, TACHE_MAP.get(newTacheVo.getNextTache()));
				else
					return newTacheVo.getNextTache();
			} else {
				return newTacheVo.getTacheId();
			}
		}

	}

	public final static class CAR {
		public final static int GET_MODEL = 100;
	}

	public final static class ROLE {

		public final static int GUEST = -1,
				CUSTOMER = 0,
				WOCKER = 1,
				DISTRIBUTOR = 2;
	}

	public final static class FUNCTION {
		public final static String DISTRIBUTE = "distribute",
				PAYMENT = "payment",
				FINISH = "finish";
	}

	public final static class OTHER {
		public final static int PRE_PAY = -2,
				LOGIN = -1;
	}

	public final static class COUPON {
		public final static int NEW_USER = 0,
				ORDER_10 = 1;

		public final static class TEMPLATE {
			public final static int NEW_USER = 1;
		}
	}

	public interface USER_RECORD {
		int ORDER = 0,
				LOCAL = 1;
	}

	public interface INVITE_CODE {
		char WORKER = 'W', VIP = 'V';

	}

}

