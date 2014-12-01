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
    username = db.Column(db.String(64), index=True, unique=True)
    email = db.Column(db.String(120), index=True, unique=True)
    phone_number = db.Column(db.String(140))
    
    def Addpeople(self,username,email,phone_number):
        self.username = username
        self.email = email
        self.phone_number = phone_number

    def __repr__(self):
        return '<User %r>' % (self.nickname)
    
    def getdata(self):
        data = {
        'id': self.id ,
        'username' : self.username,
        'email' : self.email,
        'phone_number' :  self.phone_number
        }
        return data 

    def __unicode__(self):
        return (self.username)

class Goods(db.Model):
    __searchable__ = ['goodsName','goodsDescription']

    goodsid = db.Column(db.Integer, primary_key=True)
    price = db.Column(db.Integer)
    pictureId = db.Column(db.Integer)
    longitude = db.Column(db.Float)
    latitude = db.Column(db.Float)
    goodsName = db.Column(db.String(64), index=True, unique=True)
    goodsDescription = db.Column(db.String(1400))
    data = {}

    def  GoodsInformation(self,pictureId,  longitude, latitude,  price,  name,  desc) :
        self.pictureId = pictureId
        self.longitude = longitude
        self.latitude = latitude
        self.price = price
        self.goodsName = name
        self.goodsDescription = desc
    
    def getdata(self):
        data = {
            'goodsid' : self.goodsid, 
            'price' : self.price,
            'pictureId' : self.pictureId,
            'longitude' :self.longitude,
            'latitude' : self.latitude,
            'goodsName' :self.goodsName,
            'goodsDescription' :self.goodsDescription
        }
        return data

    def __repr__(self):
        return '<Goods %r>' % (self.goodsName)

    def __unicode__(self):
        return (self.goodsName)

#if enable_search:
#    whooshalchemy.whoosh_index(app, Goods)