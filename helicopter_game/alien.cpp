

#include <cstdlib>
#include <math.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <iostream>

#include "xtools.h"
#include "alien.h"
#include "bomb.h"
#include "game.h"
#include "helicopter.h"
#include "explosion.h"
#include "aabullet.h"

Alien::Alien(float x, float y) : Entity() {
  this->x = x;
  this->y = y;
  w = Alien::WIDTH;
  h = Alien::HEIGHT;
  moveTimer = 0;
  shootTimer = 0;
}

void Alien::paint(XInfo &xinfo) {
  // Body
  XDrawArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx + 10), scaley*(y + scrolly + 8), scalex*(28), scaley*(12), 0, FULL_CIRCLE);
}

float Alien::getVX() {
  return vx - scrollvx;
}

float Alien::getVY() {
  return vy - scrollvy;
}

void Alien::update(float delta) {
  moveTimer += delta;
  vy = sin(moveTimer*5) * 200;

  if (shootTimer <= 0) {
    // Fire at player
    Helicopter *player = getPlayer();
    if (player) {
      float x = player->getX() + player->getW()/2.0f,
            y = player->getY() + player->getH()/2.0f,
            z = player->getVX(),
            w = player->getVY(),
            a = this->x + w/2,
            b = this->y,
            c = AABullet::SPEED;
      float bvx, bvy;
      if (solveInterception(x, y, z, w, a, b, c, bvx, bvy)) {
        AABullet *bullet = new AABullet(this->x + Turret::WIDTH/2, this->y, bvx, bvy);
        addEntity(bullet);
        shootTimer = 0.5f; // Only restore after actually shooting
      }
    }
  } else {
    shootTimer -= delta;
  }

  x += vx * delta;
  y += vy * delta;

  if (x + scrollx + w < 0) deleteEntity(this);

}


