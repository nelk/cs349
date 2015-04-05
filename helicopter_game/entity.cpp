
#include "entity.h"

bool Entity::collide(Entity *other) {
  return x + w >= other->x && x <= other->x + other->w &&
      y + h >= other->y && y <= other->y + other->h;
}

