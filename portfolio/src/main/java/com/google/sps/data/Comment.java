package com.google.sps.data;

import java.util.Date;

public class Comment {

  private String username;
  private String content;
  private Date date;

  public Comment(String username, String content, Date date) {
    this.username = username;
    this.content = content;
    this.date = date;
  }
}