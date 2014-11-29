from flask import render_template, flash, redirect, session, url_for, request
from flask import jsonify
from datetime import datetime
from app import app, db
from .models import User, Goods


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
	return jsonify(good.getdata())

@app.route('/user/<userid>',methods = ['GET','POST'])
def get_user_by_id(userid=0):
	user = User.query.filter_by(id = userid).all()
	return jsonify(user.getdata())

