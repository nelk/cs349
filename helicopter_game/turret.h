
#ifndef TURRET_H
#define TURRET_H

#include "xtools.h"
#include "entity.h"
#include "building.h"

class Building;

class Turret : public Entity {
public:
  Building *building;

  Turret(float x, float y);
  void paint(XInfo &xinfo);
  void update(float delta);

  const static int WIDTH = 10;
  const static int HEIGHT = 30;

private:
  float shootTimer;
  const static float SHOOT_TIME;
};

#endif

