
#include <iostream>
#include <complex>
#include <math.h>

#include "turret.h"
#include "game.h"
#include "aabullet.h"
#include "helicopter.h"

const float Turret::SHOOT_TIME = 2.0;

float abs(float a) {
  return a < 0 ? -a : a;
}

Turret::Turret(float x, float y) {
  this->x = x;
  this->y = y;
  w = Turret::WIDTH;
  h = Turret::HEIGHT;
  shootTimer = 0;
}

// TODO - make look better
void Turret::paint(XInfo &xinfo) {
  XSetForeground(xinfo.display, xinfo.gc, xinfo.colours[GREEN].pixel);
  XDrawRectangle(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*(w), scaley*(h));
  XSetForeground(xinfo.display, xinfo.gc, whitePixel);
}

void Turret::update(float delta) {
  if (-scrollx > x + Turret::WIDTH) {
    deleteEntity(this);
  }

  if (shootTimer <= 0) {
    // Fire at player
    Helicopter *player = getPlayer();
    if (player) {
      float x = player->getX() + player->getW()/2.0f,
            y = player->getY() + player->getH()/2.0f,
            z = player->getVX(),
            w = player->getVY(),
            a = this->x + Turret::WIDTH/2,
            b = this->y,
            c = AABullet::SPEED;
      float bvx, bvy;
      if (solveInterception(x, y, z, w, a, b, c, bvx, bvy)) {
        AABullet *bullet = new AABullet(this->x + Turret::WIDTH/2, this->y, bvx, bvy);
        addEntity(bullet);
        shootTimer = Turret::SHOOT_TIME;
      }
    }
  } else {
    shootTimer -= delta;
  }
}

