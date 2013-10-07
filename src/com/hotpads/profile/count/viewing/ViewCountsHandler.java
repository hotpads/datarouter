package com.hotpads.profile.count.viewing;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.StringMav;
import com.hotpads.profile.count.collection.Counters;
import com.hotpads.profile.count.collection.archive.CountArchive;
import com.hotpads.profile.count.collection.archive.CountArchiveFlusher;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;
@Deprecated
public class ViewCountsHandler extends BaseHandler{

	public static final String
		PARAM_counters = "counters",
		PARAM_archive = "archive",
		PARAM_sourceType = "sourceType",
		PARAM_source = "source",
		PARAM_periodMs = "periodMs",
		PARAM_frequency = "frequency",
		PARAM_name = "name",
		PARAM_nameLike = "nameLike",
		PARAM_rangeLengthSeconds = "rangeLengthSeconds",
		VALUE_source_all = "all",
		
		JSP_listArchives = "/WEB-INF/jsp/listArchives.jsp",
		JSP_listCounters = "/WEB-INF/jsp/listCounters.jsp",
		JSP_viewCounters = "/WEB-INF/jsp/viewCounters.jsp";
//	JSP_listArchives = "/WEB-INF/jsp/counter/listArchives.jsp",
//			JSP_listCounters = "/WEB-INF/jsp/counter/listCounters.jsp",
//			JSP_viewCounters = "/WEB-INF/jsp/counter/viewCounters.jsp";
	
	@Override
	protected Mav handleDefault() {
		return listArchives();
	}
	
	@Handler Mav listArchives(){
		Mav mav = new Mav(JSP_listArchives);
		mav.put("archives", Counters.get().getManager().getArchives());
		return mav;
	}
	
	@Handler Mav listCounters(){
		Mav mav = new Mav(JSP_listCounters);
		
		mav.put("archives", Counters.get().getManager().getArchives());
		
		String archiveName = params.required(PARAM_archive);
//		String sourceType = params.required(PARAM_sourceType);
//		String source = params.required(PARAM_source);
//		Long periodMs = params.required(PARAM_periodMs);
		String nameLike = params.optional(PARAM_nameLike, "");
		CountArchive archive = getArchive(archiveName);
		List<AvailableCounter> counters = archive.getAvailableCounters(nameLike);
		if(!archiveName.startsWith(CountArchiveFlusher.NAME_MEMORY)){
			counters = AvailableCounter.filterOutArrayServers(counters);//makes the html table unboundedly wide
		}
		mav.put("counters", counters);
		SortedMap<String,SortedMap<String,AvailableCounter>> counterBySourceByName = MapTool.createTreeMap();
		Map<String,AvailableCounter> aCounterByName = MapTool.create();
		for(AvailableCounter counter : IterableTool.nullSafe(counters)){
			String htmlName = counter.getNameHtmlEscaped();
			if(!counterBySourceByName.containsKey(htmlName)){
				counterBySourceByName.put(htmlName, new TreeMap<String,AvailableCounter>());
			}
			counterBySourceByName.get(htmlName).put(counter.getSource(), counter);
			aCounterByName.put(htmlName, counter);
		}
		mav.put("counterBySourceByName", counterBySourceByName);
		mav.put("aCounterByName", aCounterByName);
		List<String> sources = ListTool.create();//wrap(VALUE_source_all);
		sources.addAll(AvailableCounter.getAllSources(counters));
		mav.put("sources", sources);
		
		return mav;
	}
	
	@Handler Mav viewCounters(){
		Mav mav = new Mav(JSP_viewCounters);
		
		List<String> names = params.optionalCsvList(PARAM_counters, null);
		List<CountSeries> seriesList = ListTool.createArrayList();
		for(String name : IterableTool.nullSafe(names)){
			CountSeries series = getSeries(name);
			ListTool.nullSafeArrayAdd(seriesList, series);
		}
		mav.put("seriesList", seriesList);
		
		return mav;
	}
	
	@Handler Mav csvCounts(){
		String name = params.required(PARAM_name);
		String frequency = params.optional(PARAM_frequency, "second");
		CountSeries series = getSeries(name);
		StringBuilder sb = new StringBuilder("time,count");
		for(Count count : IterableTool.nullSafe(series.getPaddedCounts())){
			double value = count.getValuePer(frequency);
//			double value = count.getValue();
			String valueString = "";
			if(value!=0){ valueString = value + ""; }
			sb.append("\n"+count.getTimeString()+","+valueString);
		}
		return new StringMav(sb.toString());
	}
	
	protected CountArchive getArchive(String name){
		if(Counters.get()==null || Counters.get().getManager()==null){ return null; }
		return Counters.get().getManager().getArchiveByName().get(name);
	}
	
	protected CountSeries getSeries(String name){
		String archiveName = params.required(PARAM_archive);
		String sourceType = params.required(PARAM_sourceType);
		String source = params.required(PARAM_source);
		Long periodMs = params.requiredLong(PARAM_periodMs);
		long defaultRangeLengthMs = 800*periodMs;
		long rangeLengthSeconds = params.optionalLong(PARAM_rangeLengthSeconds, defaultRangeLengthMs/1000);
		long rangeStartMs = System.currentTimeMillis() - rangeLengthSeconds * 1000;
		rangeStartMs = Count.getIntervalStart(periodMs, rangeStartMs);
		long rangeEndMs = rangeStartMs + rangeLengthSeconds * 1000;
		CountArchive archive = getArchive(archiveName);
		if(archive==null){ return null; }
		List<Count> counts = archive.getCountsForAllSources(name, rangeStartMs, rangeEndMs);
		if(CollectionTool.isEmpty(counts)){ return null; }
		if(ObjectTool.notEquals(VALUE_source_all, source)){
			counts = Count.filterForSource(counts, source);
		}
		Collections.sort(counts);
//		logger.warn("got "+counts.size()+" Counts from "+new Date(CollectionTool.getFirst(counts).getCreated())
//				+" to "+new Date(CollectionTool.getLast(counts).getCreated()));
		CountSeries series = new CountSeries(rangeStartMs, rangeEndMs, periodMs,
				name, sourceType, source, counts);
		return series;
	}
}
