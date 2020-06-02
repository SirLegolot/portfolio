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
  const selectEl = document.getElementById("numComments");
  const displayText = selectEl.options[selectEl.selectedIndex].value;
  const displayCount = parseInt(displayText, 10);

  fetch("/data").then(response => response.json()).then(commentList => {
    // Displays only the number of comments requested. If displayCount is 
    // negative, it will display all comments.
    if (displayCount > 0) {
      commentList = commentList.slice(0, 
                      Math.min(displayCount, commentList.length));
    }
    
    // Converts the list of comment objects into an html list.
    const commentThread = document.getElementById('comments');
    commentThread.innerText = '';
    commentList.forEach(comment => {
      commentThread.appendChild(createListElement(comment));
    });
  });
}

// Creates an <li> element containing text.
function createListElement(comment) {
  const liElement = document.createElement('li');
  liElement.innerHTML = comment.username + ": " + comment.content + 
                        "<br/><i>" + comment.date + "</i>";
  return liElement;
}