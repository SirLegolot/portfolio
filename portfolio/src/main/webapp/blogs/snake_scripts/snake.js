class Snake {
  constructor () {
    this.x = 0;
    this.y = 0;
    this.xSpeed = 0;
    this.ySpeed = 0;
    this.total = 0;
    this.tail = [];
  }
  
  reset () {
    this.x = 0;
    this.y = 0;
    this.xSpeed = 0;
    this.ySpeed = 0;
    this.total = 0;
    this.tail = [];
  }
  
  draw () {
    
    ctx.fillStyle = "#FFFFFF";
    ctx.strokeStyle = "#3e3e3e";
    for (let i=0; i<this.tail.length; i++) {
      ctx.fillRect(this.tail[i].x,
        this.tail[i].y, scale, scale);
      ctx.strokeRect(this.tail[i].x,
        this.tail[i].y, scale, scale);
    }

    ctx.fillRect(this.x, this.y, scale, scale);
    ctx.strokeRect(this.x, this.y, scale, scale);
  }
  
  update () {
    for (let i=0; i<this.tail.length-1; i++) {
      this.tail[i] = this.tail[i+1];
    }

    this.tail[this.total - 1] =
      { x: this.x, y: this.y };
    
    this.x += this.xSpeed;
    this.y += this.ySpeed;
  }
  
  changeDirection () {
    let direction = moves.shift();
    switch(direction) {
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
  
  eat (fruit) {
    if (this.x === fruit.x &&
      this.y === fruit.y) {
      this.total++;
      return true;
    }

    return false;
  } 
  
  checkCollision () {
    if (this.x > canvas.width || this.y > canvas.height ||
        this.x < 0 || this.y < 0) {
      this.reset ();
    }
    for (let i=0; i<this.tail.length; i++) {
      if (this.x === this.tail[i].x &&
        this.y === this.tail[i].y) {
        this.reset();
      }
    }
  } 
}
