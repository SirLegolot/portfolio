package com.google.sps.data;

public class Account{

  private boolean isLoggedIn;
  private boolean isAdmin; 
  private String userEmail;
  private String loginLogoutURL; // Link to either login or logout page, depending on isLoggedIn.
  private String username;

  public Account (boolean isLoggedIn, boolean isAdmin, String userEmail, String loginLogoutURL, String username) {
    this.isLoggedIn = isLoggedIn;
    this.isAdmin = isAdmin;
    this.userEmail = userEmail;
    this.loginLogoutURL = loginLogoutURL;
    this.username = username;
  }
}