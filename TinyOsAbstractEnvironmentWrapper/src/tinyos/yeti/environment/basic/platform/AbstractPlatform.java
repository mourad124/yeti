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
package tinyos.yeti.environment.basic.platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.commands.make.MakeExtras;
import tinyos.yeti.environment.basic.commands.make.MakeSeparator;
import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.environment.basic.path.PlatformFile;
import tinyos.yeti.ep.IMakeExtraDescription;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.IPlatformListener;
import tinyos.yeti.ep.ISensorBoard;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.make.MakeInclude;

/**
 * An implementation of {@link IPlatform} that lazily loads its {@link IMakeExtra}s
 * and {@link ISensorBoard}s but stores them after they were loaded. 
 * @author Benjamin Sigg
 */
public abstract class AbstractPlatform implements IExtendedPlatform{
    private String description;
    private ImageDescriptor image;
    private String name;
    
    private File directory;
    
    private IMakeExtraDescription[] extras;
    private ISensorBoard[] sensorBoards;
    
    private AbstractEnvironment environment;
    
    private List<IPlatformListener> listeners = new ArrayList<IPlatformListener>();
    private MakeInclude[] includes;
    
    private IPlatformFile platformFile;
    private IMacro[] macros;
    
    private File basePlatformFile;
    private File baseFamilyFile;
    
    private String nestedCVariableSeparator = null;

	/**
     * Creates a new platform. This constructor searches for a <code>.platform</code>
     * and a <code>.family</code> file. If a <code>.family</code> file is found,
     * then the directory in which it is is set to be the root directory of
     * this platform.
     * @param environment the environment to translate paths
     * @param directory the directory in which to start the search, can be <code>null</code>
     * @param top the top directory in the search, no directory higher in the
     * tree than <code>top</code> will be searched, can be <code>null</code>
     * @param converter the {@link MMCUConverter} that will be used to read mmcu parameters from
     * the platform file. See {@link AbstractPlatformManager#createDefaultMMCUConverter()}, can be 
     * <code>null</code>.
     */
    public AbstractPlatform( AbstractEnvironment environment, File directory, File top, MMCUConverter converter ){
        this.environment = environment;
        if( directory != null ){
            setBasePlatformFile( new File( directory, ".platform" ) );
            
            // search for family file
            File root = directory;
            while( root != null && !root.equals( top ) ){
                File family = new File( root, ".family" );
                if( family.exists() ){
                    setBaseFamilyFile( family );
                    setDirectory( root );
                    break;
                }
                root = root.getParentFile();
            }
            
            if( getDirectory() == null ){
                setDirectory( directory );
            }
            
            setName( directory.getName() );
        }
        
        IPlatformFile platformFile = getPlatformFile();
        if( platformFile != null && converter != null ){
        	if( converter.interested( platformFile )){
        		addMacros(converter.convert( platformFile ));
        	}
        }
    }
    
    public void addPlatformListener( IPlatformListener listener ){
        listeners.add( listener );
    }
    
    public void removePlatformListener( IPlatformListener listener ){
        listeners.remove( listener );
    }
    
    public MakeInclude[] getDefaultIncludes(){
        MakeInclude[] defaults = environment.getPlatformManager().getDefaultMakeIncludes();
        return MakeInclude.combine( includes, defaults );
    }
    
    public MakeInclude[] getIncludes(){
        return includes;
    }
    
    @Deprecated
    public void setMacros( IMacro[] macros ){
        this.macros = macros;
    }
    
    public void addMacros( IMacro[] macros ){
    	if( macros == null || macros.length == 0 )
    		return;
    	
    	if( this.macros == null || this.macros.length == 0 )
    		this.macros = macros;
    	else{
    		IMacro[] temp = new IMacro[ this.macros.length + macros.length ];
    		System.arraycopy( this.macros, 0, temp, 0, this.macros.length );
    		System.arraycopy( macros, 0, temp, this.macros.length, macros.length );
    		this.macros = temp;
    	}
    }
    
    public IMacro[] getMacros(){
        return macros;
    }
    
    public void setIncludes( MakeInclude[] includes ){
        // check whether something changes
        boolean changed = !Arrays.equals( includes, this.includes );
        
        this.includes = includes;
        
        if( changed ){
            fireMakeIncludesChanged();
        }
    }
    
    public File[] getGlobalIncludes(){
        if( directory == null )
            return null;
        
        return new File[]{ new File( directory, "hardware.h" ) };
    }
    
    public void fireMakeIncludesChanged(){
        for( IPlatformListener listener : listeners.toArray( new IPlatformListener[ listeners.size() ] )){
            listener.makeIncludesChanged( this );
        }
    }
    
    public void firePathsChanged(){
    	for( IPlatformListener listener : listeners.toArray( new IPlatformListener[ listeners.size() ])){
    		listener.pathsChanged( this );
    	}
    }
    
    public void setDirectory( File directory ) {
        platformFile = null;
        this.directory = directory;
    }
    
    public File getDirectory() {
        return directory;
    }
    
    /**
     * Sets the <code>.platform</code> file.
     * @param basePlatformFile the platform file, can be <code>null</code>
     */
    public void setBasePlatformFile( File basePlatformFile ){
        this.basePlatformFile = basePlatformFile;
        platformFile = null;
    }
    
    public File getBasePlatformFile(){
        return basePlatformFile;
    }

    /**
     * Sets the <code>.family</code> file.
     * @param baseFamilyFile the family file, can be <code>null</code>
     */
    public void setBaseFamilyFile( File baseFamilyFile ){
        this.baseFamilyFile = baseFamilyFile;
        platformFile = null;
    }
    
    public File getBaseFamilyFile(){
        return baseFamilyFile;
    }
    
    public IPlatformFile getPlatformFile(){
        if( platformFile == null ){
            PlatformFile result = new PlatformFile();
            
            File directory = getDirectory();
            if( directory != null ){
                File family = getBaseFamilyFile();
                if( family != null && family.exists() ){
                    try{
                        result.readFrom( family );
                    }
                    catch( IOException ex ){
                        TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log(
                                new Status( Status.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, ex.getMessage(), ex ) );
                    }
                }
                
                File platform = getBasePlatformFile();
                if( platform != null && platform.exists() ){
                    try{
                        result.readFrom( platform );
                    }
                    catch( IOException ex ){
                        TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log(
                                new Status( Status.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, ex.getMessage(), ex ) );
                    }
                }
            }
            platformFile = result;
        }
        
        return platformFile;
    }
    
    public void setDescription( String description ){
        this.description = description;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setImage( ImageDescriptor image ){
        this.image = image;
    }
    
    public ImageDescriptor getImage(){
        return image;
    }
    
    public void setName( String name ){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public IMakeExtraDescription[] getExtras(){
        if( extras == null ){
            extras = loadExtras();
            if( extras == null )
                extras = new IMakeExtraDescription[]{};
        }
        
        return extras;
    }

    protected IMakeExtraDescription[] loadExtras(){
        try{
            return environment.getCommandExecuter().execute( new MakeExtras( getName(), environment ) );
        }
        catch ( InterruptedException e ){
            TinyOSAbstractEnvironmentPlugin.error( e );
        }
        catch ( IOException e ){
            TinyOSAbstractEnvironmentPlugin.error( e );
        }
        
        return null;
    }
    
    public String getNestedCVariableSeparator() {
    	if(nestedCVariableSeparator == null) {
    		nestedCVariableSeparator = loadNestedCVariableSeparator();
    	}
    	if(nestedCVariableSeparator == null) {
    		nestedCVariableSeparator = DEFAULT_NESC_SEPARATOR;
    	}
		return nestedCVariableSeparator;
	}
    
    protected String loadNestedCVariableSeparator() {
    	try{
    		return environment.getCommandExecuter().execute( new MakeSeparator( getName(), environment ) );
        }
        catch ( InterruptedException e ){
            TinyOSAbstractEnvironmentPlugin.error( e );
        }
        catch ( IOException e ){
            TinyOSAbstractEnvironmentPlugin.error( e );
        }
        
        return null;
    }
    public ISensorBoard[] getSensorboards(){
        if( sensorBoards == null ){
            sensorBoards = loadSensorBoards();
            if( sensorBoards == null )
                sensorBoards = new ISensorBoard[]{};
        }
        return sensorBoards;
    }
    
    protected abstract ISensorBoard[] loadSensorBoards();
}
