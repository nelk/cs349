
#ifndef BOMB_H
#define BOMB_H

#include "entity.h"
#include "xtools.h"

class Bomb : public Entity {
public:
  Bomb(float x, float y, float vx, float vy);
  virtual ~Bomb() {}
  void paint(XInfo &xinfo);
  void update(float delta);
private:
  const static int WIDTH = 10;
  const static int HEIGHT = 10;
};

#endif
