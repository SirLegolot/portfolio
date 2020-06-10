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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.sps.data.UserLibrary;

@WebServlet("/username")
public class UsernameServlet extends HttpServlet {

  protected DatastoreService datastore;
  protected UserService userService;

  public UsernameServlet() {
    super();
    datastore = DatastoreServiceFactory.getDatastoreService();
    userService = UserServiceFactory.getUserService();
  }
  
  // If user is logged in, provides a simple form to edit their user/nickname
  // If not, provides a link to log in.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<h1>Set Username</h1>");

    if (userService.isUserLoggedIn()) {
      // Get the username if it exists, otherwise return empty string.
      String userId = userService.getCurrentUser().getUserId();
      String username = UserLibrary.getUsername(userId, datastore, "");
      out.println("<p>Set your username here:</p>");
      out.println("<form method=\"POST\" action=\"/username\">");
      out.println("<input name=\"username\" value=\"" + username + "\" />");
      out.println("<br/>");
      out.println("<button>Submit</button>");
      out.println("</form>");
    } else {
      String loginUrl = userService.createLoginURL("/username");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // If not logged in, redirects user back to the form to login
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/username");
      return;
    }

    // Getting username and user id to store in datastore
    String username = request.getParameter("username");
    String id = userService.getCurrentUser().getUserId();

    Entity entity = new Entity("UserInfo", id);
    entity.setProperty("id", id);
    entity.setProperty("username", username);
    // The put() function automatically inserts new data or updates existing data based on ID
    datastore.put(entity);

    response.sendRedirect("/forum.jsp");
  }
}
