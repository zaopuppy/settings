#!/usr/bin/env python3

from struct import pack, unpack, calcsize
from functools import singledispatch


@singledispatch
def encode(_):
    raise NotImplemented()


@singledispatch
def get_len(_):
    raise NotImplemented()


@encode.register(str)
def _(s):
    bs = s.encode('utf8')
    l = len(bs)
    return pack(('!H%ds' % l), l, bs)


@get_len.register(str)
def _(s):
    bs = s.encode('utf8')
    l = len(bs)
    return calcsize(('!I%ds' % l))


@encode.register(int)
def _(n):
    return pack('!I', n)


# TODO: :(
def decode_str(buf):
    l = calcsize('!H')
    str_len, = unpack('!H', buf[:l])
    buf = buf[l:]
    s, = unpack(('%ds' % str_len), buf[:str_len])
    return (l + str_len), s.decode('utf8')


VERSION = 1

TYPE_UNKNOWN = -1
TYPE_LOGIN = 0
TYPE_LOGIN_RSP = 1

CODE_FAIL = -1
CODE_SUCCESS = 0


class Header:
    def __init__(self, msg_type):
        self.ver_ = VERSION
        self.len_ = 0
        self.type_ = msg_type
        self.fmt_ = '!iii'

    def encode(self):
        return pack(self.fmt_, self.ver_, self.len_, self.type_)

    def decode(self, buf):
        decode_len = self.get_len()
        if len(buf) < decode_len:
            return -1
        self.ver_, self.len_, self.type_ = unpack(self.fmt_, buf[:decode_len])
        return decode_len

    def get_len(self):
        return calcsize(self.fmt_)

    def __str__(self, *args, **kwargs):
        return 'ver={}, len={}, type={}'.format(self.ver_, self.len_, self.type_)


class LoginMsg:
    def __init__(self, nickname=None, pass_code=None):
        self.header_ = Header(TYPE_LOGIN)
        self.nickname_ = nickname
        self.pass_code_ = pass_code

    def encode(self):
        data = encode(self.nickname_) + encode(self.pass_code_)
        self.header_.len_ = len(data)
        return self.header_.encode() + data

    def decode(self, buf):
        header_len = self.header_.decode(buf)
        buf = buf[header_len:]
        body_len = 0
        l, self.nickname_ = decode_str(buf[body_len:])
        body_len += l
        l, self.pass_code_ = decode_str(buf[body_len:])
        body_len += l
        return header_len + body_len

    # def get_len(self):
    #     return self.header_.get_len() + get_len(self.nickname_) + get_

    def __str__(self, *args, **kwargs):
        return 'header=[{}], nickname={}, pass_code={}'.format(self.header_, self.nickname_, self.pass_code_)


class LoginRsp:
    def __init__(self):
        self.header_ = Header(TYPE_LOGIN_RSP)
        self.code_ = CODE_FAIL

    def encode(self):
        return self.header_.encode() + pack('!i', self.code_)

    def decode(self, buf):
        header_len = self.header_.decode(buf)
        buf = buf[header_len:]

        body_len = 0
        self.code_, = unpack('!i', buf[body_len:])
        body_len = 4
        return header_len + body_len

    def __str__(self, *args, **kwargs):
        return 'header=[{}] code={}'.format(self.header_, self.code_)


def main():
    login = LoginMsg('zao', 'pass')
    data = login.encode()
    print(data)
    print(login.decode(data))
    print(str(login))

    print('-------------------------')
    login_rsp = LoginRsp()
    login_rsp.code_ = 0x44
    data = login_rsp.encode()
    print(data)
    print(login_rsp.decode(data))
    print(str(login_rsp))

if __name__ == '__main__':
    main()

# END
