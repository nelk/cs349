
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <math.h>
#include "explosion.h"
#include "entity.h"
#include "game.h"

const float Explosion::TOTAL_TIME = 0.5f;

Explosion::Explosion(float x, float y, float r) : Entity() {
  this->x = x;
  this->y = y;
  this->r = r;
  time = 0;
}

void Explosion::paint(XInfo &xinfo) {
  float rad = sin(time/Explosion::TOTAL_TIME * PI) * r;
  XFillArc(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x - rad/2 + scrollx), scaley*(y - rad/2 + scrolly), scalex*(rad), scaley*(rad), 0, FULL_CIRCLE);
  //XDrawArc(xinfo.display, xinfo.window, xinfo.gc, x + scrollx, y + scrolly, rad, rad, 0, FULL_CIRCLE);
}

void Explosion::update(float delta) {
  time += delta;
  if (time >= Explosion::TOTAL_TIME) {
    deleteEntity(this);
  }
}

