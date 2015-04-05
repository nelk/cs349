
#include <cstdlib>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <iostream>

#include "xtools.h"
#include "helicopter.h"
#include "bomb.h"
#include "game.h"
#include "explosion.h"
#include "bullet.h"

//Pixmap Helicopter::pixmap;

Helicopter::Helicopter() : Entity() {
  w = Helicopter::WIDTH;
  h = Helicopter::HEIGHT;
  state = NORMAL;
  lives = 3;
  score = 0;
  shooting = false;
  bombing = false;
  bombTimer = 0;
  shootTimer = 0;
  respawnTimer = 0;
}

void Helicopter::setup(XInfo &xinfo) {
}

void Helicopter::paint(XInfo &xinfo) {
  // Body
  XDrawArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx + 10), scaley*(y + scrolly + 8), scalex*(28), scaley*(12), 0, FULL_CIRCLE);
  // Propeller
  XDrawArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx + 20), scaley*(y + scrolly + 0), scalex*(30), scaley*(4), 0, FULL_CIRCLE);
  // Tail
  XDrawArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly + 4), scalex*(8), scaley*(8), 0, FULL_CIRCLE);

  // Body-propeller line
  XDrawLine(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx + 34), scaley*(y + scrolly + 9), scalex*(x + scrollx + 35), scaley*(y + scrolly + 4));
  // Body-tail line
  XDrawLine(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx + 4), scaley*(y + scrolly + 8), scalex*(x + scrollx + 14), scaley*(y + scrolly + 12));

  // Respawn bubble
  if (state == RESPAWN) {
    XDrawArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*(w), scaley*(h), 0, FULL_CIRCLE);
  }

  // Bounding box
  //XDrawRectangle(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*w, scaley*h);
}

void Helicopter::spawn(float x, float y) {
  this->x = x;
  this->y = y;
  this->vx = 0;
  this->vy = 0;
}

void Helicopter::die() {
  if (state == NORMAL) {
    state = DEAD;
    lives--;
  }
}

void Helicopter::keyinput(int dx, int dy, bool pressed) {
  if (state == NORMAL || state == RESPAWN) {
    if (pressed && dx != 0) {
      this->vx = dx * Helicopter::SPEED_X;
    } else if (!pressed && ((this->vx > 0 && dx > 0) || (this->vx < 0 && dx < 0))) {
      this->vx = 0;
    }
    if (pressed && dy != 0) {
      this->vy = dy * Helicopter::SPEED_Y;
    } else if (!pressed && ((this->vy > 0 && dy > 0) || (this->vy < 0 && dy < 0))) {
      this->vy = 0;
    }
  }
}


float Helicopter::getVX() {
#ifdef SHOOT_TEST
  return vx;
#else
  return vx - scrollvx;
#endif
}

float Helicopter::getVY() {
#ifdef SHOOT_TEST
  return vy;
#else
  return vy - scrollvy;
#endif
}

void Helicopter::update(float delta) {
  if (state == DEAD) {
    vy += GRAVITY * delta;

    // Create helicopter debris
    addEntity(new Debris(x + Helicopter::WIDTH/2, y + Helicopter::HEIGHT/2, 3, 3, -50, rand()%200-100));

    // Randomly create explosions
    if (rand() % 5 == 0) {
      addEntity(new Explosion(x + rand() % (int)w, y + rand() % (int)h, rand() % 40 + 20));
    }

    // Respawn after hits certain depth
    if (y > WIN_HEIGHT * 1.5) {
      if (lives <= 0) {
        gameState = GAMEOVER;
      }
      state = RESPAWN;
      spawn(20, 200);
      respawnTimer = RESPAWN_RATE;
    }
  } else if (state == RESPAWN) {
    respawnTimer -= delta;
    if (respawnTimer <= 0) {
      respawnTimer = 0;
      state = NORMAL;
    }
  }

#ifdef SHOOT_TEST
  x += vx * delta;
  y += vy * delta;
#else
  // Helicopter moves along with scrolling
  x += (vx - scrollvx) * delta;
  y += (vy - scrollvy) * delta;
#endif

  if (state == NORMAL || state == RESPAWN) {
    // Prevent helicopter from leaving screen
    if (x + scrollx < 0) x = -scrollx;
    if (x + w + scrollx > WIN_WIDTH) x = WIN_WIDTH - scrollx - w;
    if (y + scrolly < 0) y = -scrolly;
    if (y + w + scrolly > WIN_HEIGHT) y = WIN_HEIGHT - scrolly - w;

    // Bombing
    if (bombing) {
      bombTimer -= delta;
      if (bombTimer <= 0) {
        bombTimer = Helicopter::BOMB_RATE;
        float xspeed = vx < 0 ? 0 : vx - scrollvx;
        Bomb *b = new Bomb(x + Helicopter::WIDTH/2, y + Helicopter::HEIGHT - 2, xspeed, vy - scrollvy);
        addEntity(b);
      }
    } else if (bombTimer > 0) {
      bombTimer -= delta;
    }

    // Shooting
    if (shooting) {
      shootTimer -= delta;
      if (shootTimer <= 0) {
        shootTimer = Helicopter::SHOOT_RATE;
        addEntity(new Bullet(x + w, y + h/2, -scrollvx + Bullet::SPEED, rand()%((int)SHOOT_SPREAD*10)/SHOOT_SPREAD-SHOOT_SPREAD/2));
      }
    } else if (shootTimer > 0) {
      shootTimer -= delta;
    }
  }
}

void Helicopter::setBombing(bool bombing) {
  this->bombing = bombing;
}

void Helicopter::setShooting(bool shooting) {
  this->shooting = shooting;
}

