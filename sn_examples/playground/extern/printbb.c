#include "printbb.h"

#define MAX(a, b) (((a) > (b)) ? (a) : (b))

#include <string.h>
#include <stdio.h>

void printbb(unsigned int *bb, char *b1, char *b2, char *b3, char *b4, char *b5, char *b6, char *b7, char *b8) {
    unsigned int padlen = MAX(strlen(b1), strlen(b2));
    unsigned int rowlen = 0;

    padlen = MAX(padlen, strlen(b3));
    padlen = MAX(padlen, strlen(b4));
    padlen = MAX(padlen, strlen(b5));
    padlen = MAX(padlen, strlen(b6));
    padlen = MAX(padlen, strlen(b7));
    padlen = MAX(padlen, strlen(b8));

    rowlen += (padlen * 8) + (3 * 8) + 1;

    for (int i = 0; i < rowlen; i++) { printf("-"); }
    printf("\n");

    printf("| %0*s ", padlen, b1);
    printf("| %0*s ", padlen, b2);
    printf("| %0*s ", padlen, b3);
    printf("| %0*s ", padlen, b4);
    printf("| %0*s ", padlen, b5);
    printf("| %0*s ", padlen, b6);
    printf("| %0*s ", padlen, b7);
    printf("| %0*s |\n", padlen, b8);

    unsigned v = (*bb);
    char b8v = v & 0x80 ? '1' : '0';
    char b7v = v & 0x40 ? '1' : '0';
    char b6v = v & 0x20 ? '1' : '0';
    char b5v = v & 0x10 ? '1' : '0';
    char b4v = v & 0x08 ? '1' : '0';
    char b3v = v & 0x04 ? '1' : '0';
    char b2v = v & 0x02 ? '1' : '0';
    char b1v = v & 0x01 ? '1' : '0';

    printf("| %0*c ", padlen, b1v);
    printf("| %0*c ", padlen, b2v);
    printf("| %0*c ", padlen, b3v);
    printf("| %0*c ", padlen, b4v);
    printf("| %0*c ", padlen, b5v);
    printf("| %0*c ", padlen, b6v);
    printf("| %0*c ", padlen, b7v);
    printf("| %0*c |\n", padlen, b8v);

    for (int i = 0; i < rowlen; i++) { printf("-"); }
    printf("\n");

}