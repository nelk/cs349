
#ifndef XINFO_H
#define XINFO_H

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <map>
#include <string>

#define DOUBLE_BUFFERING

enum Colour {GREEN, RED, BLUE, YELLOW, GRAY, TOTAL_COLOURS};

struct XInfo {
  Pixmap backBuffer;
  int backBufferW, backBufferH;
  Colormap colourmap;
  XColor colours[TOTAL_COLOURS];
  Display *display;
  Window window;
  GC gc, gc2;
  GC fonts[];
  Drawable &getDrawable() {
#ifdef DOUBLE_BUFFERING
      return backBuffer;
#else
      return window;
#endif
  }
};


typedef struct XInfo XInfo;

extern unsigned long whitePixel, blackPixel;

#endif


