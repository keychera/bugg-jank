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
```sh
bb jank :bug 1 # codegen error
```

## bug 2
which I assume include extra.hpp twice makes the c header libs have double implementation
this was found because I was using this C header-only lib: https://github.com/prideout/par/blob/master/par_streamlines.h
and I tried to mimic the usage of that lib with this setup

*verdict: not a bug: this kind of C header-only lib that has impl need special treatment*
```sh
bb jank :bug 2 # this will work fine
bb compile :bug 2 # this compiles error 
```

## bug 3
this is still a bug, including once "fakeSDL.h" anywhere make run-main works, but not compile

*verdict: need a doc update: [link to doc](https://book.jank-lang.org/cpp-interop/cpp-raw.html#:~:text=Note-,Unlike%20in%20C)*
```sh
bb jank :bug 3 # this will work fine
bb compile :bug 3 # this compiles error 
```


# making static lib

https://stackoverflow.com/a/71011994

```sh
clang -c -o target/lib/hello.o -Isrc-bug4/include-c src-bug4/src-c/hello.c
ar rcs target/lib/libhello.a target/lib/hello.o
clang src-bug4/another-src-c/consumer.c -Ltarget/lib/ -Isrc-bug4/include-c -lhello -o target/main
```

