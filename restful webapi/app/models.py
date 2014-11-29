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
    
    def Addpeople(username,email,phone_number):
        this.username = username
        this.email = email
        this.phone_number = phone_number

    def __repr__(self):
        return '<User %r>' % (self.nickname)
    
    def getdata():
        data = {
        'id': id ,
        'username' : username,
        'email' : email,
        'phone_number' :  phone_number
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

    def  GoodsInformation(  pictureId,  longitude, latitude,  price,  name,  desc) :
        this.pictureId = pictureId
        this.longitude = longitude
        this.latitude = latitude
        this.price = price
        this.goodsName = name
        this.goodsDescription = desc
    
    def getdata():
        data = {
        'goodsid' : goodsid , 
        'price' :price,
        'pictureId' : pictureId,
        'longitude' :longitude,
        'latitude' : latitude,
        'goodsName' : goodsName,
        'goodsDescription' :goodsDescription
        }
        return data

    def __repr__(self):
        return '<Goods %r>' % (self.goodsName)

    def __unicode__(self):
        return (self.goodsName)

if enable_search:
    whooshalchemy.whoosh_index(app, Goods)
