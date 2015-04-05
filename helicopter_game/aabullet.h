
#ifndef AABULLET_H
#define AABULLET_H

#include "entity.h"

class AABullet : public Entity {
public:
  AABullet(float x, float y, float vx, float vy);
  virtual ~AABullet() {}
  void paint(XInfo &xinfo);
  void update(float delta);
  const static int SPEED = 50000.0f; //SQUARED VALUE!!
private:
  const static int WIDTH = 5;
  const static int HEIGHT = 5;
};

#endif

