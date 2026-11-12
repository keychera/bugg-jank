# buggy jank

need 
- babashka
- jank from commit [3b7ffc220fb8c65eecf4e777bfcc8e5252d30fb3](https://github.com/jank-lang/jank/commit/3b7ffc220fb8c65eecf4e777bfcc8e5252d30fb3) , 

the detailed error is inside comments of one of the files in respective src folders
```sh
bb jank

# bug 1
bb jank :bug 1 # codegen error

# bug 2
bb jank :bug 2 # this will work fine
bb compile :bug 2 # this compiles error 
```
