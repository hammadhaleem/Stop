import os
from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy
from flask.ext.login import LoginManager
from flask.ext.openid import OpenID
from config import basedir, ADMINS, MAIL_SERVER, MAIL_PORT, MAIL_USERNAME, \
    MAIL_PASSWORD


import sys
if sys.version_info >= (3, 0):
    enable_search = False
else:
    enable_search = True
    import flask.ext.whooshalchemy as whooshalchemy
from flask.ext.admin.contrib import sqla
from flask.ext.admin import Admin
import logging
from logging.handlers import RotatingFileHandler

from models import Goods, User
from app import views, models


class FedoraModelView(sqla.ModelView):
    column_display_pk = True
    column_display_pk = True

    
app = Flask(__name__)
app.config.from_object('config')
db = SQLAlchemy(app)
db.init_app(app) 
oid = OpenID(app, os.path.join(basedir, 'tmp'))



admin = Admin(app)
admin.add_view(FedoraModelView(User, db.session))
admin.add_view(FedoraModelView(Goods, db.session))
whooshalchemy.whoosh_index(app, Goods)

from app import views, models