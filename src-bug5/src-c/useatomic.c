#include <stdatomic.h>
#include <stdio.h>

typedef struct {
    long long a;
    long long b;
} Pair;
int main(void) {
    _Atomic Pair p = (Pair){10, 20};
    Pair expected = {10, 20};
    Pair desired = {30, 40};
    atomic_compare_exchange_strong(&p, &expected, desired);
    Pair current = atomic_load(&p);
    printf("Result: %lld, %lld\n", current.a, current.b);

    return 0;
}
