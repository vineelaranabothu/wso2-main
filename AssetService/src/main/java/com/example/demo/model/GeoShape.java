package com.example.demo.model;






import org.json.JSONArray;

//import com.fasterxml.jackson.annotation.JsonFormat;




public class GeoShape {
	
	public String type;
	//@JsonFormat(shape = JsonFormat.Shape.ARRAY)
	public JSONArray coordinates;
	public GeoShape() {
		
	}
	public GeoShape(String type, JSONArray coordinates) {
		super();
		this.type = type;
		this.coordinates = coordinates;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public JSONArray getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(JSONArray coordinates) {
		this.coordinates = coordinates;
	}
	
	
	
}
