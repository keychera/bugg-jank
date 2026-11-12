#include "some_header_only_clib.h"

struct vec2 {
  vec2(double x = 0.0f, double y = 0.0f) : x(x), y(y) {};
  double x, y;
};

inline float do_lib_stuff(float a, float b) { return do_some_c_stuff(a, b); }
