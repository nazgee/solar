package eu.nazgee.prank.solar;

import org.andengine.util.color.Color;

public class Consts {
	public static final int CAMERA_WIDTH = 480;
	public static final int CAMERA_HEIGHT = 720;
	public static final int PANEL_SIZE_WIDTH = CAMERA_WIDTH;
	public static final int PANEL_SIZE_HEIGHT = 600;

	public static final String PREFS_NAME = "myprefs";
	public static final String PREFS_KEY_LIGHTMIN = "lightmin";
	public static final String PREFS_KEY_LIGHTMAX = "lightmax";

	public static final String FONT = "ELEKTRA.ttf";
	public static final int PANEL_COLS = 4;
	public static final int PANEL_ROWS = 12;
	public static final int CELL_SIZE_W = PANEL_SIZE_WIDTH/PANEL_COLS;
	public static final int CELL_SIZE_H = PANEL_SIZE_HEIGHT/PANEL_ROWS;
	
	public static final float CALIBRATION_TIME = 5;
	public static final float CHARGE_THRESHOLD = 0.07f;
	
	

	public static final Color COLOR_TEXT = Color.RED;
}

