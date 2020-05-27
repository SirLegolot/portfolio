class Fruit {
  constructor() {
    this.x;
    this.y;
  }
  
  pickNew (snake) {
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

  pickLocation(snake) {
    while (!this.pickNew(snake)) continue;
  }
  
  draw() {
    ctx.fillStyle = "#4cafab";
    ctx.fillRect(this.x, this.y, scale, scale)
  }
}