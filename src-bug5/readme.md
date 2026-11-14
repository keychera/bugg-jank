
When trying to compile SDL3-ttf in a lein-jank project, I encounter that one of the flag from it `-latomic` fails the compilation.

So I tested this atomic lib in isolation and found 2 janky behaviour.
1. libatomic can't be linked with jank flags, but works with JANK_EXTRA_FLAGS
2. linking with JANK_EXTRA_FLAGS only works the first compile command, subsequent command error unless target/_cache is deleted

maybe could be repro'd in any linux distros that have [libatomic1](https://packages.debian.org/sid/libatomic1), I only tested inside [steamrt4 SDK](https://gitlab.steamos.cloud/steamrt/steamrt4/sdk/-/blob/steamrt/steamrt4/README.md?ref_type=heads)
```sh
distrobox create -i registry.gitlab.steamos.cloud/steamrt/steamrt4/sdk steamrt4
distrobox enter steamrt4
```

inside src-bug5, need c,c++,jank compiler

```sh
mkdir target
clang src-c/useatomic.c -latomic -o target/useatomic

clang++ src-c/useatomic.cpp -latomic -o target/useatomic-cpp 
```
both command above runs, and each binnary runs ok
```sh
./target/useatomic
./target/useatomic-cpp
```

however, jank cannot link to it unless it is passed via JANK_EXTRA_FLAGS

```sh
jank --module-path src-jank -latomic --name jankatomic compile useatomic
# ─ system/failure ───────────────────────────────────────────────────────────────────────────────────
# error: Failed to find library 'atomic'.
# 
# https://book.jank-lang.org/reference/error/system/failure.html

export JANK_EXTRA_FLAGS="-latomic"

jank --module-path src-jank --name jankatomic compile useatomic
```

this runs
```sh
./target/jankatomic 
```

and there is another bug, running the compile command above the second time will throw error. And target/_cache need to be deleted to fix the error.

```sh
jank --module-path src-jank --name jankatomic compile useatomic
# JIT session error: Symbols not found: [ __atomic_load_16, __atomic_compare_exchange ]
# ─ runtime/uncaught-exception ───────────────────────────────────────────────────────────────────────
# error: Failed to find symbol: 'jank_load_useatomic'
# 
# https://book.jank-lang.org/reference/error/runtime/uncaught-exception.html
# Stack trace (most recent call first):
# #0  in jank::error::throw_result_failure at jank
# #1  in jtl::detail::panic at jank
# #2  in jank::runtime::module::loader::load_o at jank
# #3  in jank::runtime::module::loader::load at jank
# #4  in jank::runtime::context::load_module at jank
# #5  in jank::runtime::context::compile_module at jank
# #6  in jank::compile at jank
# #7  in main::$_0::__invoke at jank
# #8  in jank_init_dynamic at jank
# #9  at libc.so.6
# #10 in __libc_start_main at libc.so.6jank
# #6  in jank::compile at jank
# #7  in main::$_0::__invoke at jank
# #8  in jank_init_dynamic at jank
# #9  at libc.so.6
# #10 in __libc_start_main at libc.so.6

```
