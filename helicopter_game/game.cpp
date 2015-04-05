/*
 *
 * Helicopter Game
 *
 * Author: Alex Klen
 *
 */

#include <iostream>
#include <math.h>
#include <set>
#include <string>
#include <sstream>
#include <cstdlib>
#include <sys/time.h>
#include <unistd.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>

#include "game.h"
#include "xtools.h"
#include "entity.h"
#include "helicopter.h"
#include "building.h"
#include "bomb.h"
#include "turret.h"
#include "explosion.h"
#include "aabullet.h"
#include "bullet.h"

#define TEXT_LEFT 200

using namespace std;

typedef struct {
  Helicopter *player;

  set<Building *> buildings;
  set<Bomb *> bombs;
  set<Debris *> debris;
  set<Turret *> turrets;
  set<AABullet *> aabullets;
  set<Bullet *> bullets;
  set<Explosion *> explosions;
  set<Alien *> aliens;

  set<Building *> deleteBuildings;
  set<Bomb *> deleteBombs;
  set<Debris *> deleteDebris;
  set<Turret *> deleteTurrets;
  set<AABullet *> deleteAABullets;
  set<Bullet *> deleteBullets;
  set<Explosion *> deleteExplosions;
  set<Alien *> deleteAliens;
} GameInfo;

GameInfo gameInfo; // Global game info
GameState gameState;
const int Border = 5;
const int BufferSize = 10;
unsigned long whitePixel, blackPixel;
float scrollvx = SCROLL_V_START;
float scrollvy = 0;
float scrollx = 0;
float scrolly = 0;
float scalex = 1;
float scaley = 1;
float genCityStartx = 500.0f;
float alienTimer = 0;

void resetGame();
void cleanupGame();


float solveDumbInterception(float x, float y, float z, float w, float a, float b, float c, float &bvx, float &bvy) {
  bvx = (x-a);
  bvy = (y-b);
  float hypsqr = bvx*bvx + bvy*bvy;
  float ratio = sqrt(c/hypsqr);
  bvx *= ratio;
  bvy *= ratio;
  return 1;
}

float solveInterception(float x, float y, float z, float w, float a, float b, float c, float &bvx, float &bvy) {
  const float ZERO_BOUND = 0.01f;
  float denom = 2 * (-c+pow(w,2)+pow(z,2));
  if (denom > -ZERO_BOUND && denom < ZERO_BOUND) {
    std::cout << "denom too small" << std::endl;
    return solveDumbInterception(x, y, z, w, a, b, c, bvx, bvy);
  }
  float innerNumer = pow(2*a*z-2*b*w+2*w*y-2*x*z,2)-4*(-c+pow(w,2)+pow(z,2))*(pow(a,2)-2*a*x+pow(b,2)-2*b*y+pow(x,2)+pow(y,2));
  if (innerNumer < 0) {
    return solveDumbInterception(x, y, z, w, a, b, c, bvx, bvy);
  }

  float numer1 = sqrt(innerNumer)-2*a*z+2*b*w-2*w*y+2*x*z;
  float numer2 = -sqrt(innerNumer)-2*a*z+2*b*w-2*w*y+2*x*z;
  float t1 = -numer1/denom;
  float t2 = -numer2/denom;
  float t;
#ifdef SHOOT_TEST
  std::cout << "t = " << t1 << ", " << t2 << std::endl;
#endif
  /*
  if (t1 < 0) t = t2;
  else if (t2 < 0) t = t1;
  else if (t1 < t2) t = t1;
  else t = t2;
  if (t < 0) return -1;
  */

  // STRANGE HACK
  if (w < 0 && z > 0) {
    t = t2;
  } else {
    t = t1;
  }

  bvx = (x-a)/t+z;
  bvy = (y-b)/t+w;
  float hypsqr = bvx*bvx + bvy*bvy;
  float ratio = sqrt(c/hypsqr);
  bvx *= ratio;
  bvy *= ratio;
#ifdef SHOOT_TEST
  std::cout << "bv = " << bvx << ", " << bvy << std::endl;
#endif
  return 1;
}


bool alienSpawn(float x) {
  return rand()%1000 / 1000.0f > 10000/(x+10000);
}


Helicopter *getPlayer() {
  return gameInfo.player;
}

void addEntity(Building *b) {
  gameInfo.buildings.insert(b);
}

void addEntity(Bomb *b) {
  gameInfo.bombs.insert(b);
}

void addEntity(Debris *b) {
  gameInfo.debris.insert(b);
}

void addEntity(Turret *b) {
  gameInfo.turrets.insert(b);
}

void addEntity(AABullet *b) {
  gameInfo.aabullets.insert(b);
}

void addEntity(Bullet *b) {
  gameInfo.bullets.insert(b);
}

void addEntity(Explosion *b) {
  gameInfo.explosions.insert(b);
}

void addEntity(Alien *b) {
  gameInfo.aliens.insert(b);
}

void deleteEntity(Building *b) {
  //Only mark for deletion here
  gameInfo.deleteBuildings.insert(b);
}

void deleteEntity(Bomb *b) {
  //Only mark for deletion here
  gameInfo.deleteBombs.insert(b);
}

void deleteEntity(Debris *b) {
  //Only mark for deletion here
  gameInfo.deleteDebris.insert(b);
}

void deleteEntity(Turret *b) {
  //Only mark for deletion here
  gameInfo.deleteTurrets.insert(b);
}

void deleteEntity(AABullet *b) {
  //Only mark for deletion here
  gameInfo.deleteAABullets.insert(b);
}

void deleteEntity(Bullet *b) {
  //Only mark for deletion here
  gameInfo.deleteBullets.insert(b);
}

void deleteEntity(Explosion *b) {
  //Only mark for deletion here
  gameInfo.deleteExplosions.insert(b);
}

void deleteEntity(Alien *b) {
  //Only mark for deletion here
  gameInfo.deleteAliens.insert(b);
}

void paintString(XInfo &xinfo, GC &fontGC, int x, int y, string s) {
  XDrawString( xinfo.display, xinfo.getDrawable(), fontGC, x, y, s.c_str(), s.length() );
}

template <class T>
void deleteEntitiesFromSet(set<T *> &entitySet, set<T *> &deleteSet) {
  for (typename set<T *>::iterator it = deleteSet.begin(); it != deleteSet.end(); it++) {
    entitySet.erase(*it);
    delete (*it);
  }
  deleteSet.clear();
}

void gameUpkeep() {
  deleteEntitiesFromSet(gameInfo.bombs, gameInfo.deleteBombs);
  deleteEntitiesFromSet(gameInfo.buildings, gameInfo.deleteBuildings);
  deleteEntitiesFromSet(gameInfo.aabullets, gameInfo.deleteAABullets);
  deleteEntitiesFromSet(gameInfo.bullets, gameInfo.deleteBullets);
  deleteEntitiesFromSet(gameInfo.debris, gameInfo.deleteDebris);
  deleteEntitiesFromSet(gameInfo.turrets, gameInfo.deleteTurrets);
  deleteEntitiesFromSet(gameInfo.explosions, gameInfo.deleteExplosions);
  deleteEntitiesFromSet(gameInfo.aliens, gameInfo.deleteAliens);

}

/*
 * Function to put out a message on error exits.
 */
void error( string str ) {
  cerr << str << endl;
  exit(0);
}

template <class T>
void paintAll(set<T> &s, XInfo &xinfo) {
  for (typename set<T>::iterator it = s.begin(); it != s.end(); it++) {
    (*it)->paint(xinfo);
  }
}

void repaint(XInfo &xinfo) {
#ifdef DOUBLE_BUFFERING
    XFillRectangle(xinfo.display, xinfo.backBuffer, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH);
#else
    XClearWindow(xinfo.display, xinfo.window);
#endif

  paintAll(gameInfo.debris, xinfo);
  paintAll(gameInfo.buildings, xinfo);
  paintAll(gameInfo.bombs, xinfo);
  paintAll(gameInfo.turrets, xinfo);
  paintAll(gameInfo.aabullets, xinfo);
  paintAll(gameInfo.bullets, xinfo);
  paintAll(gameInfo.explosions, xinfo);
  paintAll(gameInfo.aliens, xinfo);
  gameInfo.player->paint(xinfo);

  // Paint score, lives, and scroll speed
  stringstream ss;
  ss << "Score: " << gameInfo.player->score;
  paintString(xinfo, xinfo.fonts[1], 5, 20, ss.str());
  ss.str("");
  ss << "Lives: " << gameInfo.player->lives;
  paintString(xinfo, xinfo.fonts[1], 5, 40, ss.str());
  ss.str("");
  ss << "Speed: " << (scrollvx/SCROLL_V_START) << "x";
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH - 150), scaley*(20), ss.str());

#ifdef DOUBLE_BUFFERING
    XCopyArea(xinfo.display, xinfo.backBuffer, xinfo.window, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH, 0, 0);
#endif

  XFlush(xinfo.display);
}

void repaintMenu(XInfo &xinfo) {
#ifdef DOUBLE_BUFFERING
    XFillRectangle(xinfo.display, xinfo.backBuffer, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH);
#else
    XClearWindow(xinfo.display, xinfo.window);
#endif

  paintString(xinfo, xinfo.fonts[0], scalex*(WIN_WIDTH / 2 - TEXT_LEFT),scaley*( WIN_HEIGHT / 2 - 200), "HeliBlitz");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 150), "Press Enter to Begin");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 100), "Press i to view Instructions");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 50), "Press q to Quit");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 + 200), "Alexander Klen (654)");

#ifdef DOUBLE_BUFFERING
    XCopyArea(xinfo.display, xinfo.backBuffer, xinfo.window, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH, 0, 0);
#endif

  XFlush(xinfo.display);
}

void repaintPause(XInfo &xinfo) {
  paintString(xinfo, xinfo.fonts[0], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 200), "HeliBlitz");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 150), "PAUSED");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 100), "Press Enter to Continue");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 + 200), "Alexander Klen (654)");
}


void repaintInstructions(XInfo &xinfo) {
#ifdef DOUBLE_BUFFERING
    XFillRectangle(xinfo.display, xinfo.backBuffer, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH);
#else
    XClearWindow(xinfo.display, xinfo.window);
#endif


  paintString(xinfo, xinfo.fonts[0], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 200), "HeliBlitz Instructions");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 150), "Use the arrow keys to move your helicopter");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 100), "Press the space bar to drop bombs");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 - 50), "Hold the left control key to shoot");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2), "Bomb buildings with turrets and shoot turrets to earn points!");
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT), scaley*(WIN_HEIGHT / 2 + 50), "Use 'a' to decrease scrolling speed and 'd' to increase scrolling speed.");

#ifdef DOUBLE_BUFFERING
    XCopyArea(xinfo.display, xinfo.backBuffer, xinfo.window, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH, 0, 0);
#endif

  XFlush(xinfo.display);

}


void repaintGameOver(XInfo &xinfo) {
#ifdef DOUBLE_BUFFERING
    XFillRectangle(xinfo.display, xinfo.backBuffer, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH);
#else
    XClearWindow(xinfo.display, xinfo.window);
#endif

  paintString(xinfo, xinfo.fonts[0], scalex*(WIN_WIDTH / 2 - TEXT_LEFT),scaley*( WIN_HEIGHT / 2 - 400), "GAME OVER");
  stringstream ss;
  ss << "Your score was: " << gameInfo.player->score;
  paintString(xinfo, xinfo.fonts[1], scalex*(WIN_WIDTH / 2 - TEXT_LEFT),scaley*( WIN_HEIGHT / 2 - 200), ss.str());

#ifdef DOUBLE_BUFFERING
    XCopyArea(xinfo.display, xinfo.backBuffer, xinfo.window, xinfo.gc2, 0, 0, xinfo.backBufferW, xinfo.backBufferH, 0, 0);
#endif

  XFlush(xinfo.display);

}

// Returns 0 if continuing, non-0 if should exit program
int handleEvent(const XInfo &xinfo) {
  XEvent event;
  KeySym key;
  //char text[BufferSize];
  bool pressed = false;

  XNextEvent(xinfo.display, &event);

  // Ignore autorepeat keys
  if (event.type == KeyRelease && XEventsQueued(xinfo.display, QueuedAfterReading)) {
      XEvent nextEvent;
      XPeekEvent(xinfo.display, &nextEvent);

      // Check if key wasn't actually released
      if (nextEvent.type == KeyPress && nextEvent.xkey.time == event.xkey.time && nextEvent.xkey.keycode == event.xkey.keycode) {
        // Ignore the release event and the next press event
        XNextEvent( xinfo.display, &event );
        return 0;
      }
  }
  switch( event.type ) {
    case Expose:
      break;
    case ButtonPress:
      break;
    case KeyPress:
      pressed = true;
    case KeyRelease:
      key = XLookupKeysym((XKeyEvent *)&event, 0);

      if (gameState == GAME) {
        if (pressed && (key == XK_q || key == XK_Escape)) {
          gameState = MENU;
        } else if (pressed && (key == XK_f || key == XK_F || key == XK_p || key == XK_P)) {
          gameState = PAUSE;
        }
        int dx = 0, dy = 0;
        if (key == XK_Left) {
          dx = -1;
        } else if (key == XK_Right) {
          dx = 1;
        } else if (key == XK_Up) {
          dy = -1;
        } else if (key == XK_Down) {
          dy = 1;
        }
        gameInfo.player->keyinput(dx, dy, pressed);

        if (key == XK_space) {
          gameInfo.player->setBombing(pressed);
        }

        if (key == XK_Control_L) {
          gameInfo.player->setShooting(pressed);
        }

        if (pressed && key == XK_a) {
          scrollvx += SCROLL_V_INC;
          if (scrollvx > MAX_SCROLL_VX) scrollvx = MAX_SCROLL_VX;
        } else if (pressed && key == XK_d) {
          scrollvx -= SCROLL_V_INC;
          if (scrollvx < MIN_SCROLL_VX) scrollvx = MIN_SCROLL_VX;
        }
      } else if (gameState == MENU) {
        if (pressed && (key == XK_q || key == XK_Escape)) {
          error( "Terminated normally." );
          XCloseDisplay(xinfo.display);
          return 1;
        } else if (pressed && key == XK_Return) {
          gameState = GAME;
          resetGame();
        } else if (pressed && (key == XK_i || key == XK_I)) {
          gameState = INSTRUCTIONS;
        }
      } else if (gameState == PAUSE) {
        if (pressed && (key == XK_f || key == XK_F || key == XK_p || key == XK_P || key == XK_Return)) {
          gameState = GAME;
        }
      } else if (gameState == INSTRUCTIONS || gameState == GAMEOVER) {
        if (pressed && (key == XK_Escape || key == XK_q || key == XK_Return)) {
          gameState = MENU;
        }
      }
      break;
  }

  return 0;
}

/**
 * Returns number of microseconds since beginning of day.
 */
unsigned long now() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return tv.tv_sec * 1000000 + tv.tv_usec;
}

template <class T>
void updateAll(const set<T> &s, float deltaSeconds){
    for (typename set<T>::iterator it = s.begin(); it != s.end(); it++) {
      (*it)->update(deltaSeconds);
    }
}

void update(XInfo &xinfo, float deltaSeconds) {
    // Update all entities
    updateAll(gameInfo.debris, deltaSeconds);
    updateAll(gameInfo.buildings, deltaSeconds);
    updateAll(gameInfo.bombs, deltaSeconds);
    updateAll(gameInfo.turrets, deltaSeconds);
    updateAll(gameInfo.aabullets, deltaSeconds);
    updateAll(gameInfo.bullets, deltaSeconds);
    updateAll(gameInfo.explosions, deltaSeconds);
    updateAll(gameInfo.aliens, deltaSeconds);
    gameInfo.player->update(deltaSeconds);
}

void deleteBuilding(Building *b) {
    // Delete turret on building
    if (b->turret) {
      gameInfo.player->score += TURRET_BUILDING_SCORE;
      deleteEntity(b->turret);
      b->turret = NULL;
    }

    // Gen lots of explosions
    for (int i = 0; i < 8; ++i) {
      addEntity(new Explosion(b->getX() + rand()%(int)b->getW(), b->getY() + rand()%max((int)b->getH(), 2), rand()%50+20));
    }

    b->die();
}

void collide() {
  // Check collisions between bombs and buildings
  for (set<Bomb *>::iterator iBomb = gameInfo.bombs.begin(); iBomb != gameInfo.bombs.end(); iBomb++) {
    for (set<Building *>::iterator iBuilding = gameInfo.buildings.begin(); iBuilding != gameInfo.buildings.end(); iBuilding++) {
      //if ((*iBuilding)->state == Entity::DEAD) continue;
      if ((*iBomb)->collide(*iBuilding)) {

        deleteBuilding(*iBuilding);
        deleteEntity(*iBomb);
        // Gen explosion
        addEntity(new Explosion((*iBomb)->getX(), (*iBomb)->getY(), 60));
      }
    }
  }


  // Check collisions between bullets and buildings
  for (set<Bullet *>::iterator iBullet = gameInfo.bullets.begin(); iBullet != gameInfo.bullets.end(); iBullet++) {
    for (set<Building *>::iterator iBuilding = gameInfo.buildings.begin(); iBuilding != gameInfo.buildings.end(); iBuilding++) {
      if ((*iBullet)->collide(*iBuilding)) {
        deleteEntity(*iBullet);
        addEntity(new Explosion((*iBullet)->getX(), (*iBullet)->getY(), 10));
      }
    }
  }

  // Check collisions between bullets and aliens
  for (set<Bullet *>::iterator iBullet = gameInfo.bullets.begin(); iBullet != gameInfo.bullets.end(); iBullet++) {
    for (set<Alien *>::iterator iAlien = gameInfo.aliens.begin(); iAlien != gameInfo.aliens.end(); iAlien++) {
      if ((*iBullet)->collide(*iAlien)) {
        deleteEntity(*iBullet);
        deleteEntity(*iAlien);
        gameInfo.player->score+=ALIEN_SCORE;
        addEntity(new Explosion((*iBullet)->getX(), (*iBullet)->getY(), 10));
      }
    }
  }


  // Check collisions between bombs and aliens
  for (set<Bomb *>::iterator iBomb = gameInfo.bombs.begin(); iBomb != gameInfo.bombs.end(); iBomb++) {
    for (set<Alien *>::iterator iAlien = gameInfo.aliens.begin(); iAlien != gameInfo.aliens.end(); iAlien++) {
      if ((*iBomb)->collide(*iAlien)) {
        deleteEntity(*iBomb);
        deleteEntity(*iAlien);
        gameInfo.player->score+=ALIEN_SCORE;
        addEntity(new Explosion((*iBomb)->getX(), (*iBomb)->getY(), 10));
      }
    }
  }


  // Check collisions between buildings and aliens
  for (set<Building *>::iterator iBuilding = gameInfo.buildings.begin(); iBuilding != gameInfo.buildings.end(); iBuilding++) {
    for (set<Alien *>::iterator iAlien = gameInfo.aliens.begin(); iAlien != gameInfo.aliens.end(); iAlien++) {
      if ((*iBuilding)->collide(*iAlien)) {
        deleteBuilding(*iBuilding);
        deleteEntity(*iAlien);
        addEntity(new Explosion((*iBuilding)->getX(), (*iBuilding)->getY(), 10));
      }
    }
  }

  // Check collisions between bullets and turrets
  for (set<Bullet *>::iterator iBullet = gameInfo.bullets.begin(); iBullet != gameInfo.bullets.end(); iBullet++) {
    for (set<Turret *>::iterator iTurret = gameInfo.turrets.begin(); iTurret != gameInfo.turrets.end(); iTurret++) {
      if ((*iBullet)->collide(*iTurret)) {
        // Unlink from building
        (*iTurret)->building->turret = NULL;
        (*iTurret)->building = NULL;

        // Gen explosion
        addEntity(new Explosion((*iTurret)->getX(), (*iTurret)->getY(), 30));
        deleteEntity(*iTurret);

        gameInfo.player->score += TURRET_SCORE;
      }
    }
  }

  // Player death collisions
  if (gameInfo.player->state != Entity::RESPAWN) {

    // Check collisions between helicopter and buildings
    for (set<Building *>::iterator iBuilding = gameInfo.buildings.begin(); iBuilding != gameInfo.buildings.end(); iBuilding++) {
      if (gameInfo.player->collide(*iBuilding)) {
        deleteBuilding(*iBuilding);
        // Generate debris
        //(*iBuilding)->genDebris();

        // Kill player
        gameInfo.player->die();
      }
    }

    // Check collisions between helicopter and aabullets
    for (set<AABullet *>::iterator iAABullet = gameInfo.aabullets.begin(); iAABullet != gameInfo.aabullets.end(); iAABullet++) {
      if (gameInfo.player->collide(*iAABullet)) {
        deleteEntity(*iAABullet);

        // Generate explosion

        // Kill player
        gameInfo.player->die();
      }
    }

    // Check collisions between helicopter and aliens
    for (set<Alien *>::iterator iAlien = gameInfo.aliens.begin(); iAlien != gameInfo.aliens.end(); iAlien++) {
      if (gameInfo.player->collide(*iAlien)) {
        deleteEntity(*iAlien);

        // Kill player
        gameInfo.player->die();
      }
    }

  }
}

void gameLoop(XInfo &xinfo) {
  int evtReturnCode = 0;
  unsigned long lastRepaint = now();

  while(true) {
    if (XPending(xinfo.display)) {
      evtReturnCode = handleEvent(xinfo);
    }
    if (evtReturnCode) break;

    // Window size code
    XWindowAttributes a;
    XGetWindowAttributes(xinfo.display, xinfo.window, &a);
    scalex = a.width/(float)WIN_WIDTH;
    scaley = a.height/(float)WIN_HEIGHT;
#ifdef DOUBLE_BUFFERING
    if (a.width > xinfo.backBufferW || a.height > xinfo.backBufferH) {
      // Recreate backbuffer
      //cout << "Recreating backbuffer" << endl;
      XFreePixmap(xinfo.display, xinfo.backBuffer);
      xinfo.backBuffer = XCreatePixmap(xinfo.display, xinfo.window, a.width, a.height, DefaultDepth(xinfo.display, DefaultScreen(xinfo.display)));
      xinfo.backBufferW = a.width;
      xinfo.backBufferH = a.height;
      //cout << "Window: (" << xinfo.backBufferW << ", " << xinfo.backBufferH << ")" << endl;
    }
#endif

    unsigned long time = now();
    unsigned long delta = time - lastRepaint;
    if (delta > 1000000/FPS) {
      if (gameState == GAME) {
        float deltaSeconds = delta/1000000.0f;

#ifndef SHOOT_TEST
        // Increment scroll position
        scrollx += scrollvx * deltaSeconds;
        if (-scrollx + WIN_WIDTH > genCityStartx) {
          genCityStartx = Building::genCityBlock(genCityStartx);
        }
#endif

        // Spawn aliens
        if (alienTimer <= 0) {
          alienTimer = 2.0f;
          if (alienSpawn(scrollx)) {
            addEntity(new Alien(-scrollx + WIN_WIDTH, WIN_HEIGHT/3));
          }
        } else {
          alienTimer -= deltaSeconds;
        }

        update(xinfo, deltaSeconds);
        collide();
        gameUpkeep();

        repaint(xinfo);
      } else if (gameState == MENU) {
        repaintMenu(xinfo);
      } else if (gameState == PAUSE) {
        repaintPause(xinfo);
      } else if (gameState == INSTRUCTIONS) {
        repaintInstructions(xinfo);
      } else if (gameState == GAMEOVER) {
        repaintGameOver(xinfo);
      }
      lastRepaint = now();
    }
    if (XPending(xinfo.display) == 0) {
      if (1000000/FPS > delta) {
        usleep(1000000/FPS - delta);
      }
    }
  }
}

/*
 * Create the window;  initialize X.
 */
void initX(int argc, char *argv[], XInfo &xinfo) {
  XSizeHints hints;
  int screen;

  /*
   * Display opening uses the DISPLAY  environment variable.
   * It can go wrong if DISPLAY isn't set, or you don't have permission.
   */
  xinfo.display = XOpenDisplay( "" );
  if ( !xinfo.display ) {
    error( "Can't open display." );
  }

  /*
   * Find out some things about the display you're using.
   */
  screen = DefaultScreen( xinfo.display );
  whitePixel = WhitePixel( xinfo.display, screen );
  blackPixel = BlackPixel( xinfo.display, screen );

  /*
   * Set up hints and properties for the window manager, and open a window.
   * Arguments to XCreateSimpleWindow :
   *                 display - the display on which the window is opened
   *                 parent - the parent of the window in the window tree
   *                 x,y - the position of the upper left corner
   *                 width, height - the size of the window
   *                 Border - the width of the window border
   *                 foreground - the colour of the window border
   *                 background - the colour of the window background.
   * Arguments to XSetStandardProperties :
   *                 display - the display on which the window exists
   *                 window - the window whose properties are set
   *                 Hello1 - the title of the window
   *                 Hello2 - the title of the icon
   *                 none - a pixmap for the icon
   *                 argv, argc - a comand to run in the window
   *                 hints - sizes to use for the window.
   */
  hints.x = 10;
  hints.y = 10;
  hints.width = 800;//WIN_WIDTH;
  hints.height = 600;//WIN_HEIGHT;
  hints.flags = PPosition | PSize;
  xinfo.window = XCreateSimpleWindow( xinfo.display, DefaultRootWindow( xinfo.display ), hints.x, hints.y, hints.width, hints.height, Border, blackPixel, whitePixel );
  XSetStandardProperties( xinfo.display, xinfo.window, "Helicopter Game", "", None, argv, argc, &hints );

  /*
   * Create pixmap backbuffer
   */
#ifdef DOUBLE_BUFFERING
    xinfo.backBuffer = XCreatePixmap(xinfo.display, xinfo.window, hints.width, hints.height, DefaultDepth(xinfo.display, screen));
    xinfo.backBufferW = hints.width;
    xinfo.backBufferH = hints.height;
#endif

  //XMapWindow(xinfo.display, xinfo.window);
  XMapRaised(xinfo.display, xinfo.window);

  /*
   * Get a graphics context and set the drawing colours.
   * Arguments to XCreateGC
   *           display - which uses this GC
   *           window - which uses this GC
   *           GCflags - flags which determine which parts of the GC are used
   *           GCdata - a struct that fills in GC data at initialization.
   */
  xinfo.gc = XCreateGC(xinfo.display, xinfo.window, 0, 0 );
  XSetBackground(xinfo.display, xinfo.gc, blackPixel);
  XSetForeground(xinfo.display, xinfo.gc, whitePixel);

  xinfo.gc2 = XCreateGC(xinfo.display, xinfo.window, 0, 0 );
  XSetBackground(xinfo.display, xinfo.gc2, whitePixel);
  XSetForeground(xinfo.display, xinfo.gc2, blackPixel);

  /*
   * Tell the window manager what input you want.
   */
  XSelectInput( xinfo.display, xinfo.window, ButtonPressMask | KeyPressMask | KeyReleaseMask | ExposureMask );


  /*
   * Put the window on the screen.
   */
  //XMapRaised( xinfo.display, xinfo.window);

  // Waiting for MapNotify to inform that the window is mapped onto the screen
  //for (XEvent e; e.type != MapNotify; XNextEvent(xinfo.display, &e));


  // Allocate colours
  xinfo.colourmap = DefaultColormap(xinfo.display, 0);
  string cv[TOTAL_COLOURS] = {"#00FF00", "#FF0000", "#0000FF", "#FFFF00", "#CCCCCC"};
  for (int i = 0; i < TOTAL_COLOURS; ++i) {
    XParseColor(xinfo.display, xinfo.colourmap, cv[i].c_str(), &xinfo.colours[i]);
    XAllocColor(xinfo.display, xinfo.colourmap, &xinfo.colours[i]);
  }

  // Allocate fonts
  XGCValues values;
  XFontStruct *f = XLoadQueryFont(xinfo.display, "-*-fixed-*-*-*-*-20-*-*-*-*-*-*-*");
  values.font = f->fid;
  xinfo.fonts[0] = XCreateGC(xinfo.display, xinfo.getDrawable(), GCFont, &values);
  XSetBackground(xinfo.display, xinfo.fonts[0], blackPixel);
  XSetForeground(xinfo.display, xinfo.fonts[0], whitePixel);

  XGCValues values2;
  XFontStruct *f2 = XLoadQueryFont(xinfo.display, "-*-fixed-*-*-*-*-18-*-*-*-*-*-*-*");
  values2.font = f2->fid;
  xinfo.fonts[1] = XCreateGC(xinfo.display, xinfo.getDrawable(), GCFont, &values2);
  XSetBackground(xinfo.display, xinfo.fonts[1], blackPixel);
  XSetForeground(xinfo.display, xinfo.fonts[1], whitePixel);

  // Initialize graphics for other classes
  Helicopter::setup(xinfo);
}

void initGame() {
  gameInfo.player = new Helicopter();
  gameState = MENU;
}

void resetGame() {
  // Resets all game data
  cleanupGame(); // Delete everything
  gameInfo.player = new Helicopter();
  gameInfo.player->spawn(20, 200);
  scrollx = 0;
  genCityStartx = 500.0f;

#ifdef SHOOT_TEST
  addEntity(new Turret(800, 400));
#endif
}

template <class T>
void deleteAndClearAll(set<T> &s) {
  for (typename set<T>::iterator it = s.begin(); it != s.end(); it++) {
    delete (*it);
  }
  s.clear();
}

void cleanupGame() {
  deleteAndClearAll(gameInfo.bombs);
  deleteAndClearAll(gameInfo.buildings);
  deleteAndClearAll(gameInfo.aabullets);
  deleteAndClearAll(gameInfo.bullets);
  deleteAndClearAll(gameInfo.turrets);
  deleteAndClearAll(gameInfo.explosions);
  deleteAndClearAll(gameInfo.aliens);
  deleteAndClearAll(gameInfo.debris);
  delete gameInfo.player;
  gameInfo.player = NULL;
}


/*
 * Start executing here.
 *   First initialize window.
 *   Next loop responding to events.
 *   Exit forcing window manager to clean up - cheesy, but easy.
 */
int main ( int argc, char *argv[] ) {
  XInfo xinfo;

  initX(argc, argv, xinfo);
  gameLoop(xinfo);

  cleanupGame();
}


