#include <sys/select.h>
#include <stdlib.h>

int __darwin_check_fd_set_overflow(int fd, const void *set, int temp) {
    return 0;
}

extern int GC_expand_hp(size_t bytes);

__attribute__((constructor))
static void init_robovm_gc_tweaks(void) {
    setenv("GC_DONT_GC", "1", 1); // disable GC to manage it manually
    GC_expand_hp(64 * 1024 * 1024); // 64 MB
}