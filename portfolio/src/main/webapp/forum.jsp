<%-- Create blobstore url to send images to. Also sets up forwarding to the
     form handler "/data". --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/data"); %>

<!DOCTYPE html>
<html>

<head>
  <meta charset="UTF-8">
  <title>Forum - Neel Gandhi</title>
  <link rel="stylesheet" href="style.css">
  <link rel=icon href=/images/favicon.ico>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
  <script src="script.js"></script>
</head>

<body onload="getComments()">
  <!-- Navigation Bar -->
  <div class="navbar" id="mynavbar">
    <a href="index.html">Home</a>
    <a href="projects.html">Portfolio</a>
    <a href="blog.html">Blog</a>
    <a href="forum.jsp" class="cur">Forum</a>
    <a href="photos.html">Photography</a>
    <a href="https://www.linkedin.com/in/neel-gandhi-5b7355148/"
       target="_blank" style="float:right">
      LinkedIn
    </a>
    <a href="https://github.com/SirLegolot" target="_blank" 
       style="float:right">
      GitHub
    </a>
    <a class="icon" onclick="navButton()">...</a>
  </div>

  <!-- Back to top button -->
  <a id="button" href="#">&#11165;</a>

  <!-- Blog cards -->
  <div id="content">

    <!-- Comment Section -->
    <div class="card">
      <div class="card-header">Comments 
        <a href="javascript:getComments()" class="reload"><b>&#x21bb;</b></a>
      </div>
      <div class="card-text">
        <!-- Choose how many comments to see on a page. -->
        <div class="commentOptions">
          <label for="numComments">Comments to display:</label>
          <select name="numComments" id="numComments" onchange="getComments()">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="15">15</option>
            <option value="20" selected>20</option>
            <option value="25">25</option>
            <option value="30">30</option>
            <option value="-1">All</option>
          </select>
        </div>
        <!-- Choose order in which to display comments. -->
        <div class="commentOptions">
          <label for="sortOrder">Sort by time:</label>
          <select name="sortOrder" id="sortOrder" onchange="getComments()">
            <option value="descending" selected>Newest first</option>
            <option value="ascending">Oldest first</option>
          </select>
        </div>

        <!-- Comments container is an unordered list. -->
        <ul id="commentThread"></ul>

        <button onclick="clearComments()">Clear Comments (Dev)</button>
      </div>
    </div>

    <!-- Leave a Comment form. -->
    <div class="card">
      <div class="card-header">Leave a Comment</div>
      <div class="card-text">
        <form action="<%= uploadUrl %>" method="POST" enctype="multipart/form-data">
          <label for="username">Username*</label><br/>
          <input type="text" name="username" id="username" required><br/><br/>
          <label for="txtbox">Comment*</label><br/>
          <textarea rows="5" name="content" id="txtbox" required></textarea>
          <br/><br/>
          <label for="imageURL">Add an image (optional)</label><br/>
          <input type="file" name="imageURL" id="imageURL" accept="image/*"><br/><br/>
          <input type="submit">
        </form>
      </div>
    </div>

  </div>

</body>

</html>
