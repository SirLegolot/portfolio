package com.google.sps.data;

import java.util.Date;

public class Comment {

  private String username;
  private String content;
  private String imageURL;
  private Date date;

  public Comment(String username, String content, String imageURL, Date date) {
    this.username = username;
    this.content = content;
    this.imageURL = imageURL;
    this.date = date;
  }
}