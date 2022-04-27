package com.example.demo.controller;


import java.sql.Timestamp;
import java.util.List;
import org.elasticsearch.common.geo.GeoPoint;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.model.Asset;
import com.example.demo.model.AssetService;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.CassandraAssetRepository;

@Controller
@RequestMapping("/api")

public class CassandraAssetController {
	
	@Autowired
	private AssetRepository repository;
	@Autowired
	private CassandraAssetRepository csrepository;
	@Autowired
	private ModelMapper modelMapper;
	
	@GetMapping("/ShowFormForAdd")
	public String ShowFormForAdd(Model model)
	{
		AssetService assetService = new AssetService();
		model.addAttribute("saveAsset",assetService);
		return "Asset-form";
	}
	
	
	//Entering values into asset table
	@PostMapping("/asset")
    private String creat(@ModelAttribute("saveAsset") AssetService assetService ){
		Asset asset = modelMapper.map(assetService, Asset.class);
		Timestamp time = new Timestamp(System.currentTimeMillis());
		asset.setCreatedat(time);
		asset.setObjectType("asset");
		GeoPoint gp = new GeoPoint();
		gp.resetFromString(assetService.getPosition1());
		asset.setPosition(gp);
		assetService.setPosition(assetService.getPosition1());
		assetService.setCreatedat(time);
		repository.save(asset);
		csrepository.save(assetService);
		return "Asset-form" ;
		 
	}
	
	
	
	@GetMapping("/assetList")
	private String allData(Model model){
		List<Asset> data = (List<Asset>) repository.findByObjectType("asset");
		model.addAttribute("asset", data);
		return "Asset-list";
	}
	
	@GetMapping("/ShowFormForUpdate")
	public String ShowFormForUpdate(Model model)
	{
		AssetService assetService = new AssetService();
		model.addAttribute("saveAsset",assetService);
		return "Asset-form";
	}
    
	@PostMapping("/assetUpdate")
    private String update(@ModelAttribute("saveAsset") AssetService assetService ){
		Asset asset = modelMapper.map(assetService, Asset.class);
		Timestamp time = new Timestamp(System.currentTimeMillis());
		asset.setCreatedat(time);
		asset.setObjectType("asset");
		GeoPoint gp = new GeoPoint();
		gp.resetFromString(assetService.getPosition1());
		asset.setPosition(gp);
		assetService.setPosition(assetService.getPosition1());
		repository.save(asset);
		csrepository.save(assetService);
		return "Asset-form" ;
		 
	}
	
	
	//Delete by id
	@GetMapping("delete")
	private String deleteById( @RequestParam("id") String id) {
		repository.deleteById(id);
		csrepository.deleteById(id);
		return "redirect:/api/assetList";
	}
	

	
	
}

