package com.google.sps.data;

public class Account{

  private boolean loginStatus; // True indicates user is logged in.
  private String userEmail;
  private String loginLogoutURL; // Link to either login or logout page, depending on loginStatus.
  private String username;

  public Account (boolean loginStatus, String userEmail, String loginLogoutURL, String username) {
    this.loginStatus = loginStatus;
    this.userEmail = userEmail;
    this.loginLogoutURL = loginLogoutURL;
    this.username = username;
  }
}