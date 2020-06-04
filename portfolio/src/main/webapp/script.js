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
  
  // Adding all the components that make up a comment.
  li.appendChild(img);
  div.appendChild(header);
  div.appendChild(p);
  li.appendChild(div);
  return li;
}

// Deletes all comments from datastore and refreshes.
function clearComments() {
  fetch("/delete-data", {method: 'POST'}).then(() => getComments());
}