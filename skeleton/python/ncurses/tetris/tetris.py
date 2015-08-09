#!/usr/bin/env python3

import curses
import locale
import random
from threading import Thread, RLock
import time


# remember, `addstr` and `addch` are using different coordinates...


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
                scr.addstr(r, c, '  ', curses.A_REVERSE)


def draw_block(scr, row, col, name, rot):
    data = get_data(name, rot)
    for r, line in enumerate(data):
        for c, mark in enumerate(line):
            if mark > 0:
                scr.addstr(row+r, col+c*2, '  ', curses.A_REVERSE)


class CursesGame(Thread):
    """
    A game framework for curses
    """
    def __init__(self, main_screen):
        super().__init__(
            group=None, target=None,
            name='curses-game',
            args=(), kwargs=None, daemon=True)
        self.main_screen = main_screen
        self.height, self.width = main_screen.getmaxyx()
        self.stop = False
        self.fps = 0.0
        self.need_redraw = True

        self.event_queue_lock = RLock()
        self.event_queue = []

        # for current active block
        self.cur_rot = 0

        # win1 pos
        win1_height = self.height
        win1_width = divmod(self.width, 2)[0]-4
        win1_pos_row = 0
        win1_pos_col = 0
        self.win1 = curses.newwin(
            win1_height, win1_width,
            win1_pos_row, win1_pos_col)

        win2_height = self.height
        win2_width = divmod(self.width, 2)[0]-4
        win2_pos_row = 0
        win2_pos_col = win1_pos_col + win1_width + 1
        self.win2 = curses.newwin(
            win2_height, win2_width,
            win2_pos_row, win2_pos_col)

        # hide cursor
        curses.curs_set(0)
        # curses.cbreak()
        # self.main_screen.keypad(1)

    def run(self):
        delta = 0.0
        last_time = time.time()
        while not self.stop:
            self.update(delta)
            self.draw()
            time.sleep(0.1)
            l_time = time.time()
            delta, last_time = (l_time - last_time), l_time
            self.fps = 1/delta

    def read_input(self):
        ch = self.main_screen.getch()
        with self.event_queue_lock:
            self.event_queue.append(ch)
        self.need_redraw = True

    def update(self, delta):
        # print('delta={}, fps={}'.format(delta, self.fps))
        with self.event_queue_lock:
            event_queue = self.event_queue.copy()
            self.event_queue.clear()
        for ev in event_queue:
            if ev == ord(' '):
                self.cur_rot = (self.cur_rot+1) % 4
            elif ev == ord('q'):
                self.stop = True
        pass

    def draw(self):
        if not self.need_redraw:
            return
        self.need_redraw = False

        try:
            # self.main_screen.clear()
            # self.main_screen.border()
            self.win1.clear()
            self.win1.border()
            self.win2.clear()
            self.win2.border()
            draw_block(self.win1, 1+5, 1+5, 'T', self.cur_rot)
        finally:
            self.main_screen.refresh()
            self.win1.refresh()
            self.win2.refresh()


def main(screen):
    game = CursesGame(screen)
    game.start()
    while not game.stop:
        game.read_input()


if __name__ == '__main__':
    curses.wrapper(main)

# END
