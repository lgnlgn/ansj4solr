package org.ansj.solr;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ansj.library.UserDefineLibrary;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;


public class AnsjTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware , UpdateKeeper.UpdateJob{

	private int analysisType = 0;
	private ResourceLoader loader; 
	
	private long lastUpdateTime = -1;
	private String conf = null;
	
	
	protected AnsjTokenizerFactory(Map<String, String> args) {
		super(args);
		assureMatchVersion();
		analysisType = getInt(args, "analysisType", 0);
		conf = get(args, "conf");
		System.out.println(":::ansj:construction::::::::::::::::::::::::::" + conf);
	}

	public void update() throws IOException {
		Properties p = canUpdate();
		if (p != null){
			List<String> dicPaths = SplitFileNames(p.getProperty("files"));
			List<InputStream> inputStreamList = new ArrayList<InputStream>();
			for (String path : dicPaths) {
				if ((path != null && !path.isEmpty())) {
					InputStream is = loader.openResource(path);

					if (is != null) {
						inputStreamList.add(is);
					}
				}
			}
			if (!inputStreamList.isEmpty()) {
				UserDefineLibrary.reloadMainAndAdd(inputStreamList); // load dic to MainDic
			}
		}
	}

	public void inform(ResourceLoader loader) throws IOException {
		System.out.println(":::ansj:::inform::::::::::::::::::::::::" + conf);
		this.loader = loader;
		this.update();
		if(conf != null && !conf.trim().isEmpty()){
			UpdateKeeper.getInstance().register(this);
		}
	}

	@Override
	public Tokenizer create(AttributeFactory factory, Reader input) {
		return new AnsjTokenizer(input, analysisType);
	}
	
	public static List<String> SplitFileNames(String fileNames) {
		if (fileNames == null)
			return Collections.<String> emptyList();
		List<String> result = new ArrayList<String>();
		for (String file : fileNames.split("[,\\s]+")) {
			result.add(file);
		}
		return result;
	}
	
	private Properties canUpdate() {

		try{
			if (conf == null)
				return null;
			Properties p = new Properties();
			InputStream confStream = loader.openResource(conf);
			p.load(confStream);
			confStream.close();
			String lastupdate = p.getProperty("lastupdate", "0");
			Long t = new Long(lastupdate);
			
			if (t > this.lastUpdateTime){
				this.lastUpdateTime = t.longValue();
				String paths = p.getProperty("files");
				if (paths==null || paths.trim().isEmpty()) 
					return null;
				System.out.println("loading conf");
				return p;
			}else{
				this.lastUpdateTime = t.longValue();
				return null;
			}
		}catch(Exception e){
			System.err.println("ansj parsing conf NullPointerException~~~~~" + e.getMessage());
			return null;
		}
	}
	
}
