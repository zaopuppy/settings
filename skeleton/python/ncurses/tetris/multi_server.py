#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import socket
import select
import time


def log(msg):
    print(msg)


class Server:
    """
    network server
    """
    def __init__(self, callback_type, callback_args=()):
        self.callback_type_ = callback_type
        self.callback_args_ = callback_args
        # self.logger = logger

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
                    callback = self.callback_type_(**self.callback_args_)
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


class ClientHandler:
    """
    pass
    """
    def __init__(self):
        pass

    def on_connected(self, s):
        pass

    def on_disconnected(self, s):
        pass

    def on_data(self, s, data):
        pass


class StdioInputSource:
    def __init__(self):
        pass

    def read_input(self):
        # c = ncurse.getch()
        pass


class NetworkInputSource:
    def __init__(self):
        pass

    def read_input(self):
        pass


class Game:
    def __init__(self):
        self.server_ = Server()
        self.server_.start()
        self.input_source_ = [
            StdioInputSource(),
            NetworkInputSource(self.server_),
        ]
        self.stop_ = False

    def start(self):
        while not self.stop_:
            time.sleep(1)


def accept():
    allocate_slot()


def as_a_game(new_income):
    if not_full:
        accept()
    else:
        keep_it_wait()


def main():
    client_handler = ClientHandler()
    server = Server(client_handler)
    server.listen()


if __name__ == "__main__":
    pass
