
#include "debris.h"
#include "entity.h"
#include "game.h"

Debris::Debris(float x, float y, float w, float h, float vx, float vy) : Entity() {
  this->x = x;
  this->y = y;
  this->vx = vx;
  this->vy = vy;
  this->w = w;
  this->h = h;
}


void Debris::paint(XInfo &xinfo) {
  XDrawRectangle(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*(w), scaley*(h));
}

void Debris::update(float delta) {
  vy += GRAVITY * delta; // Gravity

  x += vx * delta;
  y += vy * delta;

  if (y > WIN_HEIGHT) {
    deleteEntity(this);
  }
}

