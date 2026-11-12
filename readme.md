# buggy jank

need 
- babashka, at least v1.13.222
- jank, at least from commit [3b7ffc220fb8c65eecf4e777bfcc8e5252d30fb3](https://github.com/jank-lang/jank/commit/3b7ffc220fb8c65eecf4e777bfcc8e5252d30fb3) 

small explanation about the tasks:
`bb jank` is a bb script that builds `jank run-main` command
`bb compile` is a bb script that builds `jank compile` command
both task receive `:bug` flag accepting integer, this will select which alias to use from deps.edn

the detailed error is inside comments of one of the files in respective src folders
```sh
bb jank

# bug 1, which I assumed caused by using binding name the same name as a cpp type
bb jank :bug 1 # codegen error

# bug 2, which I assume include extra.hpp twice makes the c header libs have double implementation
bb jank :bug 2 # this will work fine
bb compile :bug 2 # this compiles error 
```
