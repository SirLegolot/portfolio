// Setup of constants and variables
const canvas = document.querySelector(".canvas");
const ctx = canvas.getContext("2d");
const scale = 20;
const rows = canvas.height / scale;
const columns = canvas.width / scale;
const moves = [];

function updateFrame(snake, fruit) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  fruit.draw();
  snake.changeDirection();
  snake.update();
  snake.draw();
}

// Setup function creates snake and fruit objects, and updates frames
function setup() {
  const snake = new Snake();
  const fruit = new Fruit();
  fruit.pickLocation(snake);

  // Update the board every 1/10 of a second based on player inputs
  window.setInterval(() => {
    updateFrame(snake, fruit);

    // Check for eating fruit or collisions with wall/tail
    if (snake.eat(fruit)) {
      fruit.pickLocation(snake);
    }
    snake.checkCollision();

    // Score board
    document.querySelector('.score')
    .innerText = "Score: " + snake.total;

  }, 100);
}

// Run the setup function
setup ();

// Listens for arrow key presses and disables page scrolling in the browser
window.addEventListener('keydown', ((evt) => {
  if ([37, 38, 39, 40].indexOf(evt.keyCode) > -1) {
    evt.preventDefault();
  }
  // evt.key returns strings such as "ArrowUp", so we remove the "Arrow" part
  // to make it easier to understand
  const direction = evt.key.replace('Arrow', '');
  moves.push(direction);
}));

