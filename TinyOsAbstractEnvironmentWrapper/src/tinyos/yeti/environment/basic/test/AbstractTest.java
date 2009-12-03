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
package tinyos.yeti.environment.basic.test;

import tinyos.yeti.ep.ITest;

/**
 * An abstract implementation of {@link ITest}.
 * @author Benjamin Sigg
 */
public abstract class AbstractTest implements ITest{
    private String name;
    private String description;

    public AbstractTest(){
        // nothing
    }
    
    public AbstractTest( String name, String description ){
        this.name = name;
        this.description = description;
    }
    
    public void setDescription( String description ){
        this.description = description;
    }
    
    public String getDescription(){
        return description;
    }

    public void setName( String name ){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
}
