﻿using UnityEngine;
using System.Collections;
using System.Collections.Generic;

// [RequireComponent(typeof
public class MainControl : MonoBehaviour, Tetris.Callback
{
//	public struct Position {
//		public int row;
//		public int col;
//
//		public Position(int r, int c) {
//			this.row = r;
//			this.col = c;
//		}
//	}

	public class PlayerData
	{
		public GameObject cube_parent;
		public List<GameObject> cube_list;
	}

	// cube prefab
	public GameObject cube_;

	// plane
	public GameObject plane_;

	// offsets
	private Vector3 plane_offset_;
	private Vector3 cube_offset_;

	// unit: block
	private int MAP_WIDTH;
	// uni: block
	private int MAP_HEIGHT;
	private int[,] map_;
	private GameObject[,] block_map_;
	private PlayerData cur_player_;
	//
	private Timer step_timer_ = new Timer (1000);
	//
	private const float input_interval_ = 0.1f;
	private float left_buttom_timestamp_ = 0.0f;
	private float right_buttom_timestamp_ = 0.0f;
	private float down_buttom_timestamp_ = 0.0f;

	//
	private Tetris tetris_;

	private bool pause_ = false;

	private void InitPlayer ()
	{
		cur_player_ = new PlayerData ();
		cur_player_.cube_parent = new GameObject ("cube_parent");
		cur_player_.cube_list = new List<GameObject> ();
	}
	
	private void DestroyPlayer ()
	{
		GameObject[] obj_array = cur_player_.cube_list.ToArray ();
		cur_player_.cube_list.Clear ();
		foreach (GameObject o in obj_array) {
			Destroy (o);
		}
	}
    
	/**
	 * 
	 * - normalize the coordination of playground, as the origin
	 *   is the left corner of the bottom plane
	 *   
	 *   transform = - center_coor
	 *               + (playgound_width/2 - cube_width/2, - cube_height/2, 0)
	 *
	 * 
	 */
	// Use this for initialization
	void Start ()
	{
		// -----------------------------------
		Vector3 plane_size = plane_.GetComponent<Renderer>().bounds.size;
		Debug.Log ("size: " + plane_size);

		MAP_WIDTH = (int)Mathf.Round (plane_size.x / Tetris.BLOCK_WIDTH);
		MAP_HEIGHT = (int)Mathf.Round(plane_size.z / Tetris.BLOCK_HEIGHT);

		Debug.Log ("width=" + MAP_WIDTH + ", height=" + MAP_HEIGHT);

		// -----------------------------------
		// to adapt all kinds of situation, we should use transform matrix instead of following things...
		/*
		offset_ = new Vector3 (-bottom_.transform.position.x + (MAP_WIDTH * Tetris.BLOCK_WIDTH) / 2 - Tetris.BLOCK_WIDTH / 2,
		                       -bottom_.transform.position.y - (Tetris.BLOCK_HEIGHT / 2),
		                       0);
		*/
		plane_offset_ = new Vector3 (-plane_size.x / 2, 0, -plane_size.z / 2);
		cube_offset_ = new Vector3 (Tetris.BLOCK_WIDTH/2, 0, Tetris.BLOCK_HEIGHT/2);

		// -----------------------------------
		tetris_ = Tetris.Create(MAP_WIDTH, MAP_HEIGHT, this);

		map_ = tetris_.map; // new int[MAP_HEIGHT, MAP_WIDTH];

		block_map_ = new GameObject[MAP_HEIGHT, MAP_WIDTH];
		// initialize the matrix that can transform global coordination to local coordination
		for (int r = 0; r < map_.GetLength(0); ++r) {
			for (int c = 0; c < map_.GetLength(1); ++c) {
				block_map_ [r, c] = null;
			}
		}

		InitPlayer ();

		// ----------------------------------
		tetris_.start();
	}

	private bool in_animation_ = false;
	private IEnumerator TestAnimation() {
		in_animation_ = true;

		yield return new WaitForSeconds(5.0f);

		in_animation_ = false;
	}

	public void OnPlayerCreated(Tetris.Position pos, int[,] block) {
		// Trace.Assert(cur_player_.cube_list.Count == 0);
		int block_height = block.GetLength(0);
		int block_width = block.GetLength(1);

		cur_player_.cube_parent.transform.position = MapToWorld(pos);

		GameObject obj;
		for (int r = 0; r < block_height; ++r) {
			for (int c = 0; c < block_width; ++c) {
				if (block[r, c] == 0) {
					continue;
				}
				obj = (GameObject) Instantiate(
					cube_, MapToWorld(pos + new Tetris.Position(-r, c)), Quaternion.identity);
				obj.transform.parent = cur_player_.cube_parent.transform;
				cur_player_.cube_list.Add(obj);
			}
		}

		StartCoroutine(TestAnimation());
	}

	public void OnPlayerDestroyed() {
		DestroyPlayer();
	}

	public void OnPlayerPositionChanged (Tetris.Position old_pos, Tetris.Position new_pos) {
		cur_player_.cube_parent.transform.position = MapToWorld(new_pos);
	}
	
	public void OnPlayerRotateChanged (Tetris.Position pos, int old_rot, int new_rot, int[,] block) {
		DestroyPlayer();
		OnPlayerCreated(pos, block);
	}
	
	public void OnCubePositionChanged (Tetris.Position old_pos, Tetris.Position new_pos) {
		int old_row = (int)old_pos.row;
		int old_col = (int)old_pos.col;
		int new_row = (int)new_pos.row;
		int new_col = (int)new_pos.col;

		// Destroy(block_map_[old_row, old_col]);
		GameObject obj = block_map_[old_row, old_col];
		block_map_[old_row, old_col] = null;
		block_map_[new_row, new_col] = obj;
		obj.transform.position = MapToWorld(new_pos);
	}
	
	public void OnCubeDestroyed(Tetris.Position pos) {
		int row = (int)pos.row;
		int col = (int)pos.col;
		GameObject obj = block_map_[row, col];
		block_map_[row, col] = null;
		Destroy(obj);
	}
	
	public void OnCubeCreated(Tetris.Position pos) {
		int row = (int)pos.row;
		int col = (int)pos.col;
		if (block_map_[row, col] != null) {
			Destroy(block_map_[row, col]);
		}

		block_map_[row, col] = (GameObject) Instantiate(
			cube_, MapToWorld(pos), Quaternion.identity);
	}

	// Update is called once per frame
	void Update ()
	{
		if (in_animation_) {
			return;
		}

		if (Input.GetKeyDown(KeyCode.R)) {
			tetris_.Reset();

		}

		if (Input.GetKeyDown(KeyCode.Space)) {
			pause_ = ! pause_;
		}

		if (pause_) {
			return;
		}

		long delta_in_ms = (long)Mathf.Round (Time.deltaTime * 1000);
		step_timer_.update (delta_in_ms);

		if (step_timer_.isTimeUp ()) {
			tetris_.Move(new Tetris.Position(-1, 0));
		}

		if (Input.GetKeyDown (KeyCode.UpArrow)) {
			// RotatePlayer ();
			tetris_.Rotate();
		}

		if (Input.GetKey (KeyCode.DownArrow) && Time.time > down_buttom_timestamp_) {
			// maybe we shouldn't move player down again when step timer is timeup?
			tetris_.Move (new Tetris.Position(-1, 0));
			down_buttom_timestamp_ = Time.time + input_interval_;
		}

		if (Input.GetKey (KeyCode.LeftArrow) && Time.time > left_buttom_timestamp_) {
			tetris_.Move(new Tetris.Position (0, -1));
			left_buttom_timestamp_ = Time.time + input_interval_;
		}

		if (Input.GetKey (KeyCode.RightArrow) && Time.time > right_buttom_timestamp_) {
			tetris_.Move (new Tetris.Position (0, 1));
			right_buttom_timestamp_ = Time.time + input_interval_;
		}
	}
	
	void OnGUI ()
	{
		float begin_x = 10;
		float begin_y = 10;
		float row_height = 20;
		float row_width = 1000;

		// score
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("score={0}", tetris_.score));
		begin_y += row_height;

		// mouse position
		// Vector3 mouse_pos = Camera.main.ScreenToWorldPoint (Input.mousePosition);
		Vector3 mouse_pos = Input.mousePosition;
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("mouse=({0,5:F}, {1,5:F}, {2,5:F})",
		               mouse_pos.x, mouse_pos.y, mouse_pos.z));
		begin_y += row_height;

//		// mouse2map
//		Vector2 map_mouse = WorldToMap (Camera.main.ScreenToWorldPoint (mouse_pos));
//		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
//		           string.Format ("map_mouse=({0,5:F}, {1,5:F})",
//		               map_mouse.y, map_mouse.x));
//		begin_y += row_height;

		// mouse2map
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("Time.time={0,5:F}", Time.time));
		begin_y += row_height;

		// --------------------------
		// Pause menu
		if (!ShowPauseMenu()) {
			return;
		}
		float area_width = 100;
		float area_height = 300;
		GUILayout.BeginArea(new Rect(
			(Screen.width/2)-(area_width/2),
			(Screen.height/2), area_width, area_height));
		{
			if (!tetris_.IsDead()) {
				if (GUILayout.Button("Continue")) {
					Debug.Log ("continue");
					pause_ = false;
				}
			}

			if (GUILayout.Button("Reset")) {
				Debug.Log ("reset");
				pause_ = false;
				tetris_.Reset();
			}
		}
		GUILayout.EndArea();
    }

	private bool ShowPauseMenu() {
		if (pause_) {
			return true;
		}

		if (tetris_.IsDead()) {
			return true;
		}

		return false;
	}

//	public Vector3 MapToTetris (Vector2 point)
//	{
//		return new Vector3 (
//			point.x * Tetris.BLOCK_WIDTH, point.y * Tetris.BLOCK_HEIGHT, 0);
//	}
//	
//	public Vector2 TetrisToMap (Vector3 point)
//	{
//		float col = Mathf.Round (point.x / Tetris.BLOCK_WIDTH);
//		float row = Mathf.Round (point.y / Tetris.BLOCK_HEIGHT);
//		return new Vector2 (col, row);
//	}


//	public Vector3 WorldToTetris (Vector3 point)
//	{
//		// return point + offset_;
//
//		Vector3 logic_pos = plane_.transform.InverseTransformPoint(point) -  plane_offset_ - cube_offset_;
//
//		return new Vector3(logic_pos.x / );
//	}
//	
//	public Vector3 TetrisToWorld (Vector3 point)
//	{
//		int row = Mathf.Round(point.x);
//		int col = Mathf.Round(point.z);
//		return point - offset_;
//	}

	/**
	 * 
	 * real = transfer(logic + plane_offset + cube_offset)
	 * 
	 */
	public Tetris.Position WorldToMap (Vector3 pos)
	{
		Vector3 logic = plane_.transform.InverseTransformPoint(pos) - plane_offset_ - cube_offset_;
		return new Tetris.Position(
			(int)(Mathf.Round(logic.z / Tetris.BLOCK_HEIGHT)),
			(int)(Mathf.Round(logic.x / Tetris.BLOCK_WIDTH)));
	}
	
	public Vector3 MapToWorld (Tetris.Position pos)
	{
		// return TetrisToWorld (MapToTetris (pos));
		Vector3 real = new Vector3(pos.col*Tetris.BLOCK_WIDTH, 1, pos.row*Tetris.BLOCK_HEIGHT);
		return plane_.transform.TransformPoint(real + plane_offset_ + cube_offset_);
	}
}
