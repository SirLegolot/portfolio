package com.google.sps.data;

import java.util.Date;

public class Comment {

  private String username;
  private String content;
  private Date timeStamp;

  public Comment(String username, String content, Date timeStamp) {
    this.username = username;
    this.content = content;
    this.timeStamp = timeStamp;
  }
}