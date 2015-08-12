#!/usr/bin/env python3

import curses
import random
from threading import Thread, RLock
import time
import sys
import select


# TODO: send log to a socket instead of a real file
# nc -kl 1983
import socket
try:
    log_server_socket = socket.create_connection(('localhost', 1983), timeout=0.5)
except ConnectionRefusedError:
    log_server_socket = None


def log(msg):
    print(msg, file=sys.stderr)
    if log_server_socket is not None:
        log_server_socket.send(msg.encode('utf8') + b'\n')


PLAYGROUND_WIDTH = 15
PLAYGROUND_HEIGHT = 20

BLOCK_NAME_LIST = ('O', 'J', 'L', 'Z', 'S', 'T', 'I',)

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
def get_block_data(name, rot):
    return tuple(g[rot] for g in BLOCK_DATA.get(name))


def random_image(scr, width_start, width_end, height_start, height_end):
    for r in range(height_start, height_end):
        for c in range(width_start, width_end-2, 2):
            if random.randint(0, 1) == 1:
                # remember
                # `addstr` and `addch` are using different coordinates...
                scr.addstr(r, c, '  ', curses.A_REVERSE)


def draw_block(scr, row, col, name, rot):
    data = get_block_data(name, rot)
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
        pass
        # self.process_key(game, curses.KEY_DOWN)

    def process_key(self, game, key):
        if key == curses.KEY_UP or \
                key == curses.KEY_DOWN or \
                key == curses.KEY_LEFT or \
                key == curses.KEY_RIGHT:
            new_pos_row, new_pos_col, new_rot = 0, 0, 0
            if key == curses.KEY_UP:
                game.user_log('UP')
                new_pos_row = game.cur_pos_row_
                new_pos_col = game.cur_pos_col_
                new_rot = (game.cur_rot_+1) % 4
            elif key == curses.KEY_DOWN:
                game.user_log('DOWN')
                new_pos_row = game.cur_pos_row_ + 1
                new_pos_col = game.cur_pos_col_
                new_rot = game.cur_rot_
            elif key == curses.KEY_LEFT:
                game.user_log('LEFT')
                new_pos_row = game.cur_pos_row_
                new_pos_col = game.cur_pos_col_ - 1
                new_rot = game.cur_rot_
            elif key == curses.KEY_RIGHT:
                game.user_log('RIGHT')
                new_pos_row = game.cur_pos_row_
                new_pos_col = game.cur_pos_col_ + 1
                new_rot = game.cur_rot_
            if can_put(
                    game.map_data_,
                    new_pos_row, new_pos_col, game.cur_type_, new_rot):
                log('row={}, col={}, type={}, rot={}'.format(
                    new_pos_row, new_pos_col, game.cur_type_, new_rot
                ))
                game.cur_pos_row_ = new_pos_row
                game.cur_pos_col_ = new_pos_col
                game.cur_rot_ = new_rot
                game.need_redraw_ = True
            else:
                if key == curses.KEY_DOWN:
                    log('bottom touched, fix block and spawn a new one')
                    put_block(
                        game.map_data_,
                        game.cur_pos_row_, game.cur_pos_col_,
                        game.cur_type_, game.cur_rot_)
                    check_full_row(game.map_data_)
                    game.spawn_new_block()
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

        self.log_list_ = []
        self.log_list_max_len_ = 6

        score_panel_width = 10+2
        score_panel_height = PLAYGROUND_HEIGHT + 2
        scene_width = PLAYGROUND_WIDTH*2 + 2
        scene_height = PLAYGROUND_HEIGHT + 2
        log_panel_height = self.log_list_max_len_+2

        self.main_screen_ = main_screen
        self.height_, self.width_ = main_screen.getmaxyx()

        min_height = max(scene_height, score_panel_height) + log_panel_height + 5
        min_width = score_panel_width + scene_width*2 + 5
        if self.height_ < min_height or self.width_ < min_width:
            raise Exception('screen size must be {}*{} or larger'.format(min_width, min_height))

        self.map_data_ = []
        for r in range(PLAYGROUND_HEIGHT):
            self.map_data_.append([0 for _ in range(PLAYGROUND_WIDTH)])

        self.stop_ = False
        self.fps_ = 0.0
        self.need_redraw_ = True

        self.event_queue_lock_ = RLock()
        self.event_queue_ = []

        # for current active block
        self.cur_rot_ = 0
        self.cur_type_ = 'T'
        self.cur_pos_row_ = 0
        self.cur_pos_col_ = 6
        self.spawn_new_block()

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

        # log_win
        log_win_pos_row = max(scene_height, score_panel_height)
        log_win_pos_col = 0
        log_win_height = log_panel_height
        log_win_width = scene_width*2 + score_panel_width + 2
        self.log_win_ = curses.newwin(
            log_win_height, log_win_width,
            log_win_pos_row, log_win_pos_col
        )

        self.state_ = Playing()

        # hide cursor
        curses.curs_set(0)
        # curses.cbreak()
        # self.main_screen.keypad(1)

    def user_log(self, msg):
        if len(self.log_list_) < self.log_list_max_len_:
            self.log_list_.append(msg)
        else:
            self.log_list_ = self.log_list_[1:] + [msg]

    def spawn_new_block(self):
        self.cur_type_ = random.choice(BLOCK_NAME_LIST)
        self.cur_rot_ = random.choice((0, 1, 2, 3))
        self.cur_pos_row_ = 0
        self.cur_pos_col_ = 6

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
            self.log_win_.clear()
            self.log_win_.border()

            offset = 1

            for r, line in enumerate(self.map_data_):
                for c, mark in enumerate(line):
                    if mark > 0:
                        self.win1_.addstr(r+offset, c*2+offset, '  ', curses.A_REVERSE)

            draw_block(
                self.win1_,
                self.cur_pos_row_+offset, self.cur_pos_col_*2+offset,
                self.cur_type_, self.cur_rot_)

            for i in range(len(self.log_list_)):
                self.log_win_.addstr(1+i, 1+0, self.log_list_[i])
        finally:
            self.main_screen_.refresh()
            self.win1_.refresh()
            self.score_panel_.refresh()
            self.win2_.refresh()
            self.log_win_.refresh()


def in_map(row, col):
    if row < 0 or row >= PLAYGROUND_HEIGHT:
        return False
    if col < 0 or col >= PLAYGROUND_WIDTH:
        return False
    return True


def can_put(map_data, row, col, name, rot):
    data = get_block_data(name, rot)
    for r, line in enumerate(data):
        for c, mark in enumerate(line):
            if mark > 0:
                if not in_map(row+r, col+c) or map_data[row+r][col+c] > 0:
                    return False
    return True


def fall_map(map_data):
    first_empty_row = -1
    for r in range(PLAYGROUND_HEIGHT-1, -1, -1):
        if not is_empty_row(map_data[r]):
            if first_empty_row >= 0:
                # do move
                map_data[first_empty_row], map_data[r] = map_data[r], map_data[first_empty_row]
                first_empty_row -= 1
        else:
            if first_empty_row < 0:
                first_empty_row = r


def check_full_row(map_data):
    full_line_num = 0
    for r, line in enumerate(map_data):
        if is_full_row(line):
            empty_row(line)
            full_line_num += 1
    fall_map(map_data)
    return full_line_num


def is_full_row(row):
    for e in row:
        if e == 0:
            return False
    return True


def is_empty_row(row):
    for e in row:
        if e != 0:
            return False
    return True


def empty_row(row):
    for i in range(len(row)):
        row[i] = 0


def put_block(map_data, row, col, name, rot):
    data = get_block_data(name, rot)
    for r, line in enumerate(data):
        for c, mark in enumerate(line):
            if mark > 0:
                map_data[row+r][col+c] = mark


# def default_client_handler(s, data):
#     log('received: [' + data.decode('utf8') + ']')


client_count_ = 0
class MultiPlayer:

    def __init__(self, game):
        self.game = game
        log(str(game))

    def on_connected(self, s):
        global client_count_
        log('on_connected: ' + str(s.getpeername()))
        log('client count: %d' % client_count_)
        if client_count_ > 0:
            log('max client count reached, force close')
            s.close()
        else:
            client_count_ += 1

    def on_data(self, s, data):
        log('received: [' + str(data) + ']')

    def on_disconnected(self, s):
        global client_count_
        log('on_disconnected: ' + str(s.getpeername()))
        client_count_ -= 1


class Server:
    """
    network server
    """
    def __init__(self, callback_type, **kwargs):
        self.callback_type_ = callback_type
        self.callback_kwargs_ = kwargs

    def listen(self, port=4444):
        server_socket = socket.socket()
        server_socket.setblocking(False)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind(('0.0.0.0', port))
        server_socket.listen(5)

        client_map = {}

        in_fds = [server_socket]
        out_fds = []
        while True:
            readable, writable, exceptional =\
                select.select(in_fds, out_fds, in_fds, 0.2)
            if not readable and not writable and not exceptional:
                continue

            for s in readable:
                if s is server_socket:
                    conn, addr = server_socket.accept()
                    log('connection from: ' + str(addr))
                    conn.setblocking(False)
                    callback = self.callback_type_(self.callback_kwargs_)
                    callback.on_connected(conn)
                    if conn.fileno() >= 0:
                        client_map[conn] = callback
                        in_fds.append(conn)
                else:
                    callback = client_map[s]
                    data = s.recv(2048)
                    if not data:
                        callback.on_disconnected(s)
                        log('connection closed: ' + str(s.getpeername()))
                        if s in in_fds:
                            in_fds.remove(s)
                        s.close()
                        client_map.pop(s)
                    else:
                        callback.on_data(s, data)
                        if s.fileno() < 0:
                            if s in in_fds:
                                in_fds.remove(s)
                            client_map.pop(s)

            for s in exceptional:
                log('except from: ' + s.getpeername())
                callback = client_map.pop(s)
                callback.on_disconnected(s)
                if s in in_fds:
                    in_fds.remove(s)
                s.close()


def main(screen):
    game = CursesGame(screen)
    game.start()
    while not game.stop_:
        game.read_input()


if __name__ == '__main__':
    log('just a test')
    # curses.wrapper(main)
    server = Server(MultiPlayer, game='123')
    server.listen()

# END
