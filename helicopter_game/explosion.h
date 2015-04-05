
#ifndef EXPLOSION_H
#define EXPLOSION_H

#include "entity.h"

class Explosion : public Entity {
public:
  Explosion(float x, float y, float r);
  virtual ~Explosion() {}
  void paint(XInfo &xinfo);
  virtual void update(float delta);
private:
  float r;
  float time;
  const static float TOTAL_TIME;
};

#endif
