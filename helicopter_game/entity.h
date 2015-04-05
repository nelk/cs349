
#ifndef ENTITY_H
#define ENTITY_H

#include "xtools.h"

/*
 * An abstract class representing game entities. 
 */
class Entity {
public:
  Entity() : x(0), y(0), vx(0), vy(0) {}
  virtual ~Entity() {}
  virtual void paint(XInfo &xinfo) = 0;
  virtual void update(float delta) = 0;
  virtual bool collide(Entity *other);
  float getX() const { return x; }
  float getY() const { return y; }
  float getVX() const { return vx; }
  float getVY() const { return vy; }
  float getW() const { return w; }
  float getH() const { return h; }

  enum State {NORMAL, DEAD, RESPAWN};
  State state;
protected:
  float x, y, vx, vy, w, h;
};

#endif
