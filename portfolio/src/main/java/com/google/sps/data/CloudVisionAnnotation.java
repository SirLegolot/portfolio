package com.google.sps.data;
import java.util.List;

public class CloudVisionAnnotation {
  private String imageURL;
  private List<ImageLabel> genericLabels;
  private List<ImageLabel> webLabels;
  private List<String> webBestLabels;
  private List<String> textInImage;
  private List<ImageLabel> dominantColors;
  private List<ImageLabel> objectsInImage;
  private List<ImageLabel> logosInImage;


  public CloudVisionAnnotation(String imageURL,
                               List<ImageLabel> genericLabels,
                               List<ImageLabel> webLabels,
                               List<String> webBestLabels,
                               List<String> textInImage,
                               List<ImageLabel> dominantColors,
                               List<ImageLabel> objectsInImage,
                               List<ImageLabel> logosInImage) {
    this.imageURL = imageURL;
    this.genericLabels = genericLabels;
    this.webLabels = webLabels;
    this.webBestLabels = webBestLabels;
    this.textInImage = textInImage;
    this.dominantColors = dominantColors;
    this.objectsInImage = objectsInImage;
    this.logosInImage = logosInImage;
  }
}