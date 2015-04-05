
#ifndef BULLET_H
#define BULLET_H

#include "entity.h"

class Bullet : public Entity {
public:
  Bullet(float x, float y, float vx, float vy);
  virtual ~Bullet() {}
  void paint(XInfo &xinfo);
  void update(float delta);
  const static float SPEED = 600.0f;
private:
  const static int WIDTH = 5;
  const static int HEIGHT = 5;
};

#endif

