from flask import render_template, flash, redirect, session, url_for, request, g
from flask import jsonify
from datetime import datetime
from app import app, db
from .models import User, Goods


@app.route('/', methods=['GET', 'POST'])
@app.route('/index', methods=['GET', 'POST'])
def index( ):
   return jsonify({"Pages": "list of pages"})

@app.route('/<product>',methods = ['GET','POST'])
def get_product_by_id(product=0):
	return jsonify({"Pages":product})