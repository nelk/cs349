
#include <iostream>

#include "bullet.h"
#include "game.h"

Bullet::Bullet(float x, float y, float vx, float vy) {
  this->x = x;
  this->y = y;
  this->vx = vx;
  this->vy = vy;
  w = Bullet::WIDTH;
  h = Bullet::HEIGHT;
}

void Bullet::paint(XInfo &xinfo) {
  XSetForeground(xinfo.display, xinfo.gc, xinfo.colours[YELLOW].pixel);
  XDrawLine(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*(x + scrollx + w), scaley*(y + scrolly + h/2));
  XDrawLine(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly + h), scalex*(x + scrollx + w), scaley*(y + scrolly + h/2));
  XSetForeground(xinfo.display, xinfo.gc, whitePixel);
}

void Bullet::update(float delta) {
  x += vx * delta;
  y += vy * delta;

  if (x + scrollx < 0 || x + scrollx + w > WIN_WIDTH) {
    deleteEntity(this);
  }
        // Gen explosion
}

