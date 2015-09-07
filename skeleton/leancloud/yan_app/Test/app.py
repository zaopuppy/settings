# coding: utf-8

import os

import tornado
import tornado.web
import tornado.wsgi

from tornado.web import RequestHandler
from tornado.web import StaticFileHandler

from datetime import datetime

CURRENT_PATH = os.path.dirname(__file__)


class MyStaticHandler(RequestHandler):



settings = {
    'static_path': os.path.join(CURRENT_PATH, 'public'),
    'template_path': os.path.join(CURRENT_PATH, 'templates'),
    'gzip': True,
    'debug': True,
}

handlers = [
    (r'/', StaticFileHandler, {'path': os.path.join(CURRENT_PATH, 'index.html')}),
    (r'/(.*\..*)', StaticFileHandler),
]

app = tornado.wsgi.WSGIApplication(handlers, **settings)

# from flask import Flask
# from flask import render_template
#
# from views.todos import todos_view
#
# app = Flask(__name__)
#
# # 动态路由
# app.register_blueprint(todos_view, url_prefix='/todos')
#
#
# @app.route('/')
# def index():
#     return render_template('index.html')
#
#
# @app.route('/time')
# def time():
#     return str(datetime.now())

