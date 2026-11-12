// this mimic the structure of https://github.com/prideout/par/blob/master/par_streamlines.h

#ifndef SOME_HEADER_ONLY_CLIB_H
#define SOME_HEADER_ONLY_CLIB_H

float do_some_c_stuff(float a, float b);

#ifdef SOME_HEADER_ONLY_CLIB_H_IMPL
float do_some_c_stuff(float a, float b) { return a + b; }
#endif // SOME_HEADER_ONLY_CLIB_H_IMPL

#endif // SOME_HEADER_ONLY_CLIB_H
