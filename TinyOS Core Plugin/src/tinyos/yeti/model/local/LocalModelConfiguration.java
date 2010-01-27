/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.model.local;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.model.IFileModel;
import tinyos.yeti.model.IModelConfiguration;
import tinyos.yeti.model.IProjectCache;
import tinyos.yeti.model.IProjectDefinitionCollector;
import tinyos.yeti.model.ProjectCacheFactory;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.model.standard.StandardFileModel;
import tinyos.yeti.preferences.PreferenceConstants;

public class LocalModelConfiguration implements IModelConfiguration{
    public IProjectDefinitionCollector createDefinitionCollector( ProjectModel model ){
        return new LocalProjectDefinitionCollector( model );
    }

    public IFileModel createFileModel( ProjectModel model ){
        return new StandardFileModel( model );
    }
    
    /** 
     * Returns a new {@link IProjectCache} using the current selection.
     * @return the new cache or <code>null</code> if the current selection
     * is invalid
     */
    private IProjectCache loadProjectCache(){
    	TinyOSPlugin plugin = TinyOSPlugin.getDefault();
    	if( plugin == null ){
    		return null;
    	}
        String id = getProjectCacheType();
        
        for( ProjectCacheFactory factory : plugin.getProjectCaches() ){
        	if( id.equals( factory.getId() )){
        		return factory.create();
        	}
        }
        
        return null;
    }
    
    
    public IProjectCache createProjectCache( ProjectModel model ){
        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        IProjectCache cache = null;
        
        if( plugin != null ){
        	cache = loadProjectCache();
        }
    	
        if( cache == null ){
        	throw new IllegalStateException( "no project cache available" );
        }
        
	    cache.initialize( model );
	    return cache;
    }
    
    public String getProjectCacheType(){
    	return TinyOSPlugin.getDefault().getPreferenceStore().getString( PreferenceConstants.PROJECT_CACHE );
    }
}
