const canvas = document.querySelector(".canvas");
const ctx = canvas.getContext("2d");
const scale = 20;
const rows = canvas.height / scale;
const columns = canvas.width / scale;
const moves = [];
var snake;
var fruit;


(function setup() {
  snake = new Snake();
  fruit = new Fruit();
  fruit.pickLocation(snake);

  window.setInterval(() => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    fruit.draw();
    snake.changeDirection();
    snake.update();
    snake.draw();

    if (snake.eat(fruit)) {
      fruit.pickLocation(snake);
    }

    snake.checkCollision();
    document.querySelector('.score')
      .innerText = "Score: " + snake.total;

  }, 100);
}());

window.addEventListener('keydown', ((evt) => {
  const direction = evt.key.replace('Arrow', '');
  moves.push(direction);
}));