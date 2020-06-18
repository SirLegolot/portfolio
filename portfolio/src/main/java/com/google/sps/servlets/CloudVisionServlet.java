package com.google.sps.servlets;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Date;
import com.google.sps.data.Comment;
import com.google.sps.data.UserLibrary;
import com.google.sps.data.ImageLabel;
import com.google.sps.data.CloudVisionAnnotation;

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

import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.WebDetection.WebEntity;
import com.google.cloud.vision.v1.WebDetection.WebLabel;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;

import java.util.Random;
import java.math.BigDecimal; 


@WebServlet("/cloudVision")
public class CloudVisionServlet extends HttpServlet {

  protected Gson gson;
  protected BlobstoreService blobstoreService;
  protected ImagesService imagesService;
  protected String jsonResponse;
  protected List<Feature> allFeatures;

  public CloudVisionServlet() {
    super();
    gson = new Gson();
    blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    imagesService = ImagesServiceFactory.getImagesService();
    jsonResponse = null;
    Feature labelDetection = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    Feature logoDetection = Feature.newBuilder().setType(Feature.Type.LOGO_DETECTION).build();
    Feature textDetection = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
    Feature imageProperties = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build();
    Feature objectLocalization = Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build();
    Feature webDetection = Feature.newBuilder().setType(Feature.Type.WEB_DETECTION).build();
    allFeatures = Arrays.asList(labelDetection, logoDetection, textDetection, imageProperties, objectLocalization, webDetection);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
     // Send the JSON as the response.
    response.setContentType("application/json;");
    response.getWriter().println(jsonResponse);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the URL of the image that the user uploaded.
    BlobKey blobKey = getBlobKey(request, "imageURL");
    String imageURL = getUploadedFileUrl(blobKey);

    // Use blobKey to send a request to the cloud vision api. We are guaranteed 
    // that the client uploaded an image.
    byte[] blobBytes = getBlobBytes(blobKey);
    AnnotateImageResponse imageResponse;
    // try {
      
    // } catch (Exception e) {
    //   jsonResponse = null;
    //   response.sendRedirect("/blogs/vision.jsp");
    //   return;
    // }

    imageResponse = handleCloudVisionRequest(blobBytes);
    // Store the response and refresh the page
    jsonResponse = formatImageResponse(imageResponse, blobKey);
    response.sendRedirect("/blogs/vision.jsp");
  }

  // Sends a request to the cloud vision api and retrieves the response.
  private AnnotateImageResponse handleCloudVisionRequest(byte[] imageBytes) throws IOException {
    // Convert image bytes into proper image format.
    ByteString byteString = ByteString.copyFrom(imageBytes);
    Image image = Image.newBuilder().setContent(byteString).build();

    // Create a request with all the features desired.
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addAllFeatures(allFeatures).setImage(image).build();
    List<AnnotateImageRequest> requests = new ArrayList<>();
    requests.add(request);

    // Connect to cloud vision client and send request.
    ImageAnnotatorClient client = ImageAnnotatorClient.create();
    BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
    client.close();

    // Receive the reponse and handle potential errors.
    List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
    AnnotateImageResponse imageResponse = imageResponses.get(0);
    if (imageResponse.hasError()) {
      System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
      return null;
    }

    return imageResponse;
  }

  private String formatImageResponse(AnnotateImageResponse imageResponse, BlobKey blobKey) {
    // These are labels for the overall image based on what the vision api thinks.
    List<EntityAnnotation> labelAnnotations = imageResponse.getLabelAnnotationsList();

    // These are labels based on images that are similar from the web.
    WebDetection webDetection = imageResponse.getWebDetection();
    List<WebEntity> webLabelAnnotations = webDetection.getWebEntitiesList();
    List<WebLabel> webBestLabelAnnotations = webDetection.getBestGuessLabelsList();

    // Any text found within an image is contained here.
    List<EntityAnnotation> textAnnotations = imageResponse.getTextAnnotationsList();

    // Gets a list of dominant colors in the image.
    DominantColorsAnnotation colors = imageResponse.getImagePropertiesAnnotation().getDominantColors();
    List<ColorInfo> colorAnnotations = colors.getColorsList();

    // Gets a list of all objects found in the image.
    List<LocalizedObjectAnnotation> objectAnnotations = imageResponse.getLocalizedObjectAnnotationsList();

    // Finds any logos that are in the image.
    List<EntityAnnotation> logoAnnotations = imageResponse.getLogoAnnotationsList();

    List<ImageLabel> genericLabels = new ArrayList<>();
    for (EntityAnnotation label : labelAnnotations) {
      genericLabels.add(new ImageLabel(label.getDescription(), label.getScore()));
    }

    List<ImageLabel> webLabels = new ArrayList<>();
    for (WebEntity label : webLabelAnnotations) {
      webLabels.add(new ImageLabel(label.getDescription(), label.getScore()));
    }

    List<String> webBestLabels = new ArrayList<>();
    for (WebLabel label : webBestLabelAnnotations) {
      webBestLabels.add(label.getLabel());
    }

    List<String> textInImage = new ArrayList<>();
    for (EntityAnnotation textBlock : textAnnotations) {
      textInImage.add(textBlock.getDescription());
    }

    List<String> dominantColors = new ArrayList<>();
    for (ColorInfo color : colorAnnotations) {
      dominantColors.add(String.format("fraction: %f%nr: %f, g: %f, b: %f%n",
            color.getPixelFraction(),
            color.getColor().getRed(),
            color.getColor().getGreen(),
            color.getColor().getBlue()));
    }

    List<ImageLabel> objectsInImage = new ArrayList<>();
    for (LocalizedObjectAnnotation object : objectAnnotations) {
      objectsInImage.add(new ImageLabel(object.getName(), object.getScore()));
    }

    List<ImageLabel> logosInImage = new ArrayList<>();
    for (EntityAnnotation logo : logoAnnotations) {
      logosInImage.add(new ImageLabel(logo.getDescription(), logo.getScore()));
    }

    String imageURL = getUploadedFileUrl(blobKey);
    return gson.toJson(new CloudVisionAnnotation(imageURL,
                                                 genericLabels,
                                                 webLabels,
                                                 webBestLabels,
                                                 textInImage,
                                                 dominantColors,
                                                 objectsInImage, 
                                                 logosInImage));
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

}