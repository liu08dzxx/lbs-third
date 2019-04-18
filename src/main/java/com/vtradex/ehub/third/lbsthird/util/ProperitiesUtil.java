package com.vtradex.ehub.third.lbsthird.util;

import com.vtradex.ehub.third.lbsthird.LbsThirdApplication;

/**
 * 
 * @author liuliwen
 *
 */
public class ProperitiesUtil {
	
    public static int getInt(String name) {
    	return LbsThirdApplication.ctx.getEnvironment().getProperty(name, Integer.class);
    }
    
    public static String getString(String name) {
    	return LbsThirdApplication.ctx.getEnvironment().getProperty(name);
    }
}
