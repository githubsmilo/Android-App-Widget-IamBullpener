package com.smilo.bullpen.definitions;

import android.appwidget.AppWidgetManager;

public final class ExtraItem {

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
    private int bgImageType = Constants.DEFAULT_BG_IMAGE_TYPE;
    private int textSizeType = Constants.DEFAULT_TEXT_SIZE_TYPE;

    public ExtraItem(int widgetId,
                                int pageNum,
                                int boardType,
                                int refreshType,
                                boolean isPermitMobileConnection,
                                String blackList,
                                String blockedWords,
                                int searchCategoryType,
                                int searchSubjectType,
                                String searchKeyword,
                                int bgImageType,
                                int textSizeType) {
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
        this.bgImageType = bgImageType;
        this.textSizeType = textSizeType;
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
    
    public int getBgImageType() {
        return bgImageType;
    }
    
    public int getTextSizeType() {
        return textSizeType;
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
    
    public void setBgImageType(int bgImageType) {
        this.bgImageType = bgImageType;
    }
    
    public void setTextSizeType(int textSizeType) {
        this.textSizeType = textSizeType;
    }

    public void update(ExtraItem newItem) {
        this.widgetId = newItem.getAppWidgetId();
        this.pageNum = newItem.getPageNum();
        this.boardType = newItem.getBoardType();
        this.refreshType = newItem.getRefreshTimeType();
        this.isPermitMobileConnection = newItem.getPermitMobileConnectionType();
        this.blackList = newItem.getBlackList();
        this.blockedWords = newItem.getBlockedWords();
        this.searchCategoryType = newItem.getSearchCategoryType();
        this.searchSubjectType = newItem.getSearchSubjectType();
        this.searchKeyword = newItem.getSearchKeyword();
        this.bgImageType = newItem.getBgImageType();
        this.textSizeType = newItem.getTextSizeType();
    }
    
    public String toString() {
        return ("appWidgetId[" + widgetId + "], pageNum[" + pageNum + "], boardType[" + boardType +
                "], refreshTimeType[" + refreshType + "], isPermitMobileConnectionType[" + isPermitMobileConnection +
                "], blackList[" + blackList + "], blockedWords[" + blockedWords + 
                "], searchCategoryType[" + searchCategoryType + "], searchSubjectType[" + searchSubjectType +
                "], searchKeyword[" + searchKeyword + "], bgImageType[" + bgImageType + "], textSizeType[" + textSizeType + "]");
    }
}
