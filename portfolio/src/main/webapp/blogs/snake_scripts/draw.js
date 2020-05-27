// Setup of constants and variables
const canvas = document.querySelector(".canvas");
const ctx = canvas.getContext("2d");
const scale = 20;
const rows = canvas.height / scale;
const columns = canvas.width / scale;
const moves = [];
var snake;
var fruit;

// Setup function automatically runs
(function setup() {
  snake = new Snake();
  fruit = new Fruit();
  fruit.pickLocation(snake);

  // Update the board every 1/10 of a second based on player inputs
  window.setInterval(() => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    fruit.draw();
    snake.changeDirection();
    snake.update();
    snake.draw();

    if (snake.eat(fruit)) {
      fruit.pickLocation(snake);
    }

    // Score board
    snake.checkCollision();
    document.querySelector('.score')
      .innerText = "Score: " + snake.total;

  }, 100);
}());

// Listens for arrow key presses and disables page scrolling in the browser
window.addEventListener('keydown', ((evt) => {
  if ([37, 38, 39, 40].indexOf(evt.keyCode) > -1) {
    evt.preventDefault();
  }
  const direction = evt.key.replace('Arrow', '');
  moves.push(direction);
}));