package com.spring.cassandra.AssetSaml2.model;
import java.time.LocalDate;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("Asset")
public class Asset {
	
	@PrimaryKey
	private String id;
	private String tenent_id;
	private String name;
	private String status;
	private String external_id;
	private Boolean parent;
	private Boolean del;
	private String image_url;
	private String tenent_name;
	private LocalDate createdAt;
    public Asset() {
		
	}
	
	public Asset(String id, String tenent_id, String name, String status, String external_id, Boolean parent,
			Boolean del, String image_url, String tenent_name, LocalDate createdAt) {
		super();
		this.id = id;
		this.tenent_id = tenent_id;
		this.name = name;
		this.status = status;
		this.external_id = external_id;
		this.parent = parent;
		this.del = del;
		this.image_url = image_url;
		this.tenent_name = tenent_name;
		this.createdAt = createdAt;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTenent_id() {
		return tenent_id;
	}
	public void setTenent_id(String tenent_id) {
		this.tenent_id = tenent_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getExternal_id() {
		return external_id;
	}
	public void setExternal_id(String external_id) {
		this.external_id = external_id;
	}
	public Boolean getParent() {
		return parent;
	}
	public void setParent(Boolean parent) {
		this.parent = parent;
	}
	public Boolean getDel() {
		return del;
	}
	public void setDel(Boolean del) {
		this.del = del;
	}
	public String getImage_url() {
		return image_url;
	}
	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}
	public String getTenent_name() {
		return tenent_name;
	}
	public void setTenent_name(String tenent_name) {
		this.tenent_name = tenent_name;
	}
	public LocalDate getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDate date) {
		this.createdAt = date;
	}
	
	

}

