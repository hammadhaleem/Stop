from hashlib import md5
from app import db
from app import app

import sys
if sys.version_info >= (3, 0):
    enable_search = False
else:
    enable_search = True
    import flask.ext.whooshalchemy as whooshalchemy

class User(db.Model):

    id = db.Column(db.Integer, primary_key=True)
    nickname = db.Column(db.String(64), index=True, unique=True)
    email = db.Column(db.String(120), index=True, unique=True)
    posts = db.relationship('Post', backref='author', lazy='dynamic')
    about_me = db.Column(db.String(140))
    last_seen = db.Column(db.DateTime)
    
    def __repr__(self):
        return '<User %r>' % (self.nickname)

class Goods(db.Model):
    __searchable__ = ['name']

    goodsid = db.Column(db.Integer, primary_key=True)
    price = db.Column(db.Integer)
    pictureId = db.Column(db.Integer)
    longitude = db.Column(db.Float)
    latitude = db.Column(db.Float)
    goodsName = db.Column(db.String(64), index=True, unique=True)
    goodsDescription = db.Column(db.String(1400))
    
    def __repr__(self):
        return '<Goods %r>' % (self.goodsName)

if enable_search:
    whooshalchemy.whoosh_index(app, Goods)
