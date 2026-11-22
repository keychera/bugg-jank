# jank cant find .a static libs

jank can't find static .a libraries produces by msys2/clang64 toolchain (the toolchain used to compile jank in windows)

## comparing to clang++

how to make static lib learned from here
https://stackoverflow.com/a/71011994

clang++ can consume .a lib in windows

```sh
mkdir -p target/lib
clang -c -o target/lib/hello.o -Iinclude-c src-c/hello.c -fPIC
ar r target/lib/hello.a target/lib/hello.o
clang++ another-src-cpp/consumer.cpp -Ltarget/lib/ -Iinclude-c -lhello -o target/main

./target/main.exe 
# Hello from consumer of libraries
# Hello static lib
```

jank can't find them

jank
```sh
mkdir -p target/lib
clang -c -o target/lib/hello.o -Iinclude-c src-c/hello.c -fPIC
ar r target/lib/hello.a target/lib/hello.o

jank --module-path src-jank -Ltarget/lib/ -Iinclude-c -lhello --name bermain-static compile bermain.static
# ─ system/failure ───────────────────────────────────────────────────────────────────────────────────
# error: Failed to find library 'hello'.
# 
# https://book.jank-lang.org/reference/error/system/failure.html
```

I tried this patch and it solves my problem but unsure if this is the general solution for windows.

https://github.com/keychera/jank/commit/cf6c891cda7155088483874cf6294617c2edc09b

specifically this patch fixes my static linking to SDL libraries for my jank game. with jank-build.bb here:
https://github.com/keychera/autumn-jank/blob/master/jank-build.bb


extra note:
I also tried patching jank to make it only find .a and not .lib but compilation fails with different error. specifically

<details>
<summary>Error during static linking to SDL using jank patch to only find .a static lib in windows</summary>

## Error during static linking to SDL using jank patch to only find .a static lib in windows

```sh
JIT session error: Symbols not found: [ itoa ]
JIT session error: Failed to materialize symbols: { (main, { SDL_strcmp_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_wcslen_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_strcmp_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_strcmp_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_vasprintf_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { OnAudioStreamDestroy }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_memcmp_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_strcmp_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_strcmp_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_HelperWindowDestroy }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetIOSize_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_DestroyHashTable }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_UpdateAudio }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_MinimizeWindow_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetNumberProperty_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_atoi_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_vsnprintf_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_strchr_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_UnlockRWLock_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_DestroyHashTable }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_PushEvent_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetNumberProperty_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_malloc_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_ReadIO_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_calloc_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_strcmp_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_LockMutex_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_Delay_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_strlen_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetSurfaceBlendMode_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { WIN_CheckDefaultArgcArgv }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetPersistentString }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetIOSize_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { WIN_CoInitialize }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_AddKeyboard }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_GetTLS_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_roundf_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_SetError_REAL }) }
JIT session error: Failed to materialize symbols: { (main, { SDL_isspace_REAL }) }
```

</details>
