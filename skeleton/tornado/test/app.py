# coding: utf-8


from tornado.web import StaticFileHandler, RequestHandler
from tornado.wsgi import WSGIApplication

import os.path


class MyHandler(RequestHandler):
    def get(self, *args, **kwargs):
        return self.redirect('/index.html')
        # return self.write('hello')


current_path = os.path.dirname(__file__)


settings = {
    'static_path': os.path.join(current_path, "static"),
    'template_path': os.path.join(current_path, "templates"),
    'gzip': True,
    'debug': True,
}


handlers = [
    (r'/', MyHandler),
    (r'/(.*\..*)', StaticFileHandler, {'path': settings['static_path']}),
]


app = WSGIApplication(handlers, **settings)


# from datetime import datetime
#
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
