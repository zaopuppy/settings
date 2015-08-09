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
    return tuple(g[rot] for g in BLOCK_DATA.get(name))


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


class Playing:
    """
    playing
    """
    def __init__(self):
        self.step_time_ = 0

    def update(self, game, delta):
        with game.event_queue_lock_:
            event_queue = game.event_queue_.copy()
            game.event_queue_.clear()
        for ev_type, ev_value in event_queue:
            if ev_type == 'key':
                self.process_key(game, ev_value)
            else:
                raise Exception('unknown event: {}'.format(ev_type))

        self.step_time_ += delta
        if self.step_time_ >= 0.5:
            self.step_time_ = divmod(self.step_time_, 0.5)[1]
            self.process_step_time_up(game)

    def process_step_time_up(self, game):
        self.process_key(game, curses.KEY_DOWN)

    def process_key(self, game, key):
        if key == curses.KEY_UP:
            game.cur_rot_ = (game.cur_rot_+1) % 4
            game.need_redraw_ = True
        elif key == curses.KEY_DOWN:
            game.cur_pos_row_ += 1
            game.need_redraw_ = True
        elif key == curses.KEY_LEFT:
            game.cur_pos_col_ -= 2
            game.need_redraw_ = True
        elif key == curses.KEY_RIGHT:
            game.cur_pos_col_ += 2
            game.need_redraw_ = True
        elif key == ord('q'):
            game.stop_ = True


class CursesGame(Thread):
    """
    A game framework for curses
    """
    def __init__(self, main_screen):
        super().__init__(
            group=None, target=None,
            name='curses-game',
            args=(), kwargs=None, daemon=True)

        score_panel_width = 10+2
        score_panel_height = 30
        scene_width = 15*2 + 2
        scene_height = 30
        log_panel_height = 10

        self.main_screen_ = main_screen
        self.height_, self.width_ = main_screen.getmaxyx()

        min_height = max(scene_height, score_panel_height) + log_panel_height + 5
        min_width = score_panel_width + scene_width*2 + 5
        if self.height_ < min_height or self.width_ < min_width:
            raise Exception('screen size should be {}*{} or larger'.format(min_width, min_height))

        self.stop_ = False
        self.fps_ = 0.0
        self.need_redraw_ = True

        self.event_queue_lock_ = RLock()
        self.event_queue_ = []

        # for current active block
        self.cur_rot_ = 0
        self.cur_type_ = 'T'
        self.cur_pos_row_ = 1
        self.cur_pos_col_ = 10

        # win1 -- player1
        win1_pos_row = 0
        win1_pos_col = 0
        win1_height = scene_height
        win1_width = scene_width
        self.win1_ = curses.newwin(
            win1_height, win1_width,
            win1_pos_row, win1_pos_col)

        # score panel
        score_panel_pos_row = 0
        score_panel_pos_col = win1_pos_col + win1_width + 1
        score_panel_height = score_panel_height
        score_panel_width = score_panel_width
        self.score_panel_ = curses.newwin(
            score_panel_height, score_panel_width,
            score_panel_pos_row, score_panel_pos_col)

        # win2 -- player2
        win2_pos_row = 0
        win2_pos_col = score_panel_pos_col + score_panel_width + 1
        win2_height = scene_height
        win2_width = scene_width
        self.win2_ = curses.newwin(
            win2_height, win2_width,
            win2_pos_row, win2_pos_col)

        self.state_ = Playing()

        # hide cursor
        curses.curs_set(0)
        # curses.cbreak()
        # self.main_screen.keypad(1)

    def run(self):
        delta = 0.0
        last_time = time.time()
        while not self.stop_:
            self.update(delta)
            self.draw()
            # to avoid busy loop
            time.sleep(0.05)
            l_time = time.time()
            delta, last_time = (l_time - last_time), l_time
            self.fps_ = 1/delta

    def read_input(self):
        ch = self.main_screen_.getch()
        with self.event_queue_lock_:
            self.event_queue_.append(('key', ch))

    def update(self, delta):
        # print('delta={}, fps={}'.format(delta, self.fps))
        self.state_.update(self, delta)

    def draw(self):
        if not self.need_redraw_:
            return
        self.need_redraw_ = False

        try:
            self.win1_.clear()
            self.win1_.border()
            self.score_panel_.clear()
            self.score_panel_.border()
            self.win2_.clear()
            self.win2_.border()

            draw_block(
                self.win1_,
                self.cur_pos_row_, self.cur_pos_col_,
                self.cur_type_, self.cur_rot_)
        finally:
            self.main_screen_.refresh()
            self.win1_.refresh()
            self.score_panel_.refresh()
            self.win2_.refresh()


def main(screen):
    game = CursesGame(screen)
    game.start()
    while not game.stop_:
        game.read_input()


if __name__ == '__main__':
    curses.wrapper(main)

# END
