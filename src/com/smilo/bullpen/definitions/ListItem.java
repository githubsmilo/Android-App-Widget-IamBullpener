package com.smilo.bullpen.definitions;

public final class ListItem {

    private String titlePrefix = Constants.DEFAULT_TITLE_PREFIX;
    private String title = Constants.DEFAULT_TITLE;
    private String writer = Constants.DEFAULT_WRITER;
    private String url = Constants.DEFAULT_URL;
    private int commentNum = Constants.DEFAULT_COMMENT_NUM;
    
    public ListItem(String titlePrefix, String title, String writer, String url, int commentNum) {
        this.titlePrefix = titlePrefix;
        this.title = title;
        this.writer = writer;
        this.url = url;
        this.commentNum = commentNum;
    }
    
    public String getTitlePrefix() {
        return titlePrefix;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getWriter() {
        return writer;
    }
    
    public String getUrl() {
        return url;
    }
    
    public int getCommentNum() {
        return commentNum;
    }
    
    public void setTitlePrefix(String titlePrefix) {
        this.titlePrefix = titlePrefix;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setWriter(String writer) {
        this.writer = writer;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }
    
    public String toString() {
        return ("titlePrefix[" + titlePrefix + "], title[" + title + "], writer[" + writer + "], url[" + url + "], commentNum[" + commentNum + "]");
    }
}
