
#include <iostream>

#include "xtools.h"
#include "bomb.h"
#include "game.h"

Bomb::Bomb(float x, float y, float vx, float xy) : Entity() {
  this->x = x;
  this->y = y;
  this->vx = vx;
  this->vy = vy;
  this->w = Bomb::WIDTH;
  this->h = Bomb::HEIGHT;
}

void Bomb::paint(XInfo &xinfo) {
  XSetForeground(xinfo.display, xinfo.gc, xinfo.colours[BLUE].pixel);
  XFillArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*(w), scaley*(h), 0, FULL_CIRCLE);
  XDrawArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx - 3), scaley*(y + scrolly - 3), scalex*(w + 6), scaley*(h + 6), 0, FULL_CIRCLE);
  XSetForeground(xinfo.display, xinfo.gc, whitePixel);
}

void Bomb::update(float delta) {
  vy += GRAVITY * delta; // Gravity

  x += vx * delta;
  y += vy * delta;

  if (y > WIN_HEIGHT) {
    deleteEntity(this);
  }
}

