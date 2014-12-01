from flask import render_template, flash, redirect, session, url_for, request
from flask import jsonify
from datetime import datetime
from app import app, db
from .models import User, Goods
from werkzeug import secure_filename
import os 
from flask import Flask, render_template, request, redirect, url_for, send_from_directory


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
    return str('<!DOCTYPE html> <html lang="en"> <head> <link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" rel="stylesheet"> </head> <body> <div class="container"> <div class="header"> <h3 class="text-muted">How To Upload a File</h3> </div> <hr/> <div> <form action="upload" method="post" enctype="multipart/form-data"> <input type="file" name="file"><br /><br /> <input type="submit" value="Upload"> </form> </div> </div> </body> </html>')


# Route that will process the file upload
@app.route('/upload', methods=['POST'])
def upload():
    # Get the name of the uploaded file
    file = request.files['file']
    # Check if the file is one of the allowed types/extension
    if file and allowed_file((file.filename).lower()):
        # Make the filename safe, remove unsupported chars
        filename = secure_filename(file.filename)
        # Move the file form the temporal folder to
        # the upload folder we setup

        t= file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        print t 
        # Redirect the user to the uploaded_file route, which
        # will basicaly show on the browser the uploaded file
        return redirect(url_for('uploaded_file',
                                filename=filename))
    else:
    	return(str("Error!!"))

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory('/home/engineer/htdocs/stop/webapi/uploads',filename)
    #return send_from_directory(app.config['UPLOAD_FOLDER'],filename)