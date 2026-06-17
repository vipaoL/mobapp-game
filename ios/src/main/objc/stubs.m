#include <sys/select.h>

int __darwin_check_fd_set_overflow(int fd, const void *set, int temp) {
    return 0;
}
