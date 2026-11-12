# buggy jank

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
bb jank :args bb-jank!

# bug 1, which I assumed caused by using binding name the same name as a cpp type
bb jank :bug 1 # codegen error

# bug 2, which I assume include extra.hpp twice makes the c header libs have double implementation
# this was found because I was using this C header-only lib: https://github.com/prideout/par/blob/master/par_streamlines.h
# and I tried to mimic the usage of that lib with this setup
bb jank :bug 2 # this will work fine
bb compile :bug 2 # this compiles error 

# bug 3 is the same as bug 2 codewise, but the extra.hpp is only included once in file bug3.jank
# and this is also the same as bug 2, can be run with run-main, but not aot-compile (different error)
# so it's a catch 22, and refactor the c/cpp sources is the only way out for bug-2 and bug-3
bb jank :bug 3 # this will work fine
bb compile :bug 3 # this compiles error 
```
