<%-- Create blobstore url to send images to. Also sets up forwarding to the
     form handler "/cloudVision". --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/cloudVision"); %>

<!DOCTYPE html>
<html>

<head>
	<meta charset="UTF-8">
	<title>Blog - Neel Gandhi</title>
	<link rel="stylesheet" href="/../style.css">
	<link rel=icon href=/../images/favicon.ico> 
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
	<script src="/../script.js"></script>
</head>

<body onload="getVision()">
	<!-- Navigation Bar -->
	<div class="navbar" id="mynavbar">
    <a href="/../index.html">Home</a>
    <a href="/../projects.html">Portfolio</a>
    <a href="/../blog.html" class="cur">Blog</a>
    <a href="/../forum.jsp">Forum</a>
    <a href="/../photos.html">Photography</a>
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

	<!-- Blog content -->
	<div id="content">
		<p>
      Hello! In this blog, I work with the google cloud vision api to get some
      cool information about the image such as labels, logos, dominant colors,
      and more! To use this, simply upload a picture you want analyzed and click
      submit. As of writing this blog, the api is kind of slow, so you may have
      to wait around 30 seconds to get a response.
    </p>
    <p><i>6/17/2020</i></p>

    <form action="<%= uploadUrl %>" method="POST" enctype="multipart/form-data">
      <label for="imageURL">Add an image*</label><br/>
      <input type="file" name="imageURL" id="imageURL" accept="image/*" required><br/><br/>
      <input type="submit">
    </form>

    <div id="cloudVisionBox" class="imageLabels"></div>
	</div>

</body>

</html>