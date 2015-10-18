using UnityEngine;
using System.Collections;

public class Util {

	public static int GetPlatfom() {

#if UNITY_STANDALONE || UNITY_WEBPLAYER
#else
#endif
		return 0;
	}

	public static int GetScreenWidth() {
		return Screen.width;
	}

	public static int GetScreenHeight() {
		return Screen.height;
	}

	/**
	 * 
	 * dp = pixels / dpi
	 * 
	 * 
	 **/
	public static float GetScreenWidthInDip() {
		return p2d(Screen.width);
	}

	public static float GetScreenHeightInDip() {
		return p2d(Screen.height);
	}

	public static int d2p(float d) {
		return (int)Mathf.Round(d * Screen.dpi);
	}

	public static float p2d(int p) {
		return p / Screen.dpi;
	}

	public static float getDpi() {
		return Screen.dpi;
	}
}

