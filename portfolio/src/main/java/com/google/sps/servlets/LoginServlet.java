// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.Account;
import com.google.gson.Gson;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  protected UserService userService;
  protected Gson gson;
  private boolean isLoggedIn;
  private boolean isAdmin;
  private String userEmail;
  private String loginLogoutURL;
  private String username;
  private Account account;

  public LoginServlet() {
    super();
    userService = UserServiceFactory.getUserService();
    gson = new Gson();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Retrieving account information.
    if (userService.isUserLoggedIn()) {
      isLoggedIn = true;
      isAdmin = userService.isUserAdmin();
      userEmail = userService.getCurrentUser().getEmail();
      loginLogoutURL = userService.createLogoutURL("/forum.jsp");
      username = null; // TODO: implement username functionality
    } else {
      isLoggedIn = false;
      isAdmin = false;
      userEmail = null;
      loginLogoutURL = userService.createLoginURL("/forum.jsp");
      username = null; 
    }

    // Convert Account object to json format.
    account = new Account(isLoggedIn, isAdmin, userEmail, loginLogoutURL, username);
    String json = gson.toJson(account);

    // Send the JSON as the response.
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
