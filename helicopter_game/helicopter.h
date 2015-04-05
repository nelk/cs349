
#ifndef HELICOPTER
#define HELICOPTER

#include <X11/Xlib.h>
#include <X11/Xutil.h>

#include "entity.h"
#include "xtools.h"
#include "game.h"

class Helicopter : public Entity {
public:
  Helicopter();
  virtual ~Helicopter() {}
  void paint(XInfo &xinfo);
  void update(float delta);
  float getVX();
  float getVY();

  void die();
  void spawn(float x, float y);
  void keyinput(int vx, int vy, bool pressed);
  void setBombing(bool bombing);
  void setShooting(bool shooting);
  static void setup(XInfo &xinfo);

  int lives;
  int score;
private:
  bool bombing;
  float bombTimer;
  bool shooting;
  float shootTimer;
  float respawnTimer;

  static Pixmap pixmap;

  const static float SPEED_X = 220;
  const static float SPEED_Y = 220;
  const static int WIDTH = 55;
  const static int HEIGHT = 20;
  const static float BOMB_RATE = 0.5f;
  const static float SHOOT_RATE = 0.1f;
  const static float SHOOT_SPREAD = 15.0f;
  const static float RESPAWN_RATE = 2.0f;

};

#endif

