from flask import render_template, flash, redirect, session, url_for, request
from flask import jsonify
from datetime import datetime
from app import app, db
from .models import User, Goods
from werkzeug import secure_filename
import os 
from flask import Flask, render_template, request, redirect, url_for, send_from_directory
import Image
import cv2.cv as cv
import tesseract
import pytesseract
import tesseract
import cv2
import cv2.cv as cv
import json
import numpy as np

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
   	'Add User' :  '/register/<username>/<password>/<email>/<phone>',
   	'Login' : '/login/<username>/<password>',
   	'Add Product' : '/AddProduct/<Price : int >/<Pictureid: string>/<Longitude:float>/<Latitude:float>/<Goodsname>/<Goodsdescription>'
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
		return jsonify(user[0].getdata())
	else:
		return jsonify({})

@app.route('/register/<username>/<password>/<email>/<phone>/')
@app.route('/register/<username>/<password>/<email>/<phone>')
def register(username,password,email,phone):
	user = User()
	user.Addpeople(username,password,email,phone)
	try :	
		db.session.add(user)
		db.session.commit()
		return jsonify(user.getdata())
	except :
		return jsonify({'Status': 'Error'})


@app.route('/AddProduct/<Price>/<Pictureid>/<Longitude>/<Latitude>/<Goodsname>/<Goodsdescription>/')
@app.route('/AddProduct/<Price>/<Pictureid>/<Longitude>/<Latitude>/<Goodsname>/<Goodsdescription>')
def add_good(Price,Pictureid,Longitude,Latitude,Goodsname,Goodsdescription):
	good = Goods()
	good.GoodsInformation(Price,Pictureid,Longitude,Latitude,Goodsname,Goodsdescription)
	try:
		db.session.add(good)
		db.session.commit()
		return jsonify(good.getdata())
	except Exception as e:
		print e
		return jsonify({'status' : 'Error'})