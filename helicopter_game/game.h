
#ifndef GAME_H
#define GAME_H

#include "alien.h"
#include "entity.h"
#include "building.h"
#include "bomb.h"
#include "debris.h"
#include "turret.h"
#include "aabullet.h"
#include "bullet.h"
#include "explosion.h"
#include "helicopter.h"

#define PI 3.1415927
#define FPS 60
#define WIN_WIDTH 1024
#define WIN_HEIGHT 768
#define GRAVITY 400.0f
#define FULL_CIRCLE 360 * 64
#define MAX_SCROLL_VX -50
#define MIN_SCROLL_VX -750
#define SCROLL_V_INC 25
#define SCROLL_V_START -150

#define TURRET_BUILDING_SCORE 100
#define TURRET_SCORE 75
#define ALIEN_SCORE 50

//Temporary
//#define SHOOT_TEST


class Helicopter;
class Alien;

enum GameState {MENU, PAUSE, INSTRUCTIONS, GAME, GAMEOVER};

extern GameState gameState;
extern float scrollvx;
extern float scrollvy;
extern float scrollx;
extern float scrolly;
extern float scalex;
extern float scaley;
extern float genCityStartx;

void addEntity(Building *e);
void addEntity(Bomb *e);
void addEntity(Debris *e);
void addEntity(Turret *e);
void addEntity(AABullet *e);
void addEntity(Bullet *e);
void addEntity(Explosion *e);
void addEntity(Alien *e);
void deleteEntity(Building *e);
void deleteEntity(Bomb *e);
void deleteEntity(Debris *e);
void deleteEntity(Turret *e);
void deleteEntity(AABullet *e);
void deleteEntity(Bullet *e);
void deleteEntity(Explosion *e);
void deleteEntity(Alien *e);


float solveDumbInterception(float x, float y, float z, float w, float a, float b, float c, float &bvx, float &bvy);
float solveInterception(float x, float y, float z, float w, float a, float b, float c, float &bvx, float &bvy);

Helicopter *getPlayer();

#endif

