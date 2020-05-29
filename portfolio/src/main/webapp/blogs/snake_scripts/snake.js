class Snake {
  constructor() {
    this.x = scale;
    this.y = scale;
    this.xSpeed = 0;
    this.ySpeed = 0;
    this.total = 0; // length of the tail/number of fruits eaten
    this.tail = [];
  }

  // Game over -> restart snake position and length
  reset() {
    ctx.fillStyle = "#ff0000";
    ctx.strokeStyle = "#3e3e3e";
    ctx.fillRect(this.x, this.y, scale, scale);
    ctx.strokeRect(this.x, this.y, scale, scale);
    this.x = scale;
    this.y = scale;
    this.xSpeed = 0;
    this.ySpeed = 0;
    this.total = 0;
    this.tail = [];
  }

  // Draws a white rectangle with grey borders
  draw() {
    ctx.fillStyle = "#FFFFFF";
    ctx.strokeStyle = "#3e3e3e";
    for (let i = 0; i < this.tail.length; i++) {
      ctx.fillRect(this.tail[i].x, this.tail[i].y, scale, scale);
      ctx.strokeRect(this.tail[i].x, this.tail[i].y, scale, scale);
    }

    ctx.fillRect(this.x, this.y, scale, scale);
    ctx.strokeRect(this.x, this.y, scale, scale);
  }

  // Snake movement: Shifts array to the left by one, and updates the last 
  // position. Then moves the head.
  update() {
    for (let i = 0; i < this.tail.length - 1; i++) {
      this.tail[i] = this.tail[i + 1];
    }

    this.tail[this.total - 1] =
      { x: this.x, y: this.y };

    this.x += this.xSpeed;
    this.y += this.ySpeed;
  }

  // Changes direction based on inputs. Prevents movement in the opposite 
  // direction (ex: cannot turn right when moving left), except for when the 
  // length of the tail is 0. In that case, since the snake is simply one box,
  // It can go in all directions.
  changeDirection() {
    let direction = moves.shift();
    switch (direction) {
      case 'Up':
        if (this.total > 0 && this.ySpeed > 0) break;
        this.xSpeed = 0;
        this.ySpeed = -scale;
        break;
      case 'Down':
        if (this.total > 0 && this.ySpeed < 0) break;
        this.xSpeed = 0;
        this.ySpeed = scale;
        break;
      case 'Left':
        if (this.total > 0 && this.xSpeed > 0) break;
        this.xSpeed = -scale;
        this.ySpeed = 0;
        break;
      case 'Right':
        if (this.total > 0 && this.xSpeed < 0) break;
        this.xSpeed = scale;
        this.ySpeed = 0;
        break;
      default:
        break;
    }
  }

  // Check if collided with fruit
  eat(fruit) {
    if (this.x === fruit.x && this.y === fruit.y) {
      this.total++;
      return true;
    }
    return false;
  }

  // Check if colliding with the wall or colliding with the tail
  checkCollision() {
    if (this.x > canvas.width || this.y > canvas.height ||
      this.x < 0 || this.y < 0) {
      this.reset();
    }
    for (let i = 0; i < this.tail.length; i++) {
      if (this.x === this.tail[i].x &&
        this.y === this.tail[i].y) {
        this.reset();
      }
    }
  }
}
