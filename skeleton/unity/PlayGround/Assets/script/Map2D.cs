using UnityEngine;
using System.Collections;

public class Map2D<T> {

	private T[,] data_map_;

	public Map2D(int width, int height) {
		data_map_ = new T[height, width];
	}

	public T Get(int row, int col) {
		return data_map_[row, col];
	}
}
