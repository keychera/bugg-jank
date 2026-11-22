# buggy jank

A project to repro bugs that I encounter, that may or may not be caused by me violating rules in the C/C++ land, or because of this setup, or maybe an actual jank bug.

need 
- babashka, at least v1.13.222
- jank, at least from commit [3b7ffc220fb8c65eecf4e777bfcc8e5252d30fb3](https://github.com/jank-lang/jank/commit/3b7ffc220fb8c65eecf4e777bfcc8e5252d30fb3) 

small explanation about the tasks:
- `bb jank` is a bb script that builds `jank run-main` command
- `bb compile` is a bb script that builds `jank compile` command
- both task receive `:bug` flag accepting integer, this will select which alias to use from deps.edn
- running these task will also print the jank command to be run, in case the jank command itself might be the faulty ones

the detailed error is inside comments of one of the files in respective src folders
```sh
bb jank :args bb-jank! # just hello world
```

## bug 1
which I assumed caused by using binding name the same name as a cpp type

*verdict: low prio*
https://github.com/jank-lang/jank/issues/1189 (closed as dupli of below)
https://github.com/jank-lang/jank/issues/1186
```sh
bb jank :bug 1 # codegen error
```

## bug 2
which I assume include extra.hpp twice makes the c header libs have double implementation
this was found because I was using this C header-only lib: https://github.com/prideout/par/blob/master/par_streamlines.h
and I tried to mimic the usage of that lib with this setup

*verdict: not a bug: this kind of C header-only lib that has impl need special treatment*

this is how we should do it. from [cute_headers](https://github.com/RandyGaul/cute_headers#faq)
```c
// Do this ONCE in a .c/.cpp file
#define LIBNAME_IMPLEMENTATION
#include "libname.h"

// Everywhere else, just include like a typical header
#include "libname.h"
```

```sh
bb jank :bug 2 # this will work fine
bb compile :bug 2 # this compiles error 
```

## bug 3
this is still a bug, including once "fakeSDL.h" anywhere make run-main works, but not compile

*verdict: need a doc update: [link to doc](https://book.jank-lang.org/cpp-interop/cpp-raw.html#:~:text=Note-,Unlike%20in%20C)*
quote from jeaye:
> So the path forward here is to better document the single translation unit of the JIT runtime vs the multi translation unit system of AOT.
```sh
bb jank :bug 3 # this will work fine
bb compile :bug 3 # this compiles error 
```

issue here: https://github.com/jank-lang/jank/issues/1201

## bug 4

instruction in the [src-bug4/README](src-bug4/readme.md)

my own patch that fixes my problem: https://github.com/keychera/jank/commit/cf6c891cda7155088483874cf6294617c2edc09b

## bug 5

instruction in the [src-bug5/README](src-bug5/readme.md)

is bug: jank doesn't search the same path as clang
fixed here: https://github.com/jank-lang/jank/issues/1204

