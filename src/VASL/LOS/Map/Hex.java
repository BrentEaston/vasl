/*
 * Copyright (c) 2000-2003 by David Sullivan
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASL.LOS.Map;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;

import static java.lang.StrictMath.cos;

/**
 * Title:        Hex.java
 * Copyright:    Copyright (c) 2001 David Sullivan Zuericher Strasse 6 12205 Berlin Germany. All rights reserved.
 * @author       David Sullivan
 * @version      1.0+
 */
public class Hex {

	// Property variables
	private	String	name;
	private	int		columnNumber;
	private	int		rowNumber;
	private	int		baseHeight;
	private	boolean northOnMap		= true;
	private	boolean northEastOnMap  = true;
	private	boolean southEastOnMap  = true;
	private	boolean southOnMap		= true;
	private	boolean southWestOnMap  = true;
	private	boolean northWestOnMap  = true;

	// the parent map
	private Map map;

	// geometric variables
	private	Point2D.Double   center;
	private	Polygon hexBorder 		= new Polygon(); // the hex border
	private	Polygon extendedHexBorder	= new Polygon(); // one pixel larger than the hex border

	// location variables (center and each hexside)
    // the point for each hexside location will be the midpoint of the hexside offset one pixel toward the center of the hex
	private	Location[] hexsideLocations = new Location[6];
	private	Location centerLocation;

	// terrain variables
	private Terrain[] hexsideTerrain  = new Terrain[6]; // hexside terrain is wall, hedge, etc.
	private	boolean[] hexsideHasCliff  = new boolean[6];

	// other stuff
    //TODO: bridge object no longer used
	private	Bridge	bridge;
    private	boolean stairway;

    /**
     * Create a new hex
     * @param col the hex column - the first is 0
     * @param row the hex row - first is 0
     * @param name hex name
     * @param centerDot location of hex center dot on map
     * @param hexHeight hex height
     * @param hexWidth hex width
     * @param map the map
     * @param baseHeight base height/elevation
     * @param terrain default terrain used for all locations
     */
    public Hex(int col, int row, String name, Point2D.Double centerDot, double hexHeight, double hexWidth, Map map, int baseHeight, Terrain terrain) {

		this.baseHeight = baseHeight;
		columnNumber = col;
		rowNumber	= row;
		this.map	= map;
        this.name = name;
        center = fixMapEdgePoints(centerDot.getX(), centerDot.getY());
		initHexNew(centerDot, hexHeight, hexWidth, terrain);

	}

	/**
	 * Initialized the hex using custom geometry
	 */
	private void initHexNew(Point2D.Double centerDot, double hexHeight, double hexWidth, Terrain terr){

        final double x = centerDot.getX();
		final double y = centerDot.getY();

        // create the hex geometry. Hexes are assumed to be oriented with the top and bottom hexsides
        // parallel to the map top/bottom

        // the length of the a hexside equals the distance from the center point to the vertexes
        // final double hexside = hexHeight/(2.0*cos(Math.toRadians(30.0)));
        final double hexside = hexWidth*2.0/3.0;
        final double verticalOffset = hexHeight/2.0;

        // [0] is the left-most vertex on the top hexside and the other points are clockwise from there
        Point2D.Double[] vertexPoints = new Point2D.Double[6];
        vertexPoints[0] = fixMapEdgePoints(-hexside / 2.0 + x, -verticalOffset + y);
        vertexPoints[1] = fixMapEdgePoints(hexside / 2.0 + x, -verticalOffset + y);
        vertexPoints[2] = fixMapEdgePoints(hexside + x, y);
        vertexPoints[3] = fixMapEdgePoints(hexside / 2.0 + x, verticalOffset + y);
        vertexPoints[4] = fixMapEdgePoints(-hexside / 2.0 + x, verticalOffset + y);
        vertexPoints[5] = fixMapEdgePoints(-hexside + x, y);
        for (int i = 0; i < 6; i++) {

            // create the hex borders - extended being one pixel larger
            hexBorder.addPoint((int) Math.round(vertexPoints[i].x), (int) Math.round(vertexPoints[i].y));
            switch (i) {
                case 0:
                    extendedHexBorder.addPoint((int) vertexPoints[i].x - 1, (int) vertexPoints[i].y - 1);
                    break;
                case 1:
                    extendedHexBorder.addPoint((int) vertexPoints[i].x - 1, (int) vertexPoints[i].y + 1);
                    break;
                case 2:
                    extendedHexBorder.addPoint((int) vertexPoints[i].x + 1, (int) vertexPoints[i].y);
                    break;
                case 3:
                    extendedHexBorder.addPoint((int) vertexPoints[i].x + 1, (int) vertexPoints[i].y + 1);
                    break;
                case 4:
                    extendedHexBorder.addPoint((int) vertexPoints[i].x - 1, (int) vertexPoints[i].y + 1);
                    break;
                case 5:
                    extendedHexBorder.addPoint((int) vertexPoints[i].x - 1, (int) vertexPoints[i].y);
                    break;
            }

        }

        // the hexside point is the hexside center point translated one pixel toward the hex center point
        // [0] is the top hexside and the other points are clock-wise from there
        Point2D.Double[] hexsidePoints = new Point2D.Double[6];
        final double horizontalOffset = cos(Math.toRadians(30.0)) * verticalOffset;

        hexsidePoints[0] = fixMapEdgePoints((int) x,                           (int) (-verticalOffset + y + 1.0));
        hexsidePoints[1] = fixMapEdgePoints((int) (horizontalOffset + x - 1),  (int) (-verticalOffset/2.0 + y + 1.0));
        hexsidePoints[2] = fixMapEdgePoints((int) (horizontalOffset + x - 1),  (int) (verticalOffset/2.0 + y - 1.0));
        hexsidePoints[3] = fixMapEdgePoints((int) x,                           (int) (verticalOffset + y - 1.0));
        hexsidePoints[4] = fixMapEdgePoints((int) (-horizontalOffset + x + 1),  (int) (verticalOffset/2.0 + y - 1.0));
        hexsidePoints[5] = fixMapEdgePoints((int) (-horizontalOffset + x + 1),  (int) (-verticalOffset/2.0 + y + 1.0));

        setHexFlags();

        createLocations(terr, vertexPoints, hexsidePoints);
	}

    /**
     * Create the the set of "empty" hex locations - i.e they will not reflect the grid terrain
     * @param terr a default terrain used for all locations
     * @param vertexPoints the hex vertex points
     * @param hexsidePoints the hex hexside points
     */
    private void createLocations(Terrain terr, Point2D.Double[] vertexPoints, Point2D.Double[] hexsidePoints) {

        // create center and hexside locations
        centerLocation = new Location(
                name,
                baseHeight,
                getHexCenter(),
                getHexCenter(),
                getHexCenter(),
                this,
                terr
        );

        hexsideLocations[0] = new Location(
                name + ":North",
                baseHeight,
                new Point((int)vertexPoints[0].getX(),
                        (int)vertexPoints[0].getY()),
                new Point((int)vertexPoints[1].getX(),
                        (int)vertexPoints[1].getY()),
                new Point((int)hexsidePoints[0].getX(),
                        (int)hexsidePoints[0].getY()),
                this,
                terr
        );

        hexsideLocations[1] = new Location(
                name +  ":NorthEast",
                baseHeight,
                new Point((int)vertexPoints[1].getX(),
                        (int)vertexPoints[1].getY()),
                new Point((int)vertexPoints[2].getX(),
                        (int)vertexPoints[2].getY()),
                new Point((int)hexsidePoints[1].getX(),
                        (int)hexsidePoints[1].getY()),
                this,
                terr
        );

        hexsideLocations[2] = new Location(
                name +  ":SouthEast",
                baseHeight,
                new Point((int)vertexPoints[2].getX(),
                        (int)vertexPoints[2].getY()),
                new Point((int)vertexPoints[3].getX(),
                        (int)vertexPoints[3].getY()),
                new Point((int)hexsidePoints[2].getX(),
                        (int)hexsidePoints[2].getY()),
                this,
                terr
        );

        hexsideLocations[3] = new Location(
                name +  ":South",
                baseHeight,
                new Point((int)vertexPoints[3].getX(),
                        (int)vertexPoints[3].getY()),
                new Point((int)vertexPoints[4].getX(),
                        (int)vertexPoints[4].getY()),
                new Point((int)hexsidePoints[3].getX(),
                        (int)hexsidePoints[3].getY()),
                this,
                terr
        );

        hexsideLocations[4] = new Location(
                name +  ":SouthWest",
                baseHeight,
                new Point((int)vertexPoints[4].getX(),
                        (int)vertexPoints[4].getY()),
                new Point((int)vertexPoints[5].getX(),
                        (int)vertexPoints[5].getY()),
                new Point((int)hexsidePoints[4].getX(),
                        (int)hexsidePoints[4].getY()),
                this,
                terr
        );

        hexsideLocations[5] = new Location(
                name +  ":NorthWest",
                baseHeight,
                new Point((int)vertexPoints[5].getX(),
                        (int)vertexPoints[5].getY()),
                new Point((int)vertexPoints[0].getX(),
                        (int)vertexPoints[0].getY()),
                new Point((int)hexsidePoints[5].getX(),
                        (int)hexsidePoints[5].getY()),
                this,
                terr
        );
    }

    /**
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return a point that has been adjusted so it is on the map if x or y are off the map by a pixel or two
     */
    private Point2D.Double fixMapEdgePoints(double x, double y) {

        double newX = x == -1.0 ? 0.0 : x;
        double newY = y == -1.0 ? 0.0 : y;
        newX = (int) newX == map.getGridWidth()  || (int) newX == (map.getGridWidth() + 1.0)  ? (map.getGridWidth()  - 1.0) : newX;
        newY = (int) newY == map.getGridHeight() || (int) newY == (map.getGridHeight() + 1.0) ? (map.getGridHeight() - 1.0) : newY;

        if((int) newY == 645 || (int) newY == 646) {
            System.out.println("Point adjusted - x: " + x + " y: " + y);
        }
        return new Point2D.Double(newX, newY);
    }

    /**
     * set the "on map?" flags
     */
    private void setHexFlags() {

        // first column?
        if (columnNumber == 0) {

            southWestOnMap  = false;
            northWestOnMap  = false;
        }

        // last column?
        if (columnNumber + 1 == map.getWidth()) {

            southEastOnMap  = false;
            northEastOnMap  = false;
        }

        // first hex in odd column?
        if ((columnNumber%2 == 1) &&  (rowNumber == 0)){

            northOnMap  = false;
            northEastOnMap  = false;
            northWestOnMap  = false;
        }

        // last hex in odd column?
        if ((columnNumber%2 == 1) &&  (rowNumber == map.getHeight())){

            southOnMap  = false;
            southEastOnMap  = false;
            southWestOnMap  = false;
        }

    }

	// used to update the hexside location once the map has been fully initialized
	public void resetHexsideLocationNames(){

		if (map.getAdjacentHex(this, 0) != null) hexsideLocations[0].setName(name + "/" + map.getAdjacentHex(this, 0).getName());
		if (map.getAdjacentHex(this, 1) != null) hexsideLocations[1].setName(name + "/" + map.getAdjacentHex(this, 1).getName());
		if (map.getAdjacentHex(this, 2) != null) hexsideLocations[2].setName(name + "/" + map.getAdjacentHex(this, 2).getName());
		if (map.getAdjacentHex(this, 3) != null) hexsideLocations[3].setName(name + "/" + map.getAdjacentHex(this, 3).getName());
		if (map.getAdjacentHex(this, 4) != null) hexsideLocations[4].setName(name + "/" + map.getAdjacentHex(this, 4).getName());
		if (map.getAdjacentHex(this, 5) != null) hexsideLocations[5].setName(name + "/" + map.getAdjacentHex(this, 5).getName());
	}

	// get the map
	public Map getMap() { return map;}

	// bridge methods
	public Bridge  getBridge(){ return bridge;}
	public void    setBridge(Bridge bridge){

		this.bridge = bridge;

		// create the new bridge location
		final Location l = new Location(
			name + ":Bridge",
			bridge.getRoadLevel() - baseHeight,
			new Point((int) center.getX(), (int)center.getY()),
			new Point((int) center.getX(), (int)center.getY()),
			new Point((int) center.getX(), (int)center.getY()),
			this,
                bridge.getTerrain()
		);
		bridge.setLocation(l);

		// set the location up/down pointers
		l.setDownLocation(centerLocation);
		centerLocation.setUpLocation(l);
	}

	public boolean hasBridge(){
		// return (bridge != null) || this.hasBridgeTerrain();
        return (bridge != null);
	}

	// Property methods
	public boolean isSouthEastOnMap()   {return southEastOnMap;}
	public boolean isSouthOnMap()	   {return southOnMap;}
	public boolean isSouthWestOnMap()   {return southWestOnMap;}
	public boolean 	isNorthEastOnMap() {return northEastOnMap;}
	public boolean	isNorthOnMap(){return northOnMap;}
	public boolean 	isNorthWestOnMap()   {return northWestOnMap;}
	public boolean 	isHexsideOnMap(int hexside)   {

		switch (hexside){

		    case 0 : return isNorthOnMap();
		    case 1 : return isNorthEastOnMap();
		    case 2 : return isSouthEastOnMap();
		    case 3 : return isSouthOnMap();
		    case 4 : return isSouthWestOnMap();
		    case 5 : return isNorthWestOnMap();

		    default: return false;
		}
	}

	public int		getColumnNumber() {return columnNumber;}
	public void 	setColumnNumber(int newColumnNumber) {columnNumber = newColumnNumber;
	}

	public String   getName(){return name;}

	public int	    getRowNumber()			{return rowNumber;}
	public Polygon  getHexBorder()			{return hexBorder;}
	public Polygon  getExtendedHexBorder()	{return extendedHexBorder;}
	public Point	getHexCenter()			{return new Point((int) center.getX(), (int) center.getY());}
	public int	    getBaseHeight()			{return baseHeight;}

	public Location getCenterLocation() { return centerLocation;}

	public void setHexBorder(Polygon newHexBorder) {
		hexBorder = newHexBorder;
	}

	public void setExtendedHexBorder(Polygon newHexBorder) {
		extendedHexBorder = newHexBorder;
	}

	public void setRowNumber(int newRowNumber) {
		rowNumber = newRowNumber;
	}

	public static int getOppositeHexside(int hexside){

		switch (hexside){
			case 0: return 3;
			case 1: return 4;
			case 2: return 5;
			case 3: return 0;
			case 4: return 1;
			case 5: return 2;
			default: return -1;
		}
	}

	/**
	 * @param l a location
	 * @return true if l is the center location or any location above/below the center location
	 */
	public boolean isCenterLocation(Location l) {

        if(centerLocation.equals(l)) {
            return true;
        }

		// center, up locations
		Location temp1 = centerLocation;
		while(temp1.getUpLocation() != null){
			if (l.equals(temp1.getUpLocation())){
				return true;
			}
			else {
				temp1 = temp1.getUpLocation();
			}
		}

		// down locations
		Location temp2 = centerLocation;
		while(temp2.getDownLocation() != null){
			if (l.equals(temp2.getDownLocation())){
				return true;
			}
			else {
				temp2 = temp2.getDownLocation();
			}
		}
		return false;
	}

	public int getLocationHexside(Location l){

		for(int x = 0; x < 6; x++) {
			if (l.equals(hexsideLocations[x])){
				return x;
			}
		}
		return -1;
	}

    /**
     * Set the depression terrain
     * @param terr the depression terrain - pass null to remove depression terrain
     */
    public void setDepressionTerrain(Terrain terr) {

		// change the depression terrain in the center location
		centerLocation.setDepressionTerrain(terr);

		// if were removing the depression terrain, ensure all hexside
		// depression terrain is also removed
		if (terr == null) {
			for(int x = 0; x < 6; x++){
				hexsideLocations[x].setDepressionTerrain(terr);
			}
		}
	}

	public void setHexsideDepressionTerrain(int side) {

		// change the depression terrain in the hexside location
		hexsideLocations[side].setDepressionTerrain(centerLocation.getDepressionTerrain());
	}

    /**
     * @return true if terrain is depression terrain
     */
    public boolean isDepressionTerrain() {

		return centerLocation.isDepressionTerrain();
	}

    /**
     * Resets the hex locations using the terrain information in the map terrain grid
     */
    public void resetTerrain() {

        // set the center location terrain
        final Terrain centerLocationTerrain = map.getGridTerrain((int) centerLocation.getLOSPoint().getX(), (int) centerLocation.getLOSPoint().getY());

        if(centerLocationTerrain == null) {
            System.out.println();
        }
        centerLocation.setTerrain(centerLocationTerrain);

        // add building locations
        if (centerLocationTerrain.getLOSCategory() == Terrain.LOSCategories.BUILDING ||
            centerLocationTerrain.getLOSCategory() == Terrain.LOSCategories.MARKETPLACE){

            // special case for marketplace
            if(centerLocationTerrain.getLOSCategory() == Terrain.LOSCategories.MARKETPLACE) {
                centerLocation.setTerrain(map.getTerrain("Open Ground"));
            }
            else {
                centerLocation.setTerrain(centerLocationTerrain);
            }

            // add upper level building locations
            Location previousLocation = centerLocation;
            for (int level = 1; level <= centerLocationTerrain.getHeight(); level++) {

                // need to ignore buildings without upper level locations - bit of a hack so we can use the building height
                if(!"Wooden Building".equals(centerLocationTerrain.getName()) &&
                   !"Stone Building".equals(centerLocationTerrain.getName())) {
                    final Location l = new Location(
                            centerLocation.getName() + " Level " + level,
                            level,
                            centerLocation.getLOSPoint(),
                            centerLocation.getLOSPoint(),
                            null,
                            this,
                            centerLocationTerrain
                    );

                    previousLocation.setUpLocation(l);
                    l.setDownLocation(previousLocation);
                    previousLocation = l;
                }
            }

            // set inherent stairway
            stairway =
				"Stone Building, 1 Level".equals(centerLocation.getTerrain().getName()) ||
				"Wooden Building, 1 Level".equals(centerLocation.getTerrain().getName());
        }

        // set the hexside location terrain
        for (int x = 0; x < 6; x++) {

            if (isHexsideOnMap(x)) {

                Terrain terrain =  map.getGridTerrain(
                        (int) getHexsideLocation(x).getEdgeCenterPoint().getX(),
                        (int) getHexsideLocation(x).getEdgeCenterPoint().getY());

                // if no hexside terrain use opposite location hexside terrain
				final Hex oppositeHex = map.getAdjacentHex(this, x);
                if(oppositeHex != null){
                    final int oppositeHexside = (x + 3) % 6;
					final Terrain oppositeHexsideTerrain = map.getGridTerrain(
						(int)oppositeHex.getHexsideLocation(oppositeHexside).getEdgeCenterPoint().getX(),
						(int)oppositeHex.getHexsideLocation(oppositeHexside).getEdgeCenterPoint().getY());
					if(!terrain.isHexsideTerrain() && oppositeHexsideTerrain.isHexsideTerrain()) {
                        terrain = oppositeHexsideTerrain;
                    }
                }

                if(terrain.isHexsideTerrain()) {
                    hexsideTerrain[x] = terrain;
                    if("Cliff".equals(terrain.getName())) {
                        hexsideHasCliff[x] = true;
                    }
                }
                 getHexsideLocation(x).setTerrain(terrain);
            }
        }

        // set the hex base height
        setBaseHeight(map.getGridElevation((int) centerLocation.getLOSPoint().getX(), (int) centerLocation.getLOSPoint().getY()));

        // set inherent terrain in the hex grid
        setInherentTerrain();

        // set the depression terrain
        setDepressionTerrain();

        // reset the hexside terrain
         resetHexsideTerrain();

        // correct for single hex bridges
        fixBridges();
    }

    /**
     * @return true if this hex contains bridge terrain
     */
    private boolean hasBridgeTerrain(){

        final Rectangle rectangle = getHexBorder().getBounds();
        for(int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
            for(int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {

                if(getHexBorder().contains(x,y) &&
                        map.onMap(x,y) &&
                        map.getGridTerrain(x,y).isBridge()) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Corrects hexes with single-hex bridges by making the center location the road location
     */
    private void fixBridges() {

        if(hasBridgeTerrain()) {

            // make the center location the road location by removing the depression terrain
            final Terrain depressionTerrain = centerLocation.getDepressionTerrain();

            final int height = centerLocation.getBaseHeight();
            centerLocation.setDepressionTerrain(null);
            centerLocation.setBaseHeight(height);

            final Location newLocation = new Location(centerLocation);
            newLocation.setDepressionTerrain(depressionTerrain);
            newLocation.setBaseHeight(baseHeight - 1);

            newLocation.setUpLocation(centerLocation);
            centerLocation.setDownLocation(newLocation);
        }
    }

    /**
     * Set the depression terrain
     */
    private void setDepressionTerrain(){

        final Rectangle rectangle = getHexBorder().getBounds();
        for(int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
            for(int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {

                if(rectangle.getBounds().contains(x,y) &&
                        map.onMap(x,y) &&
                        map.getGridTerrain(x,y).isDepression()) {

                    getNearestLocation(x, y).setDepressionTerrain(map.getGridTerrain(x, y));
                }
            }
        }
    }

    /**
     * If the hex contains inherent terrain set center location to that terrain type
     */
    private void setInherentTerrain(){

        final Rectangle rectangle = getHexBorder().getBounds();
        Terrain terrain = null;
        for(int x = rectangle.x; x < rectangle.x + rectangle.width && terrain == null; x++) {
            for(int y = rectangle.y; y < rectangle.y + rectangle.height  && terrain == null; y++) {

                if(rectangle.getBounds().contains(x,y) &&
                        map.onMap(x,y) &&
                        map.getGridTerrain(x,y).isInherentTerrain() &&
					getNearestLocation(x, y).equals(centerLocation)) {
                    terrain = map.getGridTerrain(x,y);
                }
            }
        }

        if(terrain != null) {
            centerLocation.setTerrain(terrain);
        }
    }

    public void resetHexsideTerrain() {

		for (int x = 0; x < 6; x++) {

            // this hexside on the map?
            if (isHexsideOnMap(x)) {

				final Location l = getHexsideLocation(x);
				final Terrain t = map.getGridTerrain((int)l.getEdgeCenterPoint().getX(), (int)l.getEdgeCenterPoint().getY());

				if (t.isHexsideTerrain()) {

                    setHexsideTerrain(x, t);
                }
                else if (t.isDepression()) {

                    l.setDepressionTerrain(t);
                }

                // if adjacent to hexside terrain, make it the same
                final Hex h2 = map.getAdjacentHex(this, x);
                if (h2 != null && h2.getHexsideTerrain(getOppositeHexside(x)) != null) {

                    setHexsideTerrain(x, h2.getHexsideTerrain(getOppositeHexside(x)));
                }

            }
        }
    }

	public Terrain getHexsideTerrain(int side) {

		return hexsideTerrain[side];
	}

	public boolean hasCliff(int side) {

		return hexsideHasCliff[side];
	}

	public void setHexsideTerrain(int side, Terrain terr) {

		// removing?
		if (terr == null){
			hexsideTerrain[side] = null;
			hexsideHasCliff[side] = false;
		}
        else if(terr.isHexsideTerrain()) {
            hexsideTerrain[side] = terr;
            if("Cliff".equals(terr.getName())) {
                hexsideHasCliff[side] = true;
            }
        }
	}

	public Location getHexsideLocation(int hexside){

		return hexsideLocations[hexside];
	}

	public void setHexsideLocationTerrain(int hexside, Terrain terr){

		hexsideLocations[hexside].setTerrain(terr);

	}

	public void setBaseHeight(int hgt) {

		baseHeight = hgt;
	}

	// geometric methods
	public boolean  contains(int x, int y)			{
        return hexBorder.getBounds().contains(x,y) && hexBorder.contains(x, y);}
	public boolean  containsExtended(int x, int y)	{return extendedHexBorder.contains(x, y);}
	public boolean  contains(Point p)				{return hexBorder.contains(p);}

	// nearest Hexside aiming point
	public Location getNearestLocation(int x, int y) {

		// get distance to center
		double  distance	 = Point2D.distance(
			(double) x,
			(double) y,
			centerLocation.getLOSPoint().getX(),
			centerLocation.getLOSPoint().getY());
		Location currentLocation = centerLocation;

		// compare distance to center to distances to vertices
		for(int side = 0; side < 6; side++) {

			// screen out locations off the map
			if (side == 0 && northOnMap 	||
				side == 1 && northEastOnMap 	||
				side == 2 && southEastOnMap 	||
				side == 3 && southOnMap	 	||
				side == 4 && southWestOnMap 	||
				side == 5 && northWestOnMap){

				final double nextDistance = Point2D.distance(
					(double) x,
					(double) y,
					hexsideLocations[side].getEdgeCenterPoint().getX(),
					hexsideLocations[side].getEdgeCenterPoint().getY()
				);

				// side is closer?
				if (nextDistance < distance) {

					distance = nextDistance;
		  			currentLocation = hexsideLocations[side];
				}
			}
		}

		return currentLocation;
	}

	// is the hex touched by the given rectangle
	public boolean isTouchedBy(Rectangle rect) {

        return hexBorder.intersects(rect);
	}

	// change all terrain within hex
	public void changeAllTerrain(Terrain fromTerrain, Terrain toTerrain, Shape s){

		final boolean containsCenter =  s.contains(centerLocation.getLOSPoint());

		// change the center location
		if (centerLocation.getTerrain().getType() == fromTerrain.getType() && containsCenter){

			centerLocation.setTerrain(toTerrain);
		}
		if (centerLocation.getDepressionTerrain() != null && centerLocation.getDepressionTerrain().getType() == fromTerrain.getType()  && containsCenter){

			setDepressionTerrain(toTerrain);
		}

		// change the hexside locations
		for (int x = 0; x < 6; x++){

			if (hexsideLocations[x].getTerrain().getType() == fromTerrain.getType() && s.contains(hexsideLocations[x].getEdgeCenterPoint())){

				hexsideLocations[x].setTerrain(toTerrain);
			}
			if (hexsideLocations[x].getDepressionTerrain() != null && hexsideLocations[x].getDepressionTerrain().getType() == fromTerrain.getType() && s.contains(hexsideLocations[x].getEdgeCenterPoint())){

				hexsideLocations[x].setDepressionTerrain(toTerrain);
			}
		}

		// change the edge terrain locations
		for (int x = 0; x < 6; x++){

			if (hexsideTerrain[x] != null && hexsideTerrain[x].getType() == fromTerrain.getType() && s.contains(hexsideLocations[x].getEdgeCenterPoint())){

				hexsideTerrain[x] = toTerrain;
			}
		}
	}

	public void flip(){

		// transform the hex polygons

		// flip the points in the center location
		flipHexPoint(centerLocation.getEdgeCenterPoint());
		flipHexPoint(centerLocation.getLOSPoint());
		flipHexPoint(centerLocation.getAuxLOSPoint());

		// flip the points in the hexside locations
		for (int x = 0; x < 6; x++){

			flipHexPoint(hexsideLocations[x].getEdgeCenterPoint());
			flipHexPoint(hexsideLocations[x].getLOSPoint());
			flipHexPoint(hexsideLocations[x].getAuxLOSPoint());
		}

		// shuffle the indexes of the hexside variables
		boolean  bTemp;
		for (int x = 0; x < 3; x++){

			final Location lTemp = hexsideLocations[x];
			bTemp = hexsideHasCliff[x];
			final Terrain tTemp = hexsideTerrain[x];
			hexsideLocations[x]     = hexsideLocations[x + 3];
			hexsideHasCliff[x]         = hexsideHasCliff[x + 3];
			hexsideTerrain[x]          = hexsideTerrain[x + 3];
			hexsideLocations[x + 3] = lTemp;
			hexsideHasCliff[x + 3]     = bTemp;
			hexsideTerrain[x + 3]      = tTemp;
		}

		// shuffle the "on map" flags
		bTemp       = northOnMap;
		northOnMap  = southOnMap;
		southOnMap  = bTemp;
		bTemp = northEastOnMap;
		northEastOnMap  = southWestOnMap;
		southWestOnMap  = bTemp;
		bTemp           = southEastOnMap;
		southEastOnMap  = northWestOnMap;
		northWestOnMap   = bTemp;

		// up and down locations
		Location l = centerLocation.getUpLocation();
		while (l != null){

			flipHexPoint(l.getLOSPoint());
			flipHexPoint(l.getAuxLOSPoint());
			l = l.getUpLocation();
		}

		Location l2 = centerLocation.getDownLocation();
		while (l2 != null){

			flipHexPoint(l2.getLOSPoint());
			flipHexPoint(l2.getAuxLOSPoint());
			l2 = l2.getDownLocation();
		}

		// flip bridge
		if (bridge != null){

			flipHexPoint(bridge.getCenter());
			bridge.setRotation(bridge.getRotation() >= 180 ? bridge.getRotation() - 180 : bridge.getRotation() + 180);
		}
	}

	private void flipHexPoint(Point p){

		p.x = map.getGridWidth() - p.x - 1;
		p.y = map.getGridHeight() - p.y - 1;
	}

	public void copy(Hex h){

/*
	Note: When a "half hex" is being copied, no attempt is made to resolve
	conflicting terrain types or ground level elevations. It is assumed the
	terrain types of the center location are the same and upper/lower levels
	will never be present.

*/
		// copy hex values
		name 		= h.getName();
		baseHeight	= h.getBaseHeight();

		// copy the center location
		centerLocation.copyLocation(h.getCenterLocation());

		// copy upper/lower level locations
		Location current = centerLocation;
		Location source  = h.getCenterLocation().getUpLocation();
		while (source != null){

			// create a new location
			final Location temp = new Location (
				source.getName(),
				source.getBaseHeight(),
				(Point) current.getLOSPoint().clone(),
				(Point) current.getAuxLOSPoint().clone(),
				(Point) current.getEdgeCenterPoint().clone(),
				this,
                    source.getTerrain()
			);
			temp.copyLocation(source);

			// set up/down links
			current.setUpLocation(temp);
			temp.setDownLocation(current);

			// increment the pointers
			current = current.getUpLocation();
			source  = source.getUpLocation();
		}
		Location current2 = centerLocation;
		Location source2  = h.getCenterLocation().getDownLocation();
		while (source2 != null){

			// create a new location
			final Location temp = new Location (
				source2.getName(),
				source2.getBaseHeight(),
				(Point) current2.getLOSPoint().clone(),
				(Point) current2.getAuxLOSPoint().clone(),
				(Point) current2.getEdgeCenterPoint().clone(),
				this,
                    source2.getTerrain()
			);
			temp.copyLocation(source2);

			// set up/down links
			current2.setDownLocation(temp);
			temp.setUpLocation(current2);

			// increment the pointers
			current2 = current2.getDownLocation();
			source2  = source2.getDownLocation();
		}

		// set the hexside locations
		if (northOnMap     && h.isNorthOnMap())     {
            hexsideLocations[0].copyLocation(h.getHexsideLocation(0));
            hexsideTerrain[0]  = h.getHexsideTerrain(0);
	        hexsideHasCliff[0] = h.hasCliff(0);
        }
		if (northEastOnMap && h.isNorthEastOnMap()) {
            hexsideLocations[1].copyLocation(h.getHexsideLocation(1));
            hexsideTerrain[1]  = h.getHexsideTerrain(1);
	        hexsideHasCliff[1] = h.hasCliff(1);
        }
		if (southEastOnMap && h.isSouthEastOnMap()) {
            hexsideLocations[2].copyLocation(h.getHexsideLocation(2));
            hexsideTerrain[2]  = h.getHexsideTerrain(2);
	        hexsideHasCliff[2] = h.hasCliff(2);
        }
		if (southOnMap     && h.isSouthOnMap()) {
            hexsideLocations[3].copyLocation(h.getHexsideLocation(3));
            hexsideTerrain[3]  = h.getHexsideTerrain(3);
	        hexsideHasCliff[3] = h.hasCliff(3);
        }
		if (southWestOnMap && h.isSouthWestOnMap()) {
            hexsideLocations[4].copyLocation(h.getHexsideLocation(4));
            hexsideTerrain[4]  = h.getHexsideTerrain(4);
	        hexsideHasCliff[4] = h.hasCliff(4);
        }
		if (northWestOnMap && h.isNorthWestOnMap()) {
            hexsideLocations[5].copyLocation(h.getHexsideLocation(5));
            hexsideTerrain[5]  = h.getHexsideTerrain(5);
	        hexsideHasCliff[5] = h.hasCliff(5);
        }

		// bridges
		if (bridge == null && h.getBridge() != null){

			setBridge(new Bridge(
				h.getBridge().getTerrain(),
				h.getBridge().getRoadLevel(),
				h.getBridge().getRotation(),
				new Location(),
				h.getBridge().isSingleHex(),
				(Point) h.getBridge().getCenter().clone()
			));
		}

        //stairways
        stairway = h.hasStairway();
	}

    public boolean hasStairway() {
        return stairway;
    }

    public void setStairway(Boolean stairway) {
        this.stairway = stairway;
    }

}

