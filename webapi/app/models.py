from hashlib import md5
from app import db
from app import app


class User(db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(64), index=True, unique=True)
    email = db.Column(db.String(120), index=True, unique=True)
    phone_number = db.Column(db.String(140))
    password = db.Column(db.String(140))
    
    def Addpeople(self,username,email,phone_number,password):
        self.username = username
        self.email = email
        self.phone_number = phone_number
        self.password = password

    def __repr__(self):
        return '<User %r>' % (self.nickname)
    
    def getdata(self):
        data = {
            'id': self.id ,
            'username' : self.username,
            'email' : self.email,
            'phone_number' :  self.phone_number,
            'password' : self.password
        }
        return data 

    def __unicode__(self):
        return (self.username)

class Goods(db.Model):
    __tablename__ = 'goods'
    __searchable__ = ['goodsname','goodsdescription']

    goodsid = db.Column(db.Integer, primary_key=True)
    price = db.Column(db.Integer)
    pictureId =  db.Column(db.String(1400))
    longitude = db.Column(db.Float)
    latitude = db.Column(db.Float)
    goodsname = db.Column(db.String(64), index=True, unique=True)
    goodsdescription = db.Column(db.String(1400))
    address = db.Column(db.String(1400))
    data = {}

    def GoodsInformation(self,Price,Pictureid,Longitude,Latitude,Goodsname,Goodsdescription,address):
        self.pictureId = Pictureid
        self.longitude = Longitude
        self.latitude = Latitude
        self.price = Price
        self.goodsname = Goodsname
        self.goodsdescription = Goodsdescription
        self.address = address
    
    def getdata(self):
        data = {
            'goodsid' : self.goodsid, 
            'price' : self.price,
            'pictureName' : self.pictureId,
            'longitude' :self.longitude,
            'latitude' : self.latitude,
            'goodsName' :self.goodsname,
            'goodsDescription' :self.goodsdescription,
            'address' : self.address
        }
        return data

    def __repr__(self):
        return '<Goods %r>' % (self.goodsname)

    def __unicode__(self):
        return (self.goodsname)

