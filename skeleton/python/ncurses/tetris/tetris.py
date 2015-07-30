#!/usr/bin/env python3

import curses
import locale
import random

g_screen = None
g_win1 = None
g_rot = 0

BLOCK_DATA = {
    'O': (((1, 1, 0, 0), (1, 1, 0, 0), (1, 1, 0, 0), (1, 1, 0, 0)),
          ((1, 1, 0, 0), (1, 1, 0, 0), (1, 1, 0, 0), (1, 1, 0, 0)),
          ((0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0)),
          ((0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0))),
    'J': (((2, 2, 2, 0), (0, 2, 0, 0), (2, 0, 0, 0), (2, 2, 0, 0)),
          ((0, 0, 2, 0), (0, 2, 0, 0), (2, 2, 2, 0), (2, 0, 0, 0)),
          ((0, 0, 0, 0), (2, 2, 0, 0), (0, 0, 0, 0), (2, 0, 0, 0)),
          ((0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0))),
    'L': (((3, 3, 3, 0), (3, 3, 0, 0), (0, 0, 3, 0), (3, 0, 0, 0)),
          ((3, 0, 0, 0), (0, 3, 0, 0), (3, 3, 3, 0), (3, 0, 0, 0)),
          ((0, 0, 0, 0), (0, 3, 0, 0), (0, 0, 0, 0), (3, 3, 0, 0)),
          ((0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0))),
    'Z': (((4, 4, 0, 0), (0, 4, 0, 0), (4, 4, 0, 0), (0, 4, 0, 0)),
          ((0, 4, 4, 0), (4, 4, 0, 0), (0, 4, 4, 0), (4, 4, 0, 0)),
          ((0, 0, 0, 0), (4, 0, 0, 0), (0, 0, 0, 0), (4, 0, 0, 0)),
          ((0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0))),
    'S': (((0, 5, 5, 0), (5, 0, 0, 0), (0, 5, 5, 0), (5, 0, 0, 0)),
          ((5, 5, 0, 0), (5, 5, 0, 0), (5, 5, 0, 0), (5, 5, 0, 0)),
          ((0, 0, 0, 0), (0, 5, 0, 0), (0, 0, 0, 0), (0, 5, 0, 0)),
          ((0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0))),
    'T': (((0, 6, 0, 0), (6, 0, 0, 0), (6, 6, 6, 0), (0, 6, 0, 0)),
          ((6, 6, 6, 0), (6, 6, 0, 0), (0, 6, 0, 0), (6, 6, 0, 0)),
          ((0, 0, 0, 0), (6, 0, 0, 0), (0, 0, 0, 0), (0, 6, 0, 0)),
          ((0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0), (0, 0, 0, 0))),
    'I': (((7, 7, 7, 7), (7, 0, 0, 0), (7, 7, 7, 7), (7, 0, 0, 0)),
          ((0, 0, 0, 0), (7, 0, 0, 0), (0, 0, 0, 0), (7, 0, 0, 0)),
          ((0, 0, 0, 0), (7, 0, 0, 0), (0, 0, 0, 0), (7, 0, 0, 0)),
          ((0, 0, 0, 0), (7, 0, 0, 0), (0, 0, 0, 0), (7, 0, 0, 0))),
    }

# rot = { 0, 1, 2, 3 }
def get_data(name, rot):
    groups = BLOCK_DATA.get(name)
    return tuple(g[rot] for g in groups)


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


def draw_block(scr, row, col, name, rot):
    data = get_data(name, rot)
    for r, line in enumerate(data):
        for c, mark in enumerate(line):
            if mark > 0:
                scr.addstr(1+row+r, 1+col+c*2, '  ', curses.A_REVERSE)


def draw():
    g_screen.clear()
    g_screen.border()
    g_win1.border()
    draw_block(g_screen, 0, 0, 'T', g_rot)


def main_old():
    global g_screen, g_rot
    global g_win1

    g_screen = init()
    try:
        # (55, 178), y = height, x = width
        height, width = g_screen.getmaxyx()
        height, width = height-1, width-1

        g_win1 = curses.newwin(10, 10, 20, 20)
        g_win1.refresh()

        while True:
            draw()
            ch = g_screen.getch()
            if ch == ord('q'):
                break
            elif ch == ord(' '):
                _, g_rot = divmod(g_rot+1, 4)
    finally:
        clear_and_quit(g_screen);


def main(stdscr):
    global g_screen, g_rot
    global g_win1

    curses.curs_set(0)

    g_screen = stdscr

    # (55, 178), y = height, x = width
    height, width = g_screen.getmaxyx()
    height, width = height-1, width-1

    g_win1 = curses.newwin(10, 10, 20, 20)
    g_win1.refresh()

    while True:
        draw()
        ch = g_screen.getch()
        if ch == ord('q'):
            break
        elif ch == ord(' '):
            _, g_rot = divmod(g_rot+1, 4)


if __name__ == '__main__':
    curses.wrapper(main)


