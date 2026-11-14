#include <atomic>
#include <iostream>

struct Pair { Pair(long a = 0, long b = 0): a(a), b(b) {}; long a, b;};

int main() {
    std::atomic<Pair> pair(Pair(1, 2));
    Pair expected{1, 2};
    Pair desired{5, 6};
    pair.compare_exchange_strong(expected, desired);
    Pair res = pair.load();
    std::cout << "Result: " << res.a << ", " << res.b << "\n";

    return 0;
}
