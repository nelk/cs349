
#include <cstdlib>
#include <iostream>
#include <math.h>
#include "building.h"
#include "turret.h"
#include "game.h"

Building::Building(float x, float y, float h) {
  this->x = x;
  this->y = y;
  this->w = Building::WIDTH;
  this->h = h;
  turret = NULL;
  state = NORMAL;
}

void Building::paint(XInfo &xinfo) {
  if (turret == NULL) {
    XSetForeground(xinfo.display, xinfo.gc, xinfo.colours[GRAY].pixel);
  }
  // Outside
  XDrawRectangle(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(x + scrollx), scaley*(y + scrolly), scalex*(w), scaley*(h));

  // Windows
  const int WH = 10;
  const int WW = 25;
  const int SH = 3;
  const int SW = 3;
  for (int i = x + SW; i <= x + w - WW - SW; i += WW + SW) {
    for (int j = y + SH; j <= y + h - WH - SH; j += WH + SH) {
      //TODO fix
      XDrawRectangle(xinfo.display, xinfo.getDrawable(), xinfo.gc, scalex*(i + scrollx), scaley*(j + scrolly), scalex*(WW), scaley*(WH));
    }
  }
  XSetForeground(xinfo.display, xinfo.gc, whitePixel);
}

void Building::update(float delta) {
  if (-scrollx > x + Building::WIDTH) {
    deleteEntity(this);
  }

  if (state == DEAD) {
    // Gen explosions
    for (int i = 0; i < 1; ++i) {
      addEntity(new Explosion(x + rand()%(int)w, y + h - rand()%30, 30));
    }

    y += Building::FALL_SPEED * delta;
    h -= Building::FALL_SPEED * delta;
    if (h <= 0) {
      deleteEntity(this);
    }
  }
}

float cityHeight(float x) {
  return sin(x/200.0f) + 0.5;
}

bool turretSpawn(float x) {
  //std::cout << 10000/(x+10000) << std::endl;
  return rand()%1000 / 1000.0f > 10000/(x+10000);
}

float Building::genCityBlock(float startx) {
  const int BUILDINGS_PER_CITY_BLOCK = 1;
  const int END_X = startx + Building::WIDTH * BUILDINGS_PER_CITY_BLOCK;
  Building *b;
  for (int i = startx; i < END_X; i += Building::WIDTH) {
    float height = (cityHeight(i) + rand() / (float)RAND_MAX * 1.25 - 0.3) * Building::HEIGHT;
    if (height > 0) {
      b = new Building(i, WIN_HEIGHT - height, height);
      addEntity(b);

      // Chance of creating a turret on this building
      if (turretSpawn(i)) {
        Turret *t = new Turret(i + Building::WIDTH/2 - Turret::WIDTH/2, WIN_HEIGHT - height - Turret::HEIGHT);
        b->turret = t;
        t->building = b;
        addEntity(t);
      }
    }
  }
  return END_X;
}

void Building::die() {
  state = DEAD;
  const int DEB_HEIGHT = 20;
  for (int i = x; i <= x + w; i += w/6) {
    for (int j = y; j <= y + h; j += DEB_HEIGHT) {
      addEntity(new Debris(i, j, 5, 5, rand()%150 - 75, rand()%50));
    }
  }
}

