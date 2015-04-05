

#ifndef ALIEN
#define ALIEN

#include <X11/Xlib.h>
#include <X11/Xutil.h>

#include "entity.h"
#include "xtools.h"
#include "game.h"

class Alien : public Entity {
public:
  Alien(float, float);
  virtual ~Alien() {}
  void paint(XInfo &xinfo);
  void update(float delta);
  float getVX();
  float getVY();

private:
  float moveTimer;
  float shootTimer;

  const static float SPEED_X = 220;
  const static float SPEED_Y = 220;
  const static int WIDTH = 40;
  const static int HEIGHT = 40;

};

#endif

