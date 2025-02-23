/**
 * Complie and run using
 * gcc -Wall -Wextra -O2 process_tree_created_by_folk.c -o process_tree_created_by_folk && ./process_tree_created_by_folk | dot -Tpng > out.png 
 */

#define _POSIX_C_SOURCE 200809L
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>

int process(const unsigned int level, const unsigned int maxlevel, FILE *dot)
{
    int status = EXIT_SUCCESS, childstatus;
    unsigned int children, i;
    pid_t p, child[2];

    if (dot)
    {
        fprintf(dot, "    \"%ld\" [ label=\"Process %ld\" ];\n", (long)getpid(), (long)getpid());

        if (level)
            fprintf(dot, "    \"%ld\" -> \"%ld\";\n", (long)getppid(), (long)getpid());

        fflush(dot);
    }

    if (level >= maxlevel)
    {
        if (level)
            exit(status);
        else
            return status;
    }

    if (level & 1)
        children = 2;
    else
        children = 1;

    for (i = 0; i < children; i++)
    {
        child[i] = fork();
        if (child[i] == -1)
        {
            fprintf(stderr, "Cannot fork: %s.\n", strerror(errno));
            exit(EXIT_FAILURE);
        }
        else if (!child[i])
        {
            exit(process(level + 1, maxlevel, dot));
        }
    }

    for (i = 0; i < children; i++)
    {
        if (child[i] != -1)
        {
            do
            {
                p = waitpid(child[i], &childstatus, 0);
            } while (p == -1 && errno == EINTR);
            if (p != child[i])
                status = EXIT_FAILURE;
        }
        else
            status = EXIT_FAILURE;
    }

    if (level)
        exit(status);
    else
        return status;
}

int dot_process_tree(const int levels, FILE *out)
{
    int retval = EXIT_SUCCESS;

    if (out)
    {
        fprintf(out, "digraph {\n");
        fflush(out);
    }

    if (levels > 0)
        retval = process(0, levels - 1, out);

    if (out)
    {
        fprintf(out, "}\n");
        fflush(out);
    }

    return retval;
}

int main(void)
{
    int N = 10;
    return dot_process_tree(N, stdout);
}