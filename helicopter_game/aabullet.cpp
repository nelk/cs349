
#include <iostream>

#include "aabullet.h"
#include "game.h"

AABullet::AABullet(float x, float y, float vx, float vy) {
  this->x = x;
  this->y = y;
  this->vx = vx;
  this->vy = vy;
  w = AABullet::WIDTH;
  h = AABullet::HEIGHT;
}

void AABullet::paint(XInfo &xinfo) {
  XSetForeground(xinfo.display, xinfo.gc, xinfo.colours[RED].pixel);
  XDrawArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*(w), scaley*(h), 0, FULL_CIRCLE);
  XSetForeground(xinfo.display, xinfo.gc, whitePixel);
}

void AABullet::update(float delta) {
  //vy += GRAVITY * delta; // Gravity

  x += vx * delta;
  y += vy * delta;

  if (y > WIN_HEIGHT || y < 0 || x < 0) {
    deleteEntity(this);
  }
}

