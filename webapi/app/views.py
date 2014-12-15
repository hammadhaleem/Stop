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
from PIL import Image
import cv2.cv as cv
import tesseract ,  pytesseract
import tesseract
import cv2
import cv2.cv as cv
import json
import sys
from bs4 import BeautifulSoup
import requests
import json
import numpy as np
import zbar

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
   	'Add Product' : '/AddProduct/<Price : int >/<PictureName: string>/<Longitude:float>/<Latitude:float>/<Goodsname:string>/<Goodsdescription:text>/<address : text>/<userid>',
   	'Search':'/search/<product-name>',
   	'Route':'/getpath/long,lat;long,lat;long,lat;long,lat;long,lat/',
    'Analyse' : '/convert/<filename>',
    'delete' : '/delete/<product-id>',
    'show-products' : '/get/<userid>'
   	 
   	 })

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
        'ocr'  : url_for('convert_file',filename=filename),
        'barcode' : url_for('bar_Code',filename=filename)
        })
    else:
    	return(str("Error!!"))

@app.route('/uploads/<filename>/')
@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory('/home/engineer/htdocs/stop/webapi/uploads',filename)

@app.route('/barcode/<filename>')
@app.route('/barcode/<filename>')
def bar_Code(filename):
  path = str('/home/engineer/htdocs/stop/webapi/uploads/'+filename).lower()
  image = cv2.imread(path)
  gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
  gradX = cv2.Sobel(gray, ddepth = cv2.cv.CV_32F, dx = 1, dy = 0, ksize = -1)
  gradY = cv2.Sobel(gray, ddepth = cv2.cv.CV_32F, dx = 0, dy = 1, ksize = -1)

  # subtract the y-gradient from the x-gradient
  gradient = cv2.subtract(gradX, gradY)
  gradient = cv2.convertScaleAbs(gradient)

  # blur and threshold the image
  blurred = cv2.blur(gradient, (9, 9))
  (_, thresh) = cv2.threshold(blurred, 225, 255, cv2.THRESH_BINARY)

  # construct a closing kernel and apply it to the thresholded image
  kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (21, 7))
  closed = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)

  # perform a series of erosions and dilations
  closed = cv2.erode(closed, None, iterations = 4)
  closed = cv2.dilate(closed, None, iterations = 4)

  # find the contours in the thresholded image, then sort the contours
  # by their area, keeping only the largest one
  (cnts, _) = cv2.findContours(closed.copy(), cv2.RETR_EXTERNAL,
    cv2.CHAIN_APPROX_SIMPLE)
  c = sorted(cnts, key = cv2.contourArea, reverse = True)[0]

  # compute the rotated bounding box of the largest contour
  rect = cv2.minAreaRect(c)
  box = np.int0(cv2.cv.BoxPoints(rect))

  # draw a bounding box arounded the detected barcode and display the
  # image
  cv2.drawContours(image, [box], -1, (0, 255, 0), 3)
  path = path + '_bar.png'
  cv2.imwrite(path,image)
  pil = Image.open(path)
  pil = pil.convert('L')
  width, height = pil.size
  raw = pil.tostring()

  # wrap image data
  image = zbar.Image(width, height, 'Y800', raw)
  scanner = zbar.ImageScanner()

  # configure the reader
  scanner.parse_config('enable')
  # scan the image for barcodes
  scanner.scan(image)
  string = "result : "
  # extract results
  for symbol in image:
      # do something useful with results
      string = string + 'decoded' +  str(symbol.type) + ' symbol ' +  str(symbol.data) + "\n"
  return str(string)


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

@app.route('/product/<product>',methods = ['GET','POST'])
def get_product_by_id(product=0):
  good = Goods.query.filter_by(goodsid= product, delete = 0 ).all()
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


@app.route('/delete/<product>')
@app.route('/delete/<product>/')
def delete(product= None):
  if product is None :
    return jsonify({'status' : 'error'})
  else:
    db.session.flush()
    product= product.split(';')
    lis = []
    print product
    for i in product:
      try:
        obj = Goods.query.filter_by(goodsid = i).first()
        obj.delete_it()
        sql = str('update goods set `delete` = 1 where `goodsid` ='+str(i)+'')
        result = db.engine.execute(sql)
        lis.append(obj.goodsname)
        db.session.commit()
      except Exception as e:
        print e
        pass
    db.session.flush()
    return jsonify({'status' : str(list(set(lis)))})

@app.route('/undo/<product>')
@app.route('/undo/<product>/')
def undo(product= None):
  if product is None :
    return jsonify({'status' : 'error'})
  else:
    db.session.flush()
    product= product.split(';')
    lis = []
    print product
    for i in product:
      try:
        obj = Goods.query.filter_by(goodsid = i).first()
        obj.delete_it()
        sql = str('update goods set `delete` = 0 where `goodsid` ='+str(i)+'')
        result = db.engine.execute(sql)
        lis.append(obj.goodsname)
        db.session.commit()
      except Exception as e :
        print e
        pass
    db.session.flush()
    return jsonify({'status' : str(list(set(lis)))})



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

@app.route('/get/<userid>')
@app.route('/get/<userid>/')
def get_added_good(userid):
  lis = []
  if userid is None: 
    return jsonify({'status' : 'error'})
  goods = Goods.query.filter_by(userid = userid).all()
  for i in goods : 
    lis.append(i.getdata())
  return jsonify({"objects" : str(lis)})

@app.route('/AddProduct/<Price>/<Pictureid>/<Longitude>/<Latitude>/<Goodsname>/<Goodsdescription>/<address>/<userid>')
@app.route('/AddProduct/<Price>/<Pictureid>/<Longitude>/<Latitude>/<Goodsname>/<Goodsdescription>/<address>/<userid>')
def add_good(Price,Pictureid,Longitude,Latitude,Goodsname,Goodsdescription,address,userid):
	good = Goods()
	good.GoodsInformation(Price,Pictureid,Longitude,Latitude,Goodsname,Goodsdescription,address,userid)
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
  cord.pop(0)
  cord.pop(len(cord)-1)
  start = str(start)
  end   = str(end)
  string = ""

  for i in cord:
    string = string+","+i

  key = "AIzaSyCkWUIO4p6JAfGC4NkQJDRtX87BPVx4kBM"
  url = "https://maps.googleapis.com/maps/api/directions/json?origin="
  url = url + start+'&destination='+end+'&waypoints=optimize:true|'+string+'&key='
  url = url +key
  r = requests.get(url)
  data = dict(json.loads(str(r.content)))
  data['url'] = url
  if data['status'] == 'ZERO_RESULTS' or data['status'] == 'NOT_FOUND':
    return jsonify(data)
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
  return jsonify(final)