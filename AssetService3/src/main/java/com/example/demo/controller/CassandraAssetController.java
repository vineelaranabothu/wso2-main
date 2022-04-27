package com.example.demo.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.EnvelopeBuilder;
import org.elasticsearch.common.geo.builders.LineStringBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilders;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.GeoShapeQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Asset;
import com.example.demo.model.AssetService;
import com.example.demo.model.Location;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.CassandraAssetRepository;
import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("deprecation")
@Controller
@RequestMapping("/api")

public class CassandraAssetController {

	@Autowired
	private AssetRepository repository;
	@Autowired
	private CassandraAssetRepository csrepository;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private RestHighLevelClient client;

	@GetMapping("/ShowFormForAdd")
	public String ShowFormForAdd(Model model) {
		AssetService assetService = new AssetService();
		model.addAttribute("saveAsset", assetService);
		return "Asset-form";
	}

	// Entering values into asset table
	@PostMapping("/asset")
	private String creat(@ModelAttribute("saveAsset") AssetService assetService) {
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
		return "Asset-form";

	}

	@GetMapping("/ShowFormForSearch")
	public String ShowFormSearch(Model model) {
		Location location = new Location();
		model.addAttribute("search", location);
		return "Search-form";
	}

	@PostMapping("/geoSearch")
	public String geoSearch(@ModelAttribute("search") Location locationEs) throws IOException {
		HashMap<String, String> map = new HashMap<>();
		map.put("id", locationEs.getId());
		System.out.println(locationEs.getId());

		ResponseEntity<Location> rsentity = new RestTemplate().getForEntity("http://localhost:8082/api/findById/{id}",
				Location.class, map);
		Location location1 = rsentity.getBody();
		String type = location1.getGeo_shape1().getType();
		String id = location1.getId();
		JsonNode gs = location1.getGeo_shape1().getCoordinates();
		BoolQueryBuilder query1 = QueryBuilders.boolQuery();
		System.out.println(type);
		String geo_json[] = location1.getGeo_json().split(",");
		String radius = geo_json[1].substring(10, geo_json[1].length() - 2);
		if (type.equalsIgnoreCase("point")) {

			double lat = gs.get(0).asDouble();
			double lon = gs.get(1).asDouble();
			GeoDistanceQueryBuilder qb = QueryBuilders.geoDistanceQuery("position").point(lat, lon).distance(radius);
			query1.filter(qb);
			System.out.println(query1.filter(qb));
		}

		if (type.equalsIgnoreCase("lineString")) {
			List<org.locationtech.jts.geom.Coordinate> coordinate = new ArrayList<org.locationtech.jts.geom.Coordinate>();
			coordinate.add(
					new org.locationtech.jts.geom.Coordinate(gs.get(0).get(0).asDouble(), gs.get(0).get(1).asDouble()));
			coordinate.add(
					new org.locationtech.jts.geom.Coordinate(gs.get(1).get(0).asDouble(), gs.get(1).get(1).asDouble()));
			ShapeBuilder<JtsGeometry, LineStringBuilder> shape = ShapeBuilders.newLineString(coordinate);
			GeoShapeQueryBuilder qb = QueryBuilders.geoShapeQuery("geo_shape1", shape);
			// .relation(ShapeRelation.INTERSECTS);
			query1.filter(qb);

		}

		if (type.equalsIgnoreCase("polygon")) {
			int size = gs.get(0).size() - 1;
			List<GeoPoint> allPoints = new ArrayList<GeoPoint>();
			for (int i = 0; i <= size; i++) {

				allPoints.add(new GeoPoint(gs.get(0).get(i).get(0).asDouble(), gs.get(0).get(i).get(1).asDouble()));
			}
			GeoPolygonQueryBuilder qb = QueryBuilders.geoPolygonQuery("position", allPoints);
			query1.filter(qb);

		}
        if(type.equalsIgnoreCase("envelope")) {
        	GeoPoint topLeft = new GeoPoint(gs.get(0).get(0).asDouble(),gs.get(0).get(1).asDouble());
        	GeoPoint bottomRight = new GeoPoint(gs.get(1).get(0).asDouble(),gs.get(1).get(1).asDouble());

        	GeoBoundingBoxQueryBuilder qb = QueryBuilders.geoBoundingBoxQuery("position").setCorners(bottomRight,topLeft);
        	   
        	query1.filter(qb);/*
        	Coordinate topLeft = new Coordinate(17.370231, 78.503377);
        	Coordinate bottomRight = new Coordinate(17.422323,78.449184);
        	ShapeBuilder shape = ShapeBuilders.newEnvelope(topLeft, bottomRight);
        	GeoShapeQueryBuilder qb = QueryBuilders.geoShapeQuery("geo_shape1", id, "_doc").indexedShapeIndex("innominds").indexedShapePath("geo_shape1"); 
        	query1.filter(qb);*/
       }
        	
		query1.must(QueryBuilders.matchAllQuery());
		SearchRequest searchRequest = new SearchRequest("innominds");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(query1);
		searchRequest.source(searchSourceBuilder);
		System.out.println(searchRequest);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		SearchHit[] results = searchResponse.getHits().getHits();

		for (SearchHit hit : results) {
			System.out.println(hit);
		}

		return "Search-form";
	}

	@PostMapping("geo/aggregate/assets")
	private String aggregates(@ModelAttribute("search") Location locationEs, Model model) throws IOException {

		HashMap<String, String> map = new HashMap<>();
		map.put("id", locationEs.getId());
		System.out.println(locationEs.getId());

		ResponseEntity<Location> rsentity = new RestTemplate().getForEntity("http://localhost:8082/api/findById/{id}",
				Location.class, map);
		Location location1 = rsentity.getBody();
		// Getting latitude and longitude from geo_shape
		String city = location1.getName();
		// String id = location1.getId();
		String type = location1.getGeo_shape1().getType();
		JsonNode gs = location1.getGeo_shape1().getCoordinates();
		BoolQueryBuilder query1 = QueryBuilders.boolQuery();
		System.out.println(type);
		String geo_json[] = location1.getGeo_json().split(",");
		String radius = geo_json[1].substring(10, geo_json[1].length() - 2);
		// AggregationBuilder aggregation = AggregationBuilders.geoDistance("aggs",
		// gp).field("position").addRange(0, r);

		if (type.equalsIgnoreCase("point")) {

			double lat = gs.get(0).asDouble();
			double lon = gs.get(1).asDouble();
			GeoDistanceQueryBuilder qb = QueryBuilders.geoDistanceQuery("position").point(lat, lon).distance(radius);
			query1.filter(qb);
			System.out.println(query1.filter(qb));
		}

		if (type.equalsIgnoreCase("LineString")) {
			List<org.locationtech.jts.geom.Coordinate> coordinate = new ArrayList<org.locationtech.jts.geom.Coordinate>();
			coordinate.add(
					new org.locationtech.jts.geom.Coordinate(gs.get(0).get(0).asDouble(), gs.get(0).get(1).asDouble()));
			coordinate.add(
					new org.locationtech.jts.geom.Coordinate(gs.get(1).get(0).asDouble(), gs.get(1).get(1).asDouble()));
			ShapeBuilder<JtsGeometry, LineStringBuilder> shape = ShapeBuilders.newLineString(coordinate);

			GeoShapeQueryBuilder qb = QueryBuilders.geoShapeQuery("geo_shape1", shape);
			// .relation(ShapeRelation.INTERSECTS);
			query1.filter(qb);

		}

		if (type.equalsIgnoreCase("polygon")) {
			int size = gs.get(0).size() - 1;
			List<GeoPoint> allPoints = new ArrayList<GeoPoint>();
			for (int i = 0; i <= size; i++) {

				allPoints.add(new GeoPoint(gs.get(0).get(i).get(0).asDouble(), gs.get(0).get(i).get(1).asDouble()));
			}
			GeoPolygonQueryBuilder qb = QueryBuilders.geoPolygonQuery("position", allPoints);
			query1.filter(qb);

		}
		if(type.equalsIgnoreCase("envelope")) {
        	GeoPoint topLeft = new GeoPoint(gs.get(0).get(0).asDouble(),gs.get(0).get(1).asDouble());
        	GeoPoint bottomRight = new GeoPoint(gs.get(1).get(0).asDouble(),gs.get(1).get(1).asDouble());

        	GeoBoundingBoxQueryBuilder qb = QueryBuilders.geoBoundingBoxQuery("position").setCorners(bottomRight,topLeft);
        	   
        	query1.filter(qb);
        	
       }
		AggregationBuilder aggregation = AggregationBuilders.filters("aggs", query1);
		SearchRequest searchRequest = new SearchRequest("innominds");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.aggregation(aggregation);
		// searchRequest.types("asset");
		searchRequest.source(searchSourceBuilder);
		System.out.println(searchRequest);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		Aggregation agg = searchResponse.getAggregations().get("aggs");
		// For each entry
		for (Bucket entry : ((MultiBucketsAggregation) agg).getBuckets()) {
			String key = entry.getKeyAsString(); // bucket key
			long docCount = entry.getDocCount(); // Doc count
			System.out.println("key:" + key);
			System.out.println(city + ":" + docCount);
			model.addAttribute("entry", entry);
			model.addAttribute("city", city);
		}

		return "Agg-list";
		// return "Search-form";
	}

	@GetMapping("/assetList")
	private String allData(Model model) {
		List<Asset> data = (List<Asset>) repository.findByObjectType("asset");
		model.addAttribute("asset", data);
		return "Asset-list";
	}

	@GetMapping("/ShowFormForUpdate")
	public String ShowFormForUpdate(Model model) {
		AssetService assetService = new AssetService();
		model.addAttribute("saveAsset", assetService);
		return "Asset-form";
	}

	@PostMapping("/assetUpdate")
	private String update(@ModelAttribute("saveAsset") AssetService assetService) {
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
		return "Asset-form";

	}

	// Delete by id
	@GetMapping("delete")
	private String deleteById(@RequestParam("id") String id) {
		repository.deleteById(id);
		csrepository.deleteById(id);
		return "redirect:/api/assetList";
	}

}
