package com.smilo.bullpen;

public final class ListItem {

    private String title = null;
    private String writer = null;
    private String url = null;
    
    public ListItem(String title, String writer, String url) {
        this.title = title;
        this.writer = writer;
        this.url = url;
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
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setWriter(String writer) {
        this.writer = writer;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String toString() {
        return ("title[" + title + "], writer[" + writer + "], url[" + url + "]");
    }

}
