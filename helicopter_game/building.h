

#ifndef BUILDING_H
#define BUILDING_H

#include "entity.h"
#include "turret.h"

class Turret;

class Building : public Entity {
public:
  Turret *turret;

  Building(float x, float y, float h);
  virtual ~Building() {}
  void paint(XInfo &xinfo);
  void update(float delta);
  void die();

  static float genCityBlock(float startx);
private:

  const static int WIDTH = 60;
  const static int HEIGHT = 250; // Size of each building
  const static int FALL_SPEED = 200;
};

#endif
