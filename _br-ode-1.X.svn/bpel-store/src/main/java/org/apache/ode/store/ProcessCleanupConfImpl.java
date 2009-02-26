package org.apache.ode.store;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dd.TCleanup;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;

public class ProcessCleanupConfImpl {
	protected static Log __log = LogFactory.getLog(ProcessCleanupConfImpl.class);
	
	private final Set<CLEANUP_CATEGORY> successCategories = EnumSet.noneOf(CLEANUP_CATEGORY.class);
	private final Set<CLEANUP_CATEGORY> failureCategories = EnumSet.noneOf(CLEANUP_CATEGORY.class);
	
	// package default
    ProcessCleanupConfImpl(TDeployment.Process pinfo) {
		for( TCleanup cleanup : pinfo.getCleanupList() ) {
			if( cleanup.getOn() == TCleanup.On.SUCCESS || cleanup.getOn() == TCleanup.On.ALWAYS ) {
				processACleanup(successCategories, cleanup.getCategoryList());
			}
			if( cleanup.getOn() == TCleanup.On.FAILURE || cleanup.getOn() == TCleanup.On.ALWAYS ) {
				processACleanup(failureCategories, cleanup.getCategoryList());
			}
		}
		
		// validate configurations
		Set<CLEANUP_CATEGORY> categories = getCleanupCategories(true);
		if( categories.contains(CLEANUP_CATEGORY.INSTANCE) && !categories.containsAll(EnumSet.of(CLEANUP_CATEGORY.CORRELATIONS, CLEANUP_CATEGORY.VARIABLES))) {
			throw new ContextException("Cleanup configuration error: the instance category requires both the correlations and variables categories specified together!!!");
		}
		categories = getCleanupCategories(false);
		if( categories.contains(CLEANUP_CATEGORY.INSTANCE) && !categories.containsAll(EnumSet.of(CLEANUP_CATEGORY.CORRELATIONS, CLEANUP_CATEGORY.VARIABLES))) {
			throw new ContextException("Cleanup configuration error: the instance category requires both the correlations and variables categories specified together!!!");
		}
    }

    private void processACleanup(Set<CLEANUP_CATEGORY> categories, List<TCleanup.Category.Enum> categoryList) {
		if( categoryList.isEmpty() ) {
			// add all categories
			categories.addAll(EnumSet.allOf(CLEANUP_CATEGORY.class));
		} else {
			for( TCleanup.Category.Enum aCategory : categoryList ) {
				if( aCategory == TCleanup.Category.ALL) {
					// add all categories
					categories.addAll(EnumSet.allOf(CLEANUP_CATEGORY.class));
				} else {
					categories.add(CLEANUP_CATEGORY.fromString(aCategory.toString()));
				}
			}
		}
    }
    
    // package default
    boolean isCleanupCategoryEnabled(boolean instanceSucceeded, CLEANUP_CATEGORY category) {
    	if( instanceSucceeded ) {
    		return successCategories.contains(category);
    	} else {
    		return failureCategories.contains(category);
    	}
    }
    
    // package default
    Set<CLEANUP_CATEGORY> getCleanupCategories(boolean instanceSucceeded) {
    	return instanceSucceeded ? successCategories : failureCategories;
    }
}
