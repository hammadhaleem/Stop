#!/usr/bin/env python
# -*- coding: utf-8 -*-

from flask import render_template, flash, redirect, session, url_for, request
from flask import jsonify
from datetime import datetime
from app import app, db
from .models import User, Goods
from werkzeug import secure_filename
import os 
from flask import Flask, render_template, request, redirect, url_for, send_from_directory
#import Image
#import cv2.cv as cv
#import tesseract
#import pytesseract
#import tesseract
#import cv2
#import cv2.cv as cv
import json
import sys
from bs4 import BeautifulSoup
import requests
import json
#import numpy as np

app.config['UPLOAD_FOLDER'] = 'uploads/'
# These are the extension that we are accepting to be uploaded
app.config['ALLOWED_EXTENSIONS'] = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])

@app.route('/', methods=['GET', 'POST'])
@app.route('/index', methods=['GET', 'POST'])
def index( ):
   return jsonify({
   	"Pages": "list of pages",
   	'Product By ID ' : '/product/<product-id>',
   	'User By ID' : '/user/<userid',
   	'Add User' :  '/register/<username>/<email>/<phone_number>/<password>',
   	'Login' : '/login/<username>/<password>',
   	'Add Product' : '/AddProduct/<Price : int >/<PictureName: string>/<Longitude:float>/<Latitude:float>/<Goodsname:string>/<Goodsdescription:text>/<address : text>',
   	'Search':'/search/<product-name>',
   	'Route':'/getpath/long,lat;long,lat;long,lat;long,lat;long,lat/'
   	 
   	 })

@app.route('/product/<product>',methods = ['GET','POST'])
def get_product_by_id(product=0):
	good = Goods.query.filter_by(goodsid= product).all()
	if len(good)  > 0 :
		return jsonify(good[0].getdata())
	else : 
		return jsonify({})

@app.route('/user/<userid>',methods = ['GET','POST'])
def get_user_by_id(userid=0):
	user = User.query.filter_by(id = userid).all()
	if len(user) > 0 :
		return jsonify(user[0].getdata())
	else:
		return jsonify({})


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in app.config['ALLOWED_EXTENSIONS']


@app.route('/form')
def index_upload():
    return str('<link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" rel="stylesheet"> </head> <body> <div class="container"> <div class="header"> <h3 class="text-muted"></h3> </div> <hr/> <div> <form action="upload" method="post" enctype="multipart/form-data"> <input type="file" name="file"><br /><br /> <input type="submit" value="Upload"> </form> </div> </div> </body> </html>')


# Route that will process the file upload
@app.route('/upload', methods=['POST'])
@app.route('/upload', methods=['POST'])
def upload():
    data = request.data
    file = request.files['file']
    if file and allowed_file((file.filename).lower()):
        filename = secure_filename(file.filename).lower()
        t= file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        return jsonify({
        'file' : url_for('uploaded_file',filename=filename),
        'name' : filename,
        'ocr'  : url_for('convert_file',filename=filename)
        })
    else:
    	return(str("Error!!"))

@app.route('/uploads/<filename>/')
@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory('/home/engineer/htdocs/stop/webapi/uploads',filename)
    #return send_from_directory(app.config['UPLOAD_FOLDER'],filename)

@app.route('/convert/<filename>/')
@app.route('/convert/<filename>')
def convert_file(filename):
    #path = str(app.config['UPLOAD_FOLDER']+filename)
    path = str('/home/engineer/htdocs/stop/webapi/uploads/'+filename).lower()
    try:
    	image=cv.LoadImage(path, cv.CV_LOAD_IMAGE_GRAYSCALE)
    except Exception as e :
    	return str("Error ")+str(e)
    api = tesseract.TessBaseAPI()
    api.Init(".","eng",tesseract.OEM_DEFAULT)
    api.SetPageSegMode(tesseract.PSM_AUTO)
    tesseract.SetCvImage(image,api)
    text=api.GetUTF8Text()
    conf=api.MeanTextConf()
    return jsonify({'output' : str(text)})


@app.route('/login/<username>/<password>/')
@app.route('/login/<username>/<password>')
def login(username,password):
  user = User.query.filter_by(username = username).all()
  if len(user) > 0 :
    data = user[0].getdata()
    if data['password'] == password : 
      return jsonify(data)
    else:
      return jsonify({})
  else:
    return jsonify({})

@app.route('/register/<username>/<email>/<phone_number>/<password>/')
@app.route('/register/<username>/<email>/<phone_number>/<password>')

def register(username,email,phone_number,password):
	user = User()
	user.Addpeople(username,email,phone_number,password)
	try :	
		db.session.add(user)
		db.session.commit()
		return jsonify(user.getdata())
	except :
		return jsonify({'Status': 'Error'})


@app.route('/AddProduct/<Price>/<Pictureid>/<Longitude>/<Latitude>/<Goodsname>/<Goodsdescription>/<address>')
@app.route('/AddProduct/<Price>/<Pictureid>/<Longitude>/<Latitude>/<Goodsname>/<Goodsdescription>/<address>/')
def add_good(Price,Pictureid,Longitude,Latitude,Goodsname,Goodsdescription,address):
	good = Goods()
	good.GoodsInformation(Price,Pictureid,Longitude,Latitude,Goodsname,Goodsdescription,address)
	try:
		db.session.add(good)
		db.session.commit()
		return jsonify(good.getdata())
	except Exception as e:
		print e
		return jsonify({'status' : 'Error'})



@app.route('/search/<keyword>', methods=['GET'])
@app.route('/search/<keyword>/', methods=['GET'])
def search(keyword=None):
    data = []
    if data is not None:
        result = Goods.query.whoosh_search(keyword).all()
        for obj in result:
            data.append(obj.getdata())
    return jsonify({'objects' : data})

@app.route('/getpath/<cord>', methods=['GET'])
@app.route('/getpath/<cord>/', methods=['GET'])
def route(cord = None):
  cord = str(cord)
  cord = cord.split(";")
  start = cord[0]
  end = cord[len(cord)-1]

  start = str(start)
  end   = str(end)
  string = ""
  key = "AIzaSyCkWUIO4p6JAfGC4NkQJDRtX87BPVx4kBM"
  url = "https://maps.googleapis.com/maps/api/directions/json?origin="
  url = url + start+'&destination='+end+'&waypoints=optimize:true|'+string+'&key='
  url = url +key
  r = requests.get(url)
  data = dict(json.loads(str(r.content)))
  data['url'] = url
  string  = ""
  da = {}
  keys = []
  for objects in data['routes']:
    for key,value in objects.items():
      if key =="legs":
        da[key] = value
        steps = {}
        for dic in value:
          dic = dict(dic)
          for key,value in dic.items():
            steps[key] = value
  count = 1
  data = {}
  final = {}
  list_keys = ['distance','duration','start_location','end_location','html_instructions']
  for objects in steps['steps']:
    data[count] = objects
    final[count] = {}
    for key,value in data[count].items():
      if key in list_keys:
        final[count][key] = value  

    count = count + 1 
  return str(final)