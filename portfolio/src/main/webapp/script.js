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
 * Adds a random greeting to the page.
 */
function addRandomFact() {
  const greetings =
      ['My favorite color is red!', 
       'I used to tap dance!', 
       'I still love building with Legos!', 
       'My favorite movie is Intersellar!',
       'In my free time, I love to play table tennis!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

// Button script
$(window).scroll(function() {
  // check if scrolled more than 100 pixels
  if ($(window).scrollTop() > 100) {
    $('#button').fadeIn();
  } else {
    $('#button').fadeOut();
  }
});

// When clicked, brings back to top
$(document).ready(function() {
  $("#button").click(function(event) {
    event.preventDefault(); // Stop default action when clicking a button
    $("html, body").animate({ scrollTop: 0 }, "slow");
  });
});

// Navigation button - expand/contract
function navButton() {
  var x = document.getElementById("mynavbar");
  if (x.className === "navbar") {
    x.className += " responsive";
  } else {
    x.className = "navbar";
  }
}

// Display comments as a bulleted list (will format later)
function getComments() {
  fetch("/data").then(response => response.json()).then(commentList => {
    // convert the list of comment objects into an html list
    const commentThread = document.getElementById('comments');
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