
#ifndef DEBRIS_H
#define DEBRIS_H

#include "entity.h"

class Debris : public Entity {
public:
  Debris(float x, float y, float w, float h, float vx, float vy);
  virtual ~Debris() {}
  void paint(XInfo &xinfo);
  void update(float delta);
private:
};

#endif
