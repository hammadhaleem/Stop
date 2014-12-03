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

app.config['UPLOAD_FOLDER'] = 'uploads/'
# These are the extension that we are accepting to be uploaded
app.config['ALLOWED_EXTENSIONS'] = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])

@app.route('/', methods=['GET', 'POST'])
@app.route('/index', methods=['GET', 'POST'])
def index( ):
   return jsonify({
   	"Pages": "list of pages",
   	'Product By ID ' : '/product/<product-id>',
   	'User By ID' : '/user/<userid>'})

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
def upload():
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

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory('/home/engineer/htdocs/stop/webapi/uploads',filename)
    #return send_from_directory(app.config['UPLOAD_FOLDER'],filename)


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
    #api.SetPageSegMode(tesseract.PSM_SINGLE_WORD)
    api.SetPageSegMode(tesseract.PSM_AUTO)
    tesseract.SetCvImage(image,api)
    text=api.GetUTF8Text()
    conf=api.MeanTextConf()
    return jsonify({'output' : str(text)})
#return send_from_directory(app.config['UPLOAD_FOLDER'],filename)


import tesseract
import cv2
import cv2.cv as cv
import numpy as np




@app.route('/converts/<filename>')
def conver_file_advance(filename):
	path = str('/home/engineer/htdocs/stop/webapi/uploads/'+filename).lower()
	
	scale = 1
	delta = 0
	ddepth = cv2.CV_16S

	gray=cv2.imread(path)
	cut_offset=23
	gray=gray[cut_offset:-cut_offset,cut_offset:-cut_offset]
	gray = cv2.cvtColor(gray,cv2.COLOR_BGR2GRAY)
	grad_x = cv2.Sobel(gray,ddepth,1,0,ksize = 3, scale = scale, delta = delta,borderType = cv2.BORDER_DEFAULT)
	grad_y = cv2.Sobel(gray,ddepth,0,1,ksize = 3, scale = scale, delta = delta, borderType = cv2.BORDER_DEFAULT)
	abs_grad_x = cv2.convertScaleAbs(grad_x)  
	abs_grad_y = cv2.convertScaleAbs(grad_y)
	gray = cv2.addWeighted(abs_grad_x,0.5,abs_grad_y,0.5,0)
	image1 = cv2.medianBlur(gray,5) 
	image1[image1 < 50]= 255
	image1 = cv2.GaussianBlur(image1,(31,13),0)     
	color_offset=230
	image1[image1 >= color_offset]= 255  
	image1[image1 < color_offset ] = 0      #black

	offset=30
	height,width = image1.shape
	image1=cv2.copyMakeBorder(image1,offset,offset,offset,offset,cv2.BORDER_CONSTANT,value=(255,255,255)) 
	cv2.namedWindow("Test")
	cv2.imshow("Test", image1)
	cv2.imwrite("an91cut_decoded.jpg",image1)
	cv2.waitKey(0)
	cv2.destroyWindow("Test")
	### tesseract OCR
	api = tesseract.TessBaseAPI()
	api.Init(".","eng",tesseract.OEM_DEFAULT)
	api.SetPageSegMode(tesseract.PSM_SINGLE_BLOCK)
	height1,width1 = image1.shape
	channel1=1
	image = cv.CreateImageHeader((width1,height1), cv.IPL_DEPTH_8U, channel1)
	cv.SetData(image, image1.tostring(),image1.dtype.itemsize * channel1 * (width1))
	tesseract.SetCvImage(image,api)
	text=api.GetUTF8Text()
	conf=api.MeanTextConf()
	image=None
	print str(text)
	return str(text)