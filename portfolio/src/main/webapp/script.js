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

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['My favorite color is red!', 
       'I used to tap dance!', 
       'I still love building with Legos!', 
       'My favorite movie is Intersellar!',
       'In my free time, I love to play table tennis!'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = fact;
}

// Check if scrolled more than 100 pixels to decide whether to show the button.
$(window).scroll(function() {
  if ($(window).scrollTop() > 100) {
    $('#button').fadeIn();
  } else {
    $('#button').fadeOut();
  }
});

// When the button is clicked, the page scrolls to tht top.
$(document).ready(function() {
  $("#button").click(function(event) {
    // Stops the default action when clicking a button.
    event.preventDefault(); 
    $("html, body").animate({ scrollTop: 0 }, "slow");
  });
});

// The navigation button expands and contracts the menu.
function navButton() {
  let x = document.getElementById("mynavbar");
  if (x.className === "navbar") {
    x.className += " responsive";
  } else {
    x.className = "navbar";
  }
}

// Displays comments as a bulleted list (will format later).
function getComments() {

  // Determines the number of comments to be displayed from the select box.
  const countEl = document.getElementById("numComments");
  const displayCount = countEl.options[countEl.selectedIndex].value;

  // Determines order in which to display comments with respect to time.
  const sortEl = document.getElementById("sortOrder");
  const sortOrder = sortEl.options[sortEl.selectedIndex].value;
  const queryString = "/data?numComments=" + displayCount +
                      "&sortOrder=" + sortOrder;

  fetch(queryString).then(response => response.json()).then(commentList => { 
    // Converts the list of comment objects into an html list.
    const commentThread = document.getElementById('commentThread');
    commentThread.innerText = '';
    commentList.forEach(comment => {
      commentThread.appendChild(createListElement(comment));
    });
  });
}

// Creates an <li> element containing text.
function createListElement(comment) {
  const li = document.createElement('li');
  li.setAttribute('class', 'comment');
  const div = document.createElement('div');
  div.setAttribute('class', 'comment-content');

  // Comment content contains an avatar, header, and paragraph text.
  const img = document.createElement('img');
  img.setAttribute('class', 'avatar');
  img.setAttribute('src', '/images/profile.jpg');
  img.setAttribute('width', '40px');
  img.setAttribute('height', '40px');
  img.setAttribute('alt', 'profile_photo');
  const header = document.createElement('header');
  header.innerHTML = "<span class='userlink'>" + comment.username + "</span>" + 
                     " - <span class='pubdate'>" + comment.date + "</span>"
  const p = document.createElement('p');
  p.innerText = comment.content;
  
  // Add optional image, if user uploaded one.
  const uploaded = comment.imageURL != null;
  const imgUpload = document.createElement('img');
  const imgLink = document.createElement('a');
  const imgLabelsBox = document.createElement('div');
  imgLabelsBox.setAttribute('class', 'imageLabels');
  const imgLabelTitle = document.createElement('label');
  imgLabelTitle.innerText = "Cloud Vision generated labels with confidence level:"
  imgLabelTitle.setAttribute('for', 'imageLabelsList');
  imgLabelsBox.appendChild(imgLabelTitle);
  const imgLabels = document.createElement('ul');
  imgLabels.setAttribute('id', 'imageLabelsList');
  if (uploaded) {
    imgLink.setAttribute('href', comment.imageURL);
    imgLink.setAttribute('target', '_blank');
    imgUpload.setAttribute('src', comment.imageURL);
    imgUpload.setAttribute('width', '300px');
    imgUpload.setAttribute('alt', 'Uploaded_image');
    imgLink.appendChild(imgUpload);
    addImageLabels(comment, imgLabels);
    imgLabelsBox.appendChild(imgLabels);
  }

  // Adding all the components that make up a comment.
  li.appendChild(img);
  div.appendChild(header);
  div.appendChild(p);
  if (uploaded) {
    div.appendChild(imgLink);
    div.appendChild(imgLabelsBox);
  }
  li.appendChild(div);
  return li;
}

// Adds labels as list elements at the end of a comment.
function addImageLabels(comment, imgLabels) {
  const labels = JSON.parse(comment.imageLabels);
  for (i = 0; i < labels.length; i++) {
    let label = labels[i];
    let newLabel = document.createElement('li');
    newLabel.innerText = label.description + ": " + label.score;
    imgLabels.appendChild(newLabel);
  }
}

// Deletes all comments from datastore and refreshes.
function clearComments() {
  fetch("/delete-data", {method: 'POST'}).then(() => getComments());
}

// Refresh page causes the comments and login status to refresh.
function refresh() {
  getComments();
  getLogin();
}

// Sets the greeting at the top of the page depending on whether user is 
// logged in or not.
function getLogin() {
  const loginInfo = document.getElementById('loginInfo');
  
  fetch('/login').then(response => response.json()).then(account => { 
    // Default greeting prompts user to log in to comment.
    let greeting = "Hello stranger! Please log in to comment.";
    const loginLogoutURL = account.loginLogoutURL;
    let linkText = "Log In";
    // If user is not logged in, commentBox does not show.
    const commentBox = document.getElementById('commentBox');
    commentBox.style.display = "none";

    // If user is not an admin, the delete comments button is not shown.
    const deleteButton = document.getElementById('deleteButton');
    deleteButton.style.display = "none";

    // If user is not logged in, do not display seetings link
    let settingsLink = "";

    // If user is logged in, sets appropriate greeting and shows comment box.
    if (account.isLoggedIn) {
      greeting = "Welcome, ";
      if (account.username != null) greeting += account.username;
      else greeting += account.userEmail;
      linkText = "Log Out";
      commentBox.style.display = "block";
      settingsLink = "<a class='login' href='/username'>Settings |&nbsp;</a>"

      // If user is an admin, allow the user to delete all the comments
      if (account.isAdmin) deleteButton.style.display = "block";
    }

    // Display the user greeting.
    loginInfo.innerHTML = greeting + "<a class='login' href='" + 
                          loginLogoutURL + "'>" + linkText + "</a>" +
                          settingsLink;
  });
}

function getVision() {
  fetch("/cloudVision").then(response => response.json()).then(annotation => {
    const cloudVisionBox = document.getElementById('cloudVisionBox');
    if (annotation == null) {
      cloudVisionBox.innerText = "No image uploaded yet.";
      return;
    }
    const genericLabels = annotation.genericLabels;
    const webLabels = annotation.webLabels;
    const webBestLabels = annotation.webBestLabels;
    const textInImage = annotation.textInImage;
    const dominantColors = annotation.dominantColors;
    const objectsInImage = annotation.objectsInImage;
    const logosInImage = annotation.logosInImage;

    const label0 = "Image that was uploaded:";
    const label1 = "Generic Labels:";
    const label2 = "Labels based on similar images from the web:";
    const label3 = "Best guess label:";
    const label4 = "OCR/Text character recognition:";
    const label5 = "Dominant colors:";
    const label6 = "Object detection:";
    const label7 = "Logos:";
    const p0 = document.createElement('p');
    const p1 = document.createElement('p');
    const p2 = document.createElement('p');
    const p3 = document.createElement('p');
    const p4 = document.createElement('p');
    const p5 = document.createElement('p');
    const p6 = document.createElement('p');
    const p7 = document.createElement('p');
    p0.innerText = label0;
    p1.innerText = label1;
    p2.innerText = label2;
    p3.innerText = label3;
    p4.innerText = label4;
    p5.innerText = label5;
    p6.innerText = label6;
    p7.innerText = label7;

    const image = document.createElement('img');
    image.src = annotation.imageURL;
    image.width = 300;
    image.alt = "uploaded_image";
    const list1 = document.createElement('ul');
    const list2 = document.createElement('ul');
    const list3 = document.createElement('ul');
    const list4 = document.createElement('ul');
    const list5 = document.createElement('ul');
    const list6 = document.createElement('ul');
    const list7 = document.createElement('ul');

    addLabelsWithScore(genericLabels, list1);
    addLabelsWithScore(webLabels, list2);
    addLabels(webBestLabels, list3);
    addLabels(textInImage, list4);
    addLabelsWithScore(dominantColors, list5);
    addLabelsWithScore(objectsInImage, list6);
    addLabelsWithScore(logosInImage, list7);
    
    cloudVisionBox.appendChild(p0);
    cloudVisionBox.appendChild(image);
    cloudVisionBox.appendChild(p1);
    cloudVisionBox.appendChild(list1);
    cloudVisionBox.appendChild(p2);
    cloudVisionBox.appendChild(list2);
    cloudVisionBox.appendChild(p3);
    cloudVisionBox.appendChild(list3);
    cloudVisionBox.appendChild(p4);
    cloudVisionBox.appendChild(list4);
    cloudVisionBox.appendChild(p5);
    cloudVisionBox.appendChild(list5);
    cloudVisionBox.appendChild(p6);
    cloudVisionBox.appendChild(list6);
    cloudVisionBox.appendChild(p7);
    cloudVisionBox.appendChild(list7);
  });
}

function addLabelsWithScore(labels, ul) {
  labels.forEach(label => {
    const newLabel = document.createElement('li');
    newLabel.innerText = label.description + ": " + label.score;
    ul.appendChild(newLabel);
  });
}

function addLabels(labels, ul) {
  labels.forEach(label => {
    const newLabel = document.createElement('li');
    newLabel.innerText = label;
    ul.appendChild(newLabel);
  })
}