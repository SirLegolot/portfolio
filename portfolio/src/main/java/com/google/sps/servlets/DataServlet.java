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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/** Servlet that returns comments stored in datastore.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  protected DatastoreService datastore;
  protected Gson gson;
  protected Query queryAscending;
  protected Query queryDescending;

  public DataServlet() {
    super();
    datastore = DatastoreServiceFactory.getDatastoreService();
    queryDescending = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    queryAscending = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);
    gson = new Gson();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Create a query to order the comments by date from datastore, either
    // ascending or descending.
    String sortOrder = request.getParameter("sortOrder");
    Query query = queryDescending;
    if (sortOrder != null && sortOrder.equals("ascending")) query = queryAscending;
    PreparedQuery pq = datastore.prepare(query);

    // Get specified number of comments. 
    String countString = request.getParameter("numComments");
    int count = -1;
    if (countString != null) count = Integer.parseInt(countString);
    
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
    // Get the URL of the image that the user uploaded to Blobstore.
    String imageURL = getUploadedFileUrl(request, "imageURL");

    // Creates Entity object.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", username);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("date", date);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("imageURL", imageURL);

    // Connects to the datastore and inserts the entity.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML forum page.
    response.sendRedirect("/forum.jsp");
  }

  // Helper functions/routines:

  // Creates a comment object from the inputs.
  private Comment createComment(Entity entity) {
    String username = entity.getProperty("username").toString();
    String content = entity.getProperty("content").toString();
    Object imageURLObject = entity.getProperty("imageURL");
    String imageURL = null;
    if (imageURLObject != null) imageURL = imageURLObject.toString();
    Date date = (Date) entity.getProperty("date");
    return (new Comment(username, content, imageURL, date));
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }
}
