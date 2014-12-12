package hk.ust.stop.model;

import java.io.Serializable;

public class GoodsInformation implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private long 	goodsId;
	private String 	pictureName;
	private double	longitude;
	private double	latitude;
	private double	price;
	private String	goodsName;
	private String  goodsDescription;
	private boolean selected;
	
	public GoodsInformation(long goodsId, String pictureName, double longitude,
			double latitude, double price, String name, String desc) {
		this.goodsId = goodsId;
		this.pictureName = pictureName;
		this.longitude = longitude;
		this.latitude = latitude;
		this.price = price;
		this.goodsName = name;
		this.goodsDescription = desc;
	}
	
	// this constructor is for test
	public GoodsInformation(String name, double price){
		this.goodsName = name;
		this.price = price;
	}
	
	public GoodsInformation() {
		
	}
	
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
	public String getPictureName() {
		return pictureName;
	}
	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getGoodsDescription() {
		return goodsDescription;
	}
	public void setGoodsDescription(String goodsDescription) {
		this.goodsDescription = goodsDescription;
	}
	public boolean getSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}
