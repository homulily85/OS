#include <stdio.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

int main(void)
{
    int N = 10;
    int fd[2];
    int depth = 0; 
    int i;
    pipe(fd);
    for (i = 0; i < N; i++)
    {
        if (fork() == 0)
        {                        
            write(fd[1], &i, 1);
            depth += 1;
        }
    }
    close(fd[1]);
    if (depth == 0)
    {
        i = 1;  // i = 0 if you want to count the number of child processes, 
                // 1 if you want to count the number of processes in the process tree (include the original process)
        while (read(fd[0], &depth, 1) != 0)
            i += 1;
        printf("%d total processes spawned\n", i);
    }

    return 0;
}