class Fruit {
  constructor() {
    this.x;
    this.y;
  }
  
  // Randomly pick a new location on the board, check if it is a valid position
  pickNew(snake) {
    this.x = (Math.floor(Math.random() * columns - 1) + 1) * scale;
    this.y = (Math.floor(Math.random() * rows - 1) + 1) * scale;
    for (let i=0; i<snake.tail.length; i++) {
      if (this.x === snake.tail[i].x && this.y === snake.tail[i].y) {
        return false;
      }
    }
    if (this.x === snake.x && this.y === snake.y) {
      return false;
    }
    return true;
  }

  // Continuously picks a new location until a valid pos is found for the fruit
  pickLocation(snake) {
    while (!this.pickNew(snake)) continue;
  }
  
  // Draw orange rectangle
  draw() {
    ctx.fillStyle = "#FF9800";
    ctx.fillRect(this.x, this.y, scale, scale)
  }
}