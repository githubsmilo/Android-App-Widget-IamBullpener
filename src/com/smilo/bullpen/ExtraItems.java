package com.smilo.bullpen;

import android.appwidget.AppWidgetManager;

public final class ExtraItems {

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int pageNum = Constants.DEFAULT_PAGE_NUM;
    private int boardType = Constants.DEFAULT_BOARD_TYPE;
    private int refreshType = Constants.DEFAULT_REFRESH_TIME_TYPE;
    private boolean isPermitMobileConnection = Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE;
    private String blackList = Constants.DEFAULT_BLACK_LIST;
    private String blockedWords = Constants.DEFAULT_BLOCKED_WORDS;
    private int searchCategoryType = Constants.DEFAULT_SEARCH_CATEGORY_TYPE;
    private int searchSubjectType = Constants.DEFAULT_SEARCH_SUBJECT_TYPE;
    private String searchKeyword = null;

    ExtraItems(int widgetId,
                                int pageNum,
                                int boardType,
                                int refreshType,
                                boolean isPermitMobileConnection,
                                String blackList,
                                String blockedWords,
                                int searchCategoryType,
                                int searchSubjectType,
                                String searchKeyword) {
        this.widgetId = widgetId;
        this.pageNum = pageNum;
        this.boardType = boardType;
        this.refreshType = refreshType;
        this.isPermitMobileConnection = isPermitMobileConnection;
        this.blackList = blackList;
        this.blockedWords = blockedWords;
        this.searchCategoryType = searchCategoryType;
        this.searchSubjectType = searchSubjectType;
        this.searchKeyword = searchKeyword;
    }
    
    public int getAppWidgetId() {
        return widgetId;
    }
    
    public int getPageNum() {
        return pageNum;
    }
    
    public int getBoardType() {
        return boardType;
    }
    
    public int getRefreshTimeType() {
        return refreshType;
    }
    
    public boolean getPermitMobileConnectionType() {
        return isPermitMobileConnection;
    }
    
    public String getBlackList() {
        return blackList;
    }
    
    public String getBlockedWords() {
        return blockedWords;
    }
    
    public int getSearchCategoryType() {
        return searchCategoryType;
    }
    
    public int getSearchSubjectType() {
        return searchSubjectType;
    }
    
    public String getSearchKeyword() {
        return searchKeyword;
    }
    
    public void setAppWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }
    
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
    
    public void setBoardType(int boardType) {
        this.boardType = boardType;
    }
    
    public void setRefreshTimeType(int refreshType) {
        this.refreshType = refreshType;
    }
    
    public void setPermitMobileConnectionType(boolean isPermitMobileConnection) {
        this.isPermitMobileConnection = isPermitMobileConnection;
    }
    
    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }
    
    public void setBlockedWords(String blockedWords) {
        this.blockedWords = blockedWords;
    }
    
    public void setSearchCategoryType(int searchCategoryType) {
        this.searchCategoryType = searchCategoryType;
    }
    
    public void setSearchSubjectType(int SearchSubjectType) {
        this.searchSubjectType = SearchSubjectType;
    }
    
    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public void update(ExtraItems newItem) {
        widgetId = newItem.getAppWidgetId();
        pageNum = newItem.getPageNum();
        boardType = newItem.getBoardType();
        refreshType = newItem.getRefreshTimeType();
        isPermitMobileConnection = newItem.getPermitMobileConnectionType();
        blackList = newItem.getBlackList();
        blockedWords = newItem.getBlockedWords();
        searchCategoryType = newItem.getSearchCategoryType();
        searchSubjectType = newItem.getSearchSubjectType();
        searchKeyword = newItem.getSearchKeyword();
    }
    
    public String toString() {
        return ("appWidgetId[" + widgetId + "], pageNum[" + pageNum + "], boardType[" + boardType +
                "], refreshTimeType[" + refreshType + "], isPermitMobileConnectionType[" + isPermitMobileConnection +
                "], blackList[" + blackList + "], blockedWords[" + blockedWords + 
                "], searchCategoryType[" + searchCategoryType + "], searchSubjectType[" + searchSubjectType +
                "], searchKeyword[" + searchKeyword + "]");
    }
}
