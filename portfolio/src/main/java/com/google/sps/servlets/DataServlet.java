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
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import com.google.sps.data.Comment;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comments stored in datastore.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  protected DatastoreService datastore;
  protected Gson gson;
  protected Query query;

  public DataServlet() {
    super();
    datastore = DatastoreServiceFactory.getDatastoreService();
    query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    gson = new Gson();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Create a query to order the comments by date from datastore.
    PreparedQuery pq = datastore.prepare(query);

    // Get specified number of comments. 
    int count = Integer.parseInt(request.getParameter("numComments"));
    ArrayList<Comment> comments = new ArrayList<Comment>();
    
    // A positive count indicates that only that number of comments will be
    // retrieved. A negative count indicates to retrieve all comments.
    if (count > 0) {
      List<Entity> results = pq.asList(FetchOptions.Builder.withLimit(count));
      for (int i = 0; i < results.size(); i++) {
        comments.add(createComment(results.get(i)));
      }
    } else {
      for (Entity entity : pq.asIterable()) {
        comments.add(createComment(entity));
      }
    }
  
    // Convert comments ArrayLists to json format.
    String json = gson.toJson(comments);

    // Send the JSON as the response.
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Creates comment fields/metadata.
    String username = request.getParameter("username");
    String content = request.getParameter("content");
    Date date = new Date();
    long timestamp = System.currentTimeMillis();

    // Creates Entity object.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", username);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("date", date);
    commentEntity.setProperty("timestamp", timestamp);

    // Connects to the datastore and inserts the entity.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML forum page.
    response.sendRedirect("/forum.html");
  }

  public Comment createComment(Entity entity) {
    String username = entity.getProperty("username").toString();
    String content = entity.getProperty("content").toString();
    Date date = (Date) entity.getProperty("date");
    return (new Comment(username, content, date));
  }
}
