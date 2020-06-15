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
import com.google.sps.data.UserLibrary;
import com.google.sps.data.ImageLabel;

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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;

import java.util.Random;
import java.math.BigDecimal; 

/** Servlet that returns comments stored in datastore.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  protected DatastoreService datastore;
  protected Gson gson;
  protected Query queryAscending;
  protected Query queryDescending;
  protected BlobstoreService blobstoreService;
  protected ImagesService imagesService;
  protected UserService userService;

  public DataServlet() {
    super();
    datastore = DatastoreServiceFactory.getDatastoreService();
    queryDescending = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    queryAscending = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);
    gson = new Gson();
    blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    imagesService = ImagesServiceFactory.getImagesService();
    userService = UserServiceFactory.getUserService();
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
    int count;
    try {
      count = Integer.parseInt(countString);
    } catch (NumberFormatException e) {
      count = -1;
    }
    
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
    String userId = userService.getCurrentUser().getUserId();
    String userEmail = userService.getCurrentUser().getEmail();
    // Return the username if exists, otherwise use the email.
    String username = UserLibrary.getUsername(userId, datastore, userEmail);
    String content = request.getParameter("content");
    Date date = new Date();
    long timestamp = System.currentTimeMillis();

    // Get the URL of the image that the user uploaded to Blobstore.
    BlobKey blobKey = getBlobKey(request, "imageURL");
    String imageURL = getUploadedFileUrl(blobKey);

    // Get image labels, only if an image was uploaded by the user.
    String imageLabels = null;
    if (blobKey != null) {
      byte[] blobBytes = getBlobBytes(blobKey);
      // Looks like this is broken for now (permission errors and whatnot)
      // in the development server. I can't test it in production either because
      // imagesService is unavailable in the production environment due to 
      // permission errors as well. 
      // imageLabels = getImageLabels(blobBytes);

      // Insead, I will supply my own fake labels for testing:
      imageLabels = getDummyImageLabels(blobBytes);
    }
    
    // Creates Entity object.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", username);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("date", date);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("imageURL", imageURL);
    commentEntity.setProperty("imageLabels", imageLabels);

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
    Date date = (Date) entity.getProperty("date");

    // Image url and image labels could be null, so we have to check for this.
    Object imageURLObject = entity.getProperty("imageURL");
    Object imageLabelsObject = entity.getProperty("imageLabels");
    String imageURL = null;
    String imageLabels = null;
    if (imageURLObject != null) imageURL = imageURLObject.toString();
    if (imageLabelsObject != null) imageLabels = imageLabelsObject.toString();
    
    return (new Comment(username, content, imageURL, imageLabels, date));
  }

  /**
   * Returns the BlobKey that points to the file uploaded by the user, or null if the user didn't
   * upload a file.
   */
  private BlobKey getBlobKey(HttpServletRequest request, String formInputElementName) {
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a BlobKey. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so the BlobKey is empty. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    return blobKey;
  }

  /** Returns a URL that points to the uploaded file. */
  private String getUploadedFileUrl(BlobKey blobKey) {
    // In the case that the user did not upload any image, return null.
    if (blobKey == null) return null;
    // Attempt to use imagesService API, otherwise serve blob directly through
    // a servlet.
    try {
      ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
      // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
      // path to the image, rather than the path returned by imagesService which contains a host.
      try {
        URL url = new URL(imagesService.getServingUrl(options));
        return url.getPath();
      } catch (MalformedURLException e) {
        return imagesService.getServingUrl(options);
      }
    } catch (Exception e) {
      return "/serveBlobstoreImage?blobKey=" + blobKey.getKeyString();
    }
  }

  /**
   * Blobstore stores files as binary data. This function retrieves the binary data stored at the
   * BlobKey parameter.
   */
  private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
      byte[] b =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(b);

      // if we read fewer bytes than we requested, then we reached the end
      if (b.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }

  /**
   * Uses the Google Cloud Vision API to generate a list of labels that apply to the image
   * represented by the binary data stored in imgBytes.
   */
  private String getImageLabels(byte[] imgBytes) throws IOException {
    ByteString byteString = ByteString.copyFrom(imgBytes);
    Image image = Image.newBuilder().setContent(byteString).build();

    Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
    List<AnnotateImageRequest> requests = new ArrayList<>();
    requests.add(request);

    ImageAnnotatorClient client = ImageAnnotatorClient.create();
    BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
    client.close();
    List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
    AnnotateImageResponse imageResponse = imageResponses.get(0);

    if (imageResponse.hasError()) {
      System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
      return null;
    }

    return convertToImageLabels(imageResponse.getLabelAnnotationsList());
  }

  // Converts labels to a json format that is good for storage and sending.
  private String convertToImageLabels(List<EntityAnnotation> entityLabels) {
    List<ImageLabel> imageLabels = new ArrayList<>(); 
    for (EntityAnnotation label : entityLabels) {
      imageLabels.add(new ImageLabel(label.getDescription(), round(label.getScore(), 2)));
    }
    return gson.toJson(imageLabels);
  }

  private String getDummyImageLabels(byte[] imgBytes) {
    List<ImageLabel> dummyImageLabels = new ArrayList<>();
    // Will choose random descriptions from the following array.
    String[] descriptions = {"Cat", "Dog", "Car", "Skyscraper", "Wagon", 
                             "Woman", "Man", "Baby", "Octopus", "City", "Sky"};
    // Choose random number of labels to insert in dummy label array, between
    // 1 and 5.
    Random rand = new Random();
    int randomNum = rand.nextInt(5) + 1;
    for (int i = 0; i<randomNum; i++) {
      String randomDescription = descriptions[rand.nextInt(11)];
      float randomScore = round(rand.nextFloat(), 2);
      dummyImageLabels.add(new ImageLabel(randomDescription, randomScore));
    }
    return gson.toJson(dummyImageLabels);
  }

  // Rounding a float to decimal place.
  private float round(float d, int decimalPlace) {
    BigDecimal bd = new BigDecimal(Float.toString(d));
    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
    return bd.floatValue();
  }
}
