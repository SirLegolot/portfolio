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
import com.google.appengine.api.datastore.Key;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comments stored in datastore.*/
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  protected DatastoreService datastore;
  protected Query query;

  public DeleteDataServlet() {
    super();
    datastore = DatastoreServiceFactory.getDatastoreService();
    query = new Query("Person").setKeysOnly();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Create a query to retrieve the keys of all the comments.
    PreparedQuery results = datastore.prepare(query);

    // // Convert prepared query to a list of keys.
    // List<Key> keys = new ArrayList<Key>();
    // for (Key key : results.asIterable()) {
    //   keys.add(key);
    // }

    // List<Entity> blah = results.asList();

    // datastore.delete();
  }
}
