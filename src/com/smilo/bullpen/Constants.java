
package com.smilo.bullpen;

public final class Constants {

    public static final String PACKAGE_NAME = "com.smilo.bullpen";

    public static final boolean DEBUG_MODE = true;
    
    // Actions predefined by Google
    public static final String ACTION_APPWIDGET_ENABLD = "android.appwidget.action.APPWIDGET_ENABLED";
    public static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    
    // Actions defined by appWidget
    public static final String ACTION_INIT_LIST = PACKAGE_NAME + ".INIT_LIST";
    public static final String ACTION_REFRESH_LIST = PACKAGE_NAME + ".REFRESH_LIST";
    public static final String ACTION_SHOW_LIST = PACKAGE_NAME + ".SHOW_LIST";
    public static final String ACTION_SHOW_ITEM = PACKAGE_NAME + ".SHOW_ITEM";
    public static final String ACTION_UPDATE_ITEM_URL = PACKAGE_NAME + ".UPDATE_ITEM_URL";
    public static final String ACTION_UPDATE_LIST_URL = PACKAGE_NAME + ".UPDATE_LIST_URL";
    public static final String ACTION_SEARCH = PACKAGE_NAME + ".SEARCH";
    public static final String EXTRA_PAGE_NUM = PACKAGE_NAME + ".EXTRA_PAGE_NUM";
    public static final String EXTRA_BOARD_TYPE = PACKAGE_NAME + ".BOARD_TYPE";
    public static final String EXTRA_REFRESH_TIME_TYPE = PACKAGE_NAME + ".REFRESH_TIME_TYPE";
    public static final String EXTRA_PERMIT_MOBILE_CONNECTION_TYPE = PACKAGE_NAME + ".PERMIT_MOBILE_CONNECTION";
    public static final String EXTRA_ITEM_URL = PACKAGE_NAME + ".EXTRA_ITEM_URL";
    public static final String EXTRA_SEARCH_CATEGORY_TYPE = PACKAGE_NAME + ".SEARCH_CATEGORY_TYPE";
    public static final String EXTRA_SEARCH_SUBJECT_TYPE = PACKAGE_NAME + ".SEARCH_SUBJECT_TYPE";
    public static final String EXTRA_SEARCH_KEYWORD = PACKAGE_NAME + ".SEARCH_KEYWORD";
    
    public static final int LISTVIEW_MAX_ITEM_COUNT = 20;
    public static final int BITMAP_MAX_SIZE = 800;
    
    // Intentitem default values
    public static final int     DEFAULT_PAGE_NUM = 1;
    public static final int     DEFAULT_REFRESH_TIME_TYPE = 0;
    public static final int     DEFAULT_BOARD_TYPE = 0;
    public static final boolean DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE = false;
    
    // Intentitem error values
    public static final int     ERROR_PAGE_NUM = -1;
    public static final int     ERROR_REFRESH_TIME_TYPE = -1;
    public static final int     ERROR_BOARD_TYPE = -1;
    public static final boolean ERROR_PERMIT_MOBILE_CONNECTION_TYPE = false;
    
    public enum PARSING_RESULT {
    	SUCCESS_FULL_BOARD,
        SUCCESS_MOBILE_BOARD,
        SUCCESS_MOBILE_TODAY_BEST,
        FAILED_IO_EXCEPTION,
        FAILED_JSON_EXCEPTION,
        FAILED_STACK_OVERFLOW,
        FAILED_UNKNOWN,
    };
    
    // Search activity
    public static final int DEFAULT_SEARCH_CATEGORY_TYPE = 0;
    public static final int DEFAULT_SEARCH_SUBJECT_TYPE = 0;
    public static final int ERROR_SEARCH_CAGETORY_TYPE = -1;
    public static final int ERROR_SEARCH_SUBJECT_TYPE = -1;
    
    public static final String URL_BASE = "http://mlbpark.donga.com";
    public static final String URL_MLB_TOWN = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=mlbtown&cpage=";
    public static final String URL_KBO_TOWN = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=kbotown&cpage=";
    public static final String URL_BULLPEN = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&cpage=";
    public static final String URL_MLB_TOWN_TODAY_BEST = URL_BASE + "/MLBTOWN_TODAYBEST";
    public static final String URL_KBO_TOWN_TODAY_BEST = URL_BASE + "/KBOTOWN_TODAYBEST";
    public static final String URL_BULLPEN_TODAY_BEST = URL_BASE + "/BULLPEN_TODAYBEST";
    public static final String URL_BULLPEN_1000 = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&mbsW=search&select=hit&opt=1&keyword=1000&cpage=";
    public static final String URL_BULLPEN_2000 = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&mbsW=search&select=hit&opt=1&keyword=2000&cpage=";
    
    public static final int BOARD_TYPE_MLB_TOWN = 0;
    public static final int BOARD_TYPE_KBO_TOWN = 1;
    public static final int BOARD_TYPE_BULLPEN  = 2;
    public static final int BOARD_TYPE_MLB_TOWN_TODAY_BEST = 3;
    public static final int BOARD_TYPE_KBO_TOWN_TODAY_BEST = 4;
    public static final int BOARD_TYPE_BULLPEN_TODAY_BEST  = 5;
    public static final int BOARD_TYPE_BULLPEN_1000 = 6;
    public static final int BOARD_TYPE_BULLPEN_2000 = 7;
    
    public static final int DEFAULT_INTERVAL = 20000;
    public static final int TIME_1_MIN  = 60000;
    public static final int TIME_5_MIN  = 60000 * 5;
    public static final int TIME_10_MIN = 60000 * 10;
    public static final int TIME_20_MIN = 60000 * 20;
    public static final int TIME_30_MIN = 60000 * 30;
    public static final int TIME_STOP   = -1;
    
    public static final int REFRESH_TIME_TYPE_1_MIN  = 0;
    public static final int REFRESH_TIME_TYPE_5_MIN  = 1;
    public static final int REFRESH_TIME_TYPE_10_MIN = 2;
    public static final int REFRESH_TIME_TYPE_20_MIN = 3;
    public static final int REFRESH_TIME_TYPE_30_MIN = 4;
    public static final int REFRESH_TIME_TYPE_STOP   = 5;
    
    public static final int SEARCH_CATEGORY_TYPE_TITLE = 0;
    public static final int SEARCH_CATEGORY_TYPE_TITLE_CONTENTS = 1;
    public static final int SEARCH_CATEGORY_TYPE_ID = 2;
    public static final int SEARCH_CATEGORY_TYPE_WRITER = 3;
    public static final int SEARCH_CATEGORY_TYPE_SUBJECT = 4;
    public static final int SEARCH_CATEGORY_TYPE_HITS = 5;
    
    public static final int SEARCH_SUBJECT_TYPE_0 = 0; // 정치
    public static final int SEARCH_SUBJECT_TYPE_1 = 1; // 19금
    public static final int SEARCH_SUBJECT_TYPE_2 = 2; // 단문
    public static final int SEARCH_SUBJECT_TYPE_3 = 3; // 펌글
    public static final int SEARCH_SUBJECT_TYPE_4 = 4; // 게임
    public static final int SEARCH_SUBJECT_TYPE_5 = 5; // 질문
    public static final int SEARCH_SUBJECT_TYPE_6 = 6; // 17금
    public static final int SEARCH_SUBJECT_TYPE_7 = 7; // 음악
    public static final int SEARCH_SUBJECT_TYPE_8 = 8; // 응원
    public static final int SEARCH_SUBJECT_TYPE_9 = 9; // COB
    public static final int SEARCH_SUBJECT_TYPE_10 = 10; // 뻘글
    public static final int SEARCH_SUBJECT_TYPE_11 = 11; // SK
    public static final int SEARCH_SUBJECT_TYPE_12 = 12; // 두산
    public static final int SEARCH_SUBJECT_TYPE_13 = 13; // 롯데
    public static final int SEARCH_SUBJECT_TYPE_14 = 14; // 삼성
    public static final int SEARCH_SUBJECT_TYPE_15 = 15; // 한화
    public static final int SEARCH_SUBJECT_TYPE_16 = 16; // KIA
    public static final int SEARCH_SUBJECT_TYPE_17 = 17; // 넥센
    public static final int SEARCH_SUBJECT_TYPE_18 = 18; // LG
    public static final int SEARCH_SUBJECT_TYPE_19 = 19; // NC
    public static final int SEARCH_SUBJECT_TYPE_20 = 20; // 후기
    public static final int SEARCH_SUBJECT_TYPE_21 = 21; // 채팅
    public static final int SEARCH_SUBJECT_TYPE_22 = 22; // 짤방
    public static final int SEARCH_SUBJECT_TYPE_23 = 23; // 경제
    public static final int SEARCH_SUBJECT_TYPE_24 = 24; // 아이돌
}
