#!/usr/bin/env python3

import curses
import locale
import random


def init():
    locale.setlocale(locale.LC_ALL, 'C')

    stdscr = curses.initscr()

    curses.noecho()
    curses.cbreak()
    curses.curs_set(0)

    stdscr.keypad(1)

    return stdscr


def clear_and_quit(scr):
    curses.echo()
    curses.nocbreak()
    curses.curs_set(1)

    scr.keypad(0)

    curses.endwin()


def random_image(scr, width_start, width_end, height_start, height_end):
    for r in range(height_start, height_end):
        for c in range(width_start, width_end-2, 2):
            if random.randint(0, 1) == 1:
                # remember, `addstr` and `addch` are using different coordinates...
                scr.addstr(r, c, '  ', curses.A_REVERSE)


# I, J, L, O, T, Z, S
#
# I = ((1, 1, 1, 1),)
#
# J = ((1, 0, 0),
#      (1, 1, 1))
#
# L = ((0, 0, 1),
#      (1, 1, 1))
#
# O = ((1, 1),
#      (1, 1))
#
# T = ((0, 1, 0),
#      (1, 1, 1))
#
# Z = ((1, 1, 0),
#      (0, 1, 1))
#
# S = ((0, 1, 1),
#      (1, 1, 0))
def draw(block):
    pass


def main():
    scr = init()
    try:
        # (55, 178), y = height, x = width
        height, width = scr.getmaxyx()
        height, width = height-1, width-1
        # random_image(scr, width, height)
        scr.border()
        # random_image(scr, 1, width, 1, height)
    finally:
        scr.getch()
        clear_and_quit(scr);


if __name__ == '__main__':
    main()


