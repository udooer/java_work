package PamguardMVC;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import PamController.OfflineDataStore;

import dataMap.DataMapDrawing;

/**
 * Class which get's held within a PamDataBlock which 
 * provides summary information on data within that 
 * block when operating in viewer mode. 
 * <p>
 * It is possible that some datablocks may own two of these
 * things - one for database data and one for binary stored data.  
 * @author Doug Gillespie
 *
 */
abstract public class OfflineDataMap<TmapPoint extends OfflineDataMapPoint> {

	private PamDataBlock  parentDataBlock; 
	
	private OfflineDataStore offlineDataStore;

	private List<TmapPoint> mapPoints;
	
	private DataMapDrawing specialDrawing;

	/**
	 * time of the first data available for this data block
	 */
	private long firstDataTime;

	/**
	 * time of the last data available for this data block
	 */
	private long lastDataTime;

	/**
	 * Point in this store with most data. 
	 */
	private long highestPoint;

	/**
	 * Lowest point in this store (can be zero)
	 */
	private long lowestPoint; 

	/**
	 * Lowest non zero point in the store (can be 
	 * zero if no data or if highestPoint is zero)
	 */
	private long lowestNonZeroPoint;

	/**
	 * Vertical scaling - scale each Map data point to 
	 * a data rate, or don't bother scaling at all. 
	 */
	public static final int SCALE_NONE = 0;
	public static final int SCALE_PERSECOND = 1;
	public static final int SCALE_PERMINUTE = 2;
	public static final int SCALE_PERHOUR = 3;
	public static final int SCALE_PERDAY = 4;

	public OfflineDataMap(OfflineDataStore offlineDataStore, PamDataBlock parentDataBlock) {
		super();
		this.offlineDataStore = offlineDataStore;
		this.parentDataBlock = parentDataBlock;
		mapPoints = Collections.synchronizedList(new LinkedList<TmapPoint>());
		firstDataTime = Long.MAX_VALUE;
		lastDataTime = Long.MIN_VALUE;
	}

	/**
	 * Get an iterator over the map contents. 
	 * <p>
	 * Objects using this iterator should synchronise on the map object first. 
	 * @return an iterator over the map contents.
	 *  
	 */
	public Iterator<TmapPoint> getListIterator() {
		return mapPoints.iterator();
	}

	/**
	 * @return the parentDataBlock
	 */
	public PamDataBlock getParentDataBlock() {
		return parentDataBlock;
	}

	/**
	 * Add a new map point into the list. 
	 * @param mapPoint new map point to add
	 */
	synchronized public void addDataPoint(TmapPoint mapPoint) {
		boolean first = (mapPoints.size() == 0);
		if (mapPoint.getStartTime() > 0) {
			firstDataTime = Math.min(firstDataTime, mapPoint.getStartTime());
		}
		if (mapPoint.getEndTime() > 0) {
			lastDataTime = Math.max(lastDataTime, mapPoint.getEndTime());
		}
		mapPoints.add(mapPoint);

		// sort out range information
		int n = mapPoint.getNDatas();
		if (first) {
			highestPoint = lowestPoint = lowestNonZeroPoint = n;
		}
		else {
			highestPoint = Math.max(highestPoint, n);
			lowestPoint = Math.min(lowestPoint, n);
			if (lowestNonZeroPoint == 0) {
				lowestNonZeroPoint = n;
			}
			else if (n > 0) {
				lowestNonZeroPoint = Math.min(lowestNonZeroPoint, n);
			}
		}
	}

	/**
	 * @return the total number of map points.  
	 */
	public int getNumMapPoints() {
		return mapPoints.size();
	}

	/**
	 * Sort all map points into ascending order based on start time.
	 * <p>
	 * this should be called after all data have been loaded. 
	 */
	synchronized public void sortMapPoints() {
		Collections.sort(mapPoints);
	}

	/**
	 * @return the firstDataTime
	 */
	public long getFirstDataTime() {
		return firstDataTime;
	}

	/**
	 * @param firstDataTime the firstDataTime to set
	 */
	public void setFirstDataTime(long firstDataTime) {
		this.firstDataTime = firstDataTime;
	}

	/**
	 * @return the lastDataTime
	 */
	public long getLastDataTime() {
		return lastDataTime;
	}

	/**
	 * @param lastDataTime the lastDataTime to set
	 */
	public void setLastDataTime(long lastDataTime) {
		this.lastDataTime = lastDataTime;
	}

	/**
	 * @param vScaleType 
	 * @return the highestPoint
	 */
	public double getHighestPoint(int vScaleType) {
		if (vScaleType == SCALE_NONE) {
			return highestPoint;
		}
		else {
			synchronized (this) {
				TmapPoint mapPoint;
				double hp = 0;
				double ap;
				Iterator<TmapPoint> it = getListIterator();
				while (it.hasNext()) {
					mapPoint = it.next();
					ap = scaleData(mapPoint.getNDatas(), mapPoint.getEndTime()-mapPoint.getStartTime(), vScaleType);
					hp = Math.max(hp, ap);
				}
				return hp;
			}
		}
	}
	/**
	 * @return the lowestPoint
	 */
	public double getLowestPoint(int vScaleType) {
		if (vScaleType == SCALE_NONE || lowestPoint == 0) {
			return lowestPoint;
		}
		else {
			synchronized (this) {
				TmapPoint mapPoint;
				double lp = Double.MAX_VALUE;
				double ap;
				Iterator<TmapPoint> it = getListIterator();
				while (it.hasNext()) {
					mapPoint = it.next();
					ap = scaleData(mapPoint.getNDatas(), mapPoint.getEndTime()-mapPoint.getStartTime(), vScaleType);
					lp = Math.min(lp, ap);
				}
				return lp;
			}
		}
	}

	/**
	 * @return the lowestNonZeroPoint
	 */
	public double getLowestNonZeroPoint(int vScaleType) {
		if (vScaleType == SCALE_NONE || lowestNonZeroPoint == 0) {
			return lowestNonZeroPoint;
		}
		else {
			synchronized (this) {
				TmapPoint mapPoint;
				double lp = Double.MAX_VALUE;
				double ap;
				Iterator<TmapPoint> it = getListIterator();
				while (it.hasNext()) {
					mapPoint = it.next();
					if (mapPoint.getNDatas() == 0) {
						continue;
					}
					ap = scaleData(mapPoint.getNDatas(), mapPoint.getEndTime()-mapPoint.getStartTime(), vScaleType);
					lp = Math.min(lp, ap);
				}
				return lp;
			}
		}
	}

	public static double scaleData(double count, long duration, int scaleType) {
		if (duration > 0) {
			double secs = duration / 1000.;
			switch (scaleType) {
			case SCALE_NONE:
				return count;
			case SCALE_PERSECOND:
				return count / secs;
			case SCALE_PERMINUTE:
				return count / secs * 60;
			case SCALE_PERHOUR:
				return count / secs * 3600;
			case SCALE_PERDAY:
				return count / secs * 3600 * 24;
			}
		}
		return count;
	}

	/**
	 * @return the offlineDataSource
	 */
	public OfflineDataStore getOfflineDataSource() {
		return offlineDataStore;
	}

	/**
	 * @return the specialDrawing
	 */
	public DataMapDrawing getSpecialDrawing() {
		return specialDrawing;
	}

	/**
	 * @param specialDrawing the specialDrawing to set
	 */
	public void setSpecialDrawing(DataMapDrawing specialDrawing) {
		this.specialDrawing = specialDrawing;
	}


}
