using UnityEngine;
using System.Collections;

public class Tetris
{
	public interface Callback {
		void OnPlayerCreated(Position pos, int[,] block); //int type, int rot);
		
		void OnPlayerDestroyed();
		
		void OnPlayerPositionChanged (Position old_pos, Position new_pos);
		
		void OnPlayerRotateChanged (Position pos, int old_rot, int new_rot, int[,] block);
		
		void OnCubePositionChanged (Position old_pos, Position new_pos);
		
		void OnCubeDestroyed(Position pos);
		
		void OnCubeCreated(Position pos);
	}

	public struct Position
	{
		public int row;
		public int col;

		public Position (int r, int c)
		{
			this.row = r;
			this.col = c;
		}

		public static Position operator +(Position v1, Position v2) {
			return new Position(v1.row + v2.row, v1.col + v2.col);
		}
	};

	public struct Player
	{
		public Position pos;
		public int type;
		public int rot;
		public int[,] block_data;
		public bool dead;
	};

	public const float BLOCK_WIDTH = 1;
	public const float BLOCK_HEIGHT = 1;
	public const int BLOCK_NONE = -1;
	public const int BLOCK_L = 0;
	public const int BLOCK_J = 1;
	public const int BLOCK_T = 2;
	public const int BLOCK_O = 3;
	public const int BLOCK_Z = 4;
	public const int BLOCK_S = 5;
	public const int BLOCK_I = 6;
	public const int COLOR_1 = 0;
	public readonly static int[,,,] BLOCK_LIST = {
		// L
		{
			{ { 1, 0, 0, 0 }, { 1, 1, 1, 0 }, { 1, 1, 0, 0 }, { 0, 0, 1, 0 }, },
			{ { 1, 0, 0, 0 }, { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 1, 1, 1, 0 }, },
			{ { 1, 1, 0, 0 }, { 0, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 }, },
			{ { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, },
		},
		
		// J
		{
			{ { 0, 1, 0, 0 }, { 1, 0, 0, 0 }, { 1, 1, 0, 0 }, { 1, 1, 1, 0 }, },
			{ { 0, 1, 0, 0 }, { 1, 1, 1, 0 }, { 1, 0, 0, 0 }, { 0, 0, 1, 0 }, },
			{ { 1, 1, 0, 0 }, { 0, 0, 0, 0 }, { 1, 0, 0, 0 }, { 0, 0, 0, 0 }, },
			{ { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, },
		},
		
		// T
		{
			{ { 1, 1, 1, 0 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 1, 0, 0, 0 }, },
			{ { 0, 1, 0, 0 }, { 1, 1, 0, 0 }, { 1, 1, 1, 0 }, { 1, 1, 0, 0 }, },
			{ { 0, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 0 }, { 1, 0, 0, 0 }, },
			{ { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, },
		},
		
		// O
		{
			{ { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, },
			{ { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, { 1, 1, 0, 0 }, },
			{ { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, },
			{ { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, },
		},
		
		// Z
		{
			{ { 1, 1, 0, 0 },{ 0, 1, 0, 0 },{ 1, 1, 0, 0 },{ 0, 1, 0, 0 }, },
			{ { 0, 1, 1, 0 },{ 1, 1, 0, 0 },{ 0, 1, 1, 0 },{ 1, 1, 0, 0 }, },
			{ { 0, 0, 0, 0 },{ 1, 0, 0, 0 },{ 0, 0, 0, 0 },{ 1, 0, 0, 0 }, },
			{ { 0, 0, 0, 0 },{ 0, 0, 0, 0 },{ 0, 0, 0, 0 },{ 0, 0, 0, 0 }, },
		},
		
		// S
		{
			{ { 0, 1, 1, 0 },{ 1, 0, 0, 0 },{ 0, 1, 1, 0 },{ 1, 0, 0, 0 }, },
			{ { 1, 1, 0, 0 },{ 1, 1, 0, 0 },{ 1, 1, 0, 0 },{ 1, 1, 0, 0 }, },
			{ { 0, 0, 0, 0 },{ 0, 1, 0, 0 },{ 0, 0, 0, 0 },{ 0, 1, 0, 0 }, },
			{ { 0, 0, 0, 0 },{ 0, 0, 0, 0 },{ 0, 0, 0, 0 },{ 0, 0, 0, 0 }, },
		},
		
		// I
		{
			{ { 1, 0, 0, 0 },{ 1, 1, 1, 1 },{ 1, 0, 0, 0 },{ 1, 1, 1, 1 }, },
			{ { 1, 0, 0, 0 },{ 0, 0, 0, 0 },{ 1, 0, 0, 0 },{ 0, 0, 0, 0 }, },
			{ { 1, 0, 0, 0 },{ 0, 0, 0, 0 },{ 1, 0, 0, 0 },{ 0, 0, 0, 0 }, },
			{ { 1, 0, 0, 0 },{ 0, 0, 0, 0 },{ 1, 0, 0, 0 },{ 0, 0, 0, 0 }, },
		},
	};
	
	public static int GetRandomBlock ()
	{
		// return 6;
		return Random.Range (0, 7);
	}
	
	public static int GetRandomRotate ()
	{
		// return 0;
		return Random.Range (0, 4);
	}

	public static int NextRotate (int rot)
	{
		return (rot + 1) % 4;
	}

	// -----------------------------------

	public static Tetris Create (int map_width, int map_height, Callback callback)
	{
		return new Tetris (map_width, map_height, callback);
	}

	private Callback callback_;

	private int[,] map_;
	private readonly int map_height_;
	private readonly int map_width_;
	Player player_ = new Player ();

	private int score_ = 0;

	public int score {
		get { return score_; }
	}

	protected Tetris (int map_width, int map_height, Callback callback)
	{
		map_height_ = map_height;
		map_width_ = map_width;
		callback_ = callback;

		map_ = new int[map_height, map_width];

		for (int r = 0; r < map_.GetLength(0); ++r) {
			for (int c = 0; c < map_.GetLength(1); ++c) {
				map_ [r, c] = 0;
			}
		}

		player_.pos = new Position(0, 0);
	}

	public void start () {
		SpawnBlock ();
	}

	public void Move(Position mov) {
		if (player_.dead) {
			return;
		}

		Position old_pos = player_.pos;
		Position new_pos = player_.pos + mov;
		if (CanPut(new_pos, player_.block_data)) {
			player_.pos = new_pos;
			// playerPositionChangeDelegate_(old_pos, new_pos);
			callback_.OnPlayerPositionChanged(old_pos, new_pos);
		} else {
			if (mov.row != 0) {
				Merge();
				RemoveFullLines();
				SpawnBlock();
			}
		}
	}

	public void Rotate() {
		if (player_.dead) {
			return;
		}

		Position pos = player_.pos;
		int type = player_.type;
		int old_rot = player_.rot;
		int rot = NextRotate(old_rot);

		int[,] new_block = GetBlockData(type, rot);

		if (!CanPut(pos, new_block)) {
			return;
		}

		player_.block_data = new_block;
		player_.rot = rot;

		// playerRotateChangeDelegate_(pos, old_rot, rot, new_block);
		callback_.OnPlayerRotateChanged(pos, old_rot, rot, new_block);
	}

	private void Merge () {
		Position pos = player_.pos;
		int[,] block = player_.block_data;
		int block_height = block.GetLength (0);
		int block_width = block.GetLength (1);
		for (int r = 0; r < block_height; ++r) {
			for (int c = 0; c < block_width; ++c) {
				if (block [r, c] != 0) {
					// 10 score for every cube
					score_ += 10;
					int new_row = (int)pos.row - r;
					int new_col = (int)pos.col + c;
					int old_type = map_[new_row, new_col];
					int new_type = block[r, c];
					map_[new_row, new_col] = new_type;
					callback_.OnCubeCreated(new Position(new_row, new_col));
				}
			}
		}
		// playerDestroyDelegate_();
		callback_.OnPlayerDestroyed();
	}

	private void RemoveFullLines () {
		int line_to_copy = -1;
		int line_to_delete = 0;
		for (int r = 0; r < map_height_; ++r) {
			if (IsEmptyLine (r)) {
				// break;
				continue;
			}
			if (!IsFullLine (r)) {
				if (line_to_copy >= 0) {
					// move upper line down
					for (int c = 0; c < map_width_; ++c) {
						if (map_ [r, c] == 0) {
							continue;
						}
						map_ [line_to_copy, c] = map_ [r, c];
						map_ [r, c] = 0;
						callback_.OnCubePositionChanged(
							new Position(r, c), new Position(line_to_copy, c));
					}
					line_to_copy += 1;
				}
				continue;
			} else {
				if (line_to_copy < 0) {
					line_to_copy = r;
				}
				++line_to_delete;
				// clear current line
				for (int c = 0; c < map_width_; ++c) {
					map_ [r, c] = 0;
					callback_.OnCubeDestroyed(new Position(r, c));
				}
			}
		}

		score_ += ((int)Mathf.Pow(2, line_to_delete) - 1) * 100;
	}

	public bool IsDead() {
		return player_.dead;
	}

	public void Reset() {
		for (int r = 0; r < map_height_; ++r) {
			for (int c = 0; c < map_width_; ++c) {
				map_[r, c] = 0;
				callback_.OnCubeDestroyed(new Position(r, c));
			}
		}

		callback_.OnPlayerDestroyed();
		SpawnBlock();
	}

	public void SpawnBlock () {
		Position pos = new Position (map_height_ - 1, map_width_ / 2);
		int type = GetRandomBlock ();
		int rot = GetRandomRotate ();

		int[,] block = GetBlockData (type, rot);
		if (!CanPut (pos, block)) {
			// TODO: DEAD FLAG
			player_.block_data = null;
			player_.dead = true;
			return;
		}

		player_.pos = pos;
		player_.rot = rot;
		player_.type = type;
		player_.block_data = block;
		player_.dead = false;

		// playerCreateDelegate_(pos, block);
		callback_.OnPlayerCreated(pos, block);
	}

	public int mapHeight {
		get { return map_height_; }
	}

	public int mapWidth {
		get { return map_width_; }
	}

	public int[,] map {
		get { return map_; }
	}

	// for the different coordination, transformation is needed
	// in data-structure, the origin is in left-top corner
	// but in map-view, the origin is in left-bottom corner
	public int[,] GetBlockData (int type, int rot)
	{
		// return BLOCK_LIST[type, 0];
		int block_height = BLOCK_LIST.GetLength (1);
		int block_width = BLOCK_LIST.GetLength (2);
		int[,] ret = new int[block_height, block_width];
		for (int i = 0; i < block_height; ++i) {
			for (int j = 0; j < block_width; ++j) {
				ret [i, j] = BLOCK_LIST [type, i, rot, j];
			}
		}
		
		return ret;
	}

	public bool CanPut (Position pos, int[,] block)
	{
		int block_height = block.GetLength (0);
		int block_width = block.GetLength (1);
		for (int r = 0; r < block_height; ++r) {
			for (int c = 0; c < block_width; ++c) {
				if (block [r, c] == 0) {
					continue;
				}
				int new_row = (int)pos.row - r;
				int new_col = (int)pos.col + c;
				if (!InMap (new Position(new_row, new_col))) {
					return false;
				}
				if (map_ [new_row, new_col] != 0) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public bool InMap (Position pos)
	{
		if (pos.col < 0 || pos.col >= map_width_) {
			return false;
		}
		
		if (pos.row < 0 || pos.row >= map_height_) {
			return false;
		}
		
		return true;
	}

	public bool IsFullLine (int r)
	{
		for (int c = 0; c < map_width_; ++c) {
			if (map_ [r, c] == 0) {
				return false;
			}
		}
		
		return true;
	}
	
	public bool IsEmptyLine (int r)
	{
		for (int c = 0; c < map_width_; ++c) {
			if (map_ [r, c] == 1) {
				return false;
			}
		}
		
		return true;
	}
}
