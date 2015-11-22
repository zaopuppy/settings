using UnityEngine;
using System.Collections;

[RequireComponent(typeof(GameObject))]
public class InputController : MonoBehaviour
{
	public class Position
	{
		public int row;
		public int col;

		public Position (int r, int c)
		{
			this.row = r;
			this.col = c;
		}
	}

	public GameObject plane_;
	public int plane_width_;
	public int plane_height_;
	public GameObject cube_;

	private Vector3 plane_offset_;
	private Vector3 cube_offset_;
	// private Quaternion plane_rotation_;
	// private Quaternion cube_rotation_;

	// player
	// private int cur_player_row_ = 0;
	// private int cur_player_col_ = 0;

	// cube(what's the difference!? T_T)
	// private int cur_cube_row_ = 0;
	// private int cur_cube_col_ = 0;
	private Position cur_cube_pos_ = new Position(0, 0);

	private int[,] map_;
	private int map_width_;
	private int map_height_;
	private float tile_width_ = 1.0f;
	// private int tile_height_ = 0.5f;

	// Use this for initialization
	void Start ()
	{
		// get map size
		// TODO: not rotation independent
		// Vector3 plane_size = plane_.GetComponent<Renderer> ().bounds.size;
		// Debug.Log ("plane-size: " + plane_size);
		map_width_ = (int)Mathf.Round (plane_width_ / tile_width_);
		map_height_ = (int)Mathf.Round (plane_height_ / tile_width_);

		plane_offset_ = new Vector3 (-plane_width_ / 2, 0, -plane_height_ / 2);
		cube_offset_ = new Vector3 (tile_width_ / 2, 0, tile_width_ / 2);
		// plane_rotation_ = plane_.transform.rotation;

		Debug.Log (string.Format ("map info: width={0,5:F}, height={1,5:F}",
		                         map_width_, map_height_));
		Debug.Log (string.Format ("offset info: plane={0}, cube={1}",
		                          plane_offset_.ToString(), cube_offset_.ToString()));

		// initialize map
		map_ = new int[map_height_, map_width_];
		for (int r = 0; r < map_height_; ++r) {
			for (int c = 0; c < map_width_; ++c) {
				map_ [r, c] = 0x00;
			}
		}

		// put cube
		// Position cube_pos = World2Map(cube_.transform.position);
		// cur_cube_row_ = cube_pos.row;
		// cur_cube_col_ = cube_pos.col;

		MoveCube ();

		Debug.Log ("cube pos: " + cube_.transform.position);
		Debug.Log (string.Format ("row={0}, col={1}", cur_cube_pos_.row, cur_cube_pos_.col));
	}
	
	// Update is called once per frame
	void Update ()
	{
		if (Input.GetKeyDown (KeyCode.Space)) {
			Debug.Log ("trigger");
			// begin_time_ = Time.time;
			// StartCoroutine(AnimationCoroutineRotate());
			// StartCoroutine(AnimationCoroutine());
			// StartCoroutine (AnimationCoroutineByEdge ());
		}

		Position pos_delta = new Position(0, 0);

		// LEFT
		if (!flipping && Input.GetKeyDown (KeyCode.H)) {
			// Debug.Log ("left");
			if (CanMove (0, -1)) {
				pos_delta.col += -1;
			}
		}
		// RIGHT
		if (!flipping && Input.GetKeyDown (KeyCode.L)) {
			// Debug.Log ("right");
			if (CanMove (0, 1)) {
				pos_delta.col += 1;
			}
		}
		// UP
		if (!flipping && Input.GetKeyDown (KeyCode.K)) {
			// Debug.Log ("up");
			if (CanMove (1, 0)) {
				pos_delta.row += 1;
			}
		}
		// DOWN
		if (!flipping && Input.GetKeyDown (KeyCode.J)) {
			// Debug.Log ("down");
			if (CanMove (-1, 0)) {
				pos_delta.row += -1;
			}
		}

		// MoveCube ();
		/*
		 * if (flipping) {
			return;
		}*/
		
		if (pos_delta.row != 0 ||
		    pos_delta.col != 0) {
			StartCoroutine(FlipToPos(pos_delta));
		}

		// TODO: still can't handle this situation
		// rotate the plane
		// TestPlaneRotate();
	}

	private void TestPlaneRotate() {
		// Vector3 delta_change = new Vector3(.1f, .1f, .1f);
		// plane_.transform.Rotate(delta_change);
		// cube_.transform.Rotate(delta_change);

		// plane_.transform.RotateAround(plane_.transform.position, new Vector3(0.1f, 1.0f, 0.0f), 0.5f);
		// cube_.transform.RotateAround(plane_.transform.position, new Vector3(0.1f, 1.0f, 0.0f), 0.5f);

		plane_.transform.position += new Vector3(0.0f, 0.01f, 0.0f);
		cube_.transform.position += new Vector3(0.0f, 0.01f, 0.0f);

	}

	private bool InMap (int row, int col)
	{
		if (row < 0 || row >= map_height_) {
			return false;
		}

		if (col < 0 || col >= map_width_) {
			return false;
		}

		return true;
	}

	private bool CanMove (int row, int col)
	{
		int new_row = cur_cube_pos_.row + row;
		int new_col = cur_cube_pos_.col + col;
		if (InMap (new_row, new_col)) {
			return true;
			// cur_player_row_ = new_row;
			// cur_player_col_ = new_col;
		}
		return false;
	}

//	private bool CubePositionChanged() {
//		if (cur_cube_row_ == cur_player_row_ &&
//		    cur_cube_col_ == cur_player_col_) {
//			return false;
//		}
//
//		Debug.Log (string.Format("cube pos={0}, should be={1}",
//		                         cube_.transform.position,
//		                         Map2World(cur_player_row_, cur_player_col_, 0)));
//
//		return true;
//	}

	private void MoveCube ()
	{
		if (flipping) {
			return;
		}

//		if (!CubePositionChanged()) {
//			return;
//		}

		cube_.transform.position = Map2World (cur_cube_pos_.row, cur_cube_pos_.col, tile_width_ / 2);
		cube_.transform.rotation = plane_.transform.rotation;

		// cur_cube_row_ = cur_player_row_;
		// cur_cube_col_ = cur_player_col_;
	}

	private IEnumerator FlipToPos (Position delta_pos)
	{
		int target_row = cur_cube_pos_.row + delta_pos.row;
		int target_col = cur_cube_pos_.col + delta_pos.col;
		int delta_row = delta_pos.row < 0 ? -1 : 1;
		int delta_col = delta_pos.col < 0 ? -1 : 1;

		Debug.Log (string.Format("delta pos: ({0}, {1}), row={2}, col={3}",
		                         delta_pos.row, delta_pos.col, delta_row, delta_col));
		// for (cur_cube_row_ += delta_row; cur_cube_row_ != target_row + delta_row; cur_cube_row_ += delta_row) {
		while (cur_cube_pos_.row != target_row) {
			cur_cube_pos_.row += delta_row;
			yield return StartCoroutine(AnimationCoroutineByEdge (Map2World (cur_cube_pos_.row, cur_cube_pos_.col, 0.0f)));
		}

		// for (cur_cube_col_ += delta_col; cur_cube_col_ != target_col + delta_col; cur_cube_col_ += delta_col) {
		while (cur_cube_pos_.row != target_col) {
			cur_cube_pos_.col += delta_col;
			yield return StartCoroutine(AnimationCoroutineByEdge (Map2World (cur_cube_pos_.row, cur_cube_pos_.col, 0.0f)));
		}


		// StartCoroutine (AnimationCoroutineByEdge (Map2World (cur_cube_row_, cur_cube_col_, 0.0f)));
	}

	/**
	 *
	 * logic_pos = (col*width, height, row*height);
	 * real_pos = TransformPoint(logic_pos + (plane_offset + cube_offset));
	 * 
	 */
	private Vector3 Map2World (int row, int col, float height)
	{
		float x = col * tile_width_;
		float y = height;
		float z = row * tile_width_;
		return plane_.transform.TransformPoint (
			new Vector3 (x, y, z) + plane_offset_ + cube_offset_);
			// new Vector3 (x, y, z) + new Vector3(-5.0f+0.5f, 0.0f, -4.3f+0.5f));
			// new Vector3(0, 0, 0));
	}

	/**
	 * logic_pos = real_pos - (plane_offset + cube_offset)
	 * (row, col) = (logic_pos.z/height, logic_pos.x/width)
	 * 
	 * FXME: there may be some calculation error
	 */
	private Position World2Map (Vector3 world_pos)
	{
		Vector3 logic_pos =
			plane_.transform.InverseTransformPoint(world_pos - plane_offset_ - cube_offset_);
		int row = (int)Mathf.Round (logic_pos.z / tile_width_);
		int col = (int)Mathf.Round (logic_pos.x / tile_width_);
		return new Position (row, col);
	}

	private float begin_time_ = 0.0f;
	private void DoAnimation ()
	{
		float period = 1.0f;

		float passed_time = Time.time - begin_time_;
		// Debug.Log (passed_time);

		if (passed_time > period) {
			Debug.Log ("needless");
			return;
		}

		Quaternion rot = Quaternion.Euler (0, 0, 45.0f);
		// 0.0 ~ 1.0
		rot = Quaternion.Slerp (cube_.transform.rotation, rot, passed_time / period);
		cube_.transform.rotation = rot;
	}

	private IEnumerator AnimationCoroutine ()
	{
		float period = 1.0f;

		Quaternion target_rot = cube_.transform.rotation * Quaternion.Euler (0, 0, 45.0f);
		float begin = Time.time;
		for (float passed = 0; passed <= period; passed = Time.time - begin) {
			// 0.0 ~ 1.0
			Quaternion rot = Quaternion.Slerp (cube_.transform.rotation, target_rot, passed / period);
			cube_.transform.rotation = rot;
			// yield return new WaitForSeconds(0.00f);
			yield return new WaitForSeconds (0.05f);
		}

		// TODO: a fix is needed (in case of error in numerical calculation)
	}

	private IEnumerator AnimationCoroutineRotate ()
	{
		float period = 1.0f;

		float begin = Time.time;
		for (float passed = 0; passed <= period; passed = Time.time - begin) {
			// 0.0 ~ 1.0
			// Quaternion rot = Quaternion.Slerp(cube_.transform.rotation, target_rot, passed / period);
			// cube_.transform.RotateAround(info.center, info.axis, 5.0f);
			cube_.transform.Rotate (new Vector3 (0, 0, 5.0f));
			yield return new WaitForSeconds (0.05f);
		}
		
		// TODO: a fix is needed (in case of error in numerical calculation)
	}

	/**
	 * rotate around the specific edge
	 */
	private bool flipping = false;

	private IEnumerator AnimationCoroutineByEdge (Vector3 target)
	{
		flipping = true;

		EdgeInfo info = GetNearestEdge (
			cube_.transform,
			// cube_.transform.TransformPoint(new Vector3(0.5f, 0.0f, 0.5f)));
			// cube_.transform.position + new Vector3 (-1.0f, 0.0f, 0.5f));
			target);

		// Debug.Log (cube_.transform.rotation + ", " + cube_.transform.rotation.eulerAngles + ", " + info.axis);

		float period = .5f;
		float delta = 90.0f / (period / 0.05f);
		for (float rotated = 0.0f; rotated < 90.0f; rotated += delta) {
			// 0.0 ~ 1.0
			// Quaternion rot = Quaternion.Slerp(cube_.transform.rotation, target_rot, passed / period);
			// FIXME: this won't be correct if cube is moving
			//        because neither the info.center nor the info.axis will be correct if cube's position has been changed.
			cube_.transform.RotateAround (info.center, info.axis, delta);
			yield return new WaitForSeconds (0.05f);
		}

		// FIXME
//		Position cube_pos = World2Map(cube_.transform.position);
//		cur_cube_row_ = cube_pos.row;
//		cur_cube_col_ = cube_pos.col;

		flipping = false;
	}
	
	private struct EdgeInfo
	{
		public Vector3 center;
		public Vector3 axis;

		public EdgeInfo (Vector3 center, Vector3 axis)
		{
			this.center = center;
			this.axis = axis;
		}
	}

	/**
	 *           2
	 *      +----------+
	 *    1/|       4 /|
	 *    / | 3      / |
	 *   +-9--------+10|
	 *   |  |    6  |  |
	 * 11|  +-----12|--+
	 *   |5/        |7/
	 *   |/     8   |/
	 *   +----------+
	 * 
	 */
	private readonly Vector3[] EDGE_CENTER_LIST = {
		new Vector3 (-1, 1, 0), new Vector3 (0, 1, 1), new Vector3 (0, 1, -1), new Vector3 (1, 1, 0),
		new Vector3 (-1, -1, 0), new Vector3 (0, -1, 1), new Vector3 (0, -1, -1), new Vector3 (1, -1, 0),
		new Vector3 (-1, 0, 1), new Vector3 (1, 0, 1), new Vector3 (-1, 0, -1), new Vector3 (1, 0, -1),
	};
	private readonly Vector3[] EDGE_AXIS_LIST = {
		new Vector3 (0, 0, 1), new Vector3 (1, 0, 0), new Vector3 (1, 0, 0), new Vector3 (0, 0, 1),
		new Vector3 (0, 0, 1), new Vector3 (1, 0, 0), new Vector3 (1, 0, 0), new Vector3 (0, 0, 1),
		new Vector3 (0, 1, 0), new Vector3 (0, 1, 0), new Vector3 (0, 1, 0), new Vector3 (0, 1, 0),
	};

	private Vector3[] GetAllEdgeCenter (Transform trans)
	{
		int size = EDGE_CENTER_LIST.GetLength (0);
		Vector3[] l = new Vector3[size];

		for (int i = 0; i < EDGE_CENTER_LIST.GetLength(0); ++i) {
			Vector3 p = EDGE_CENTER_LIST [i];
			l [i] = trans.TransformPoint (p * 0.5f);
		}

		return l;
	}

	private EdgeInfo GetNearestEdge (Transform trans, Vector3 point)
	{
		Vector3[] edge_center_list = GetAllEdgeCenter (trans);

		int idx = 0;
		float shortest = float.PositiveInfinity;
		// foreach (Vector3 p in edge_center_list) {
		for (int i = 0; i < edge_center_list.GetLength(0); ++i) {
			float distance = Vector3.Distance (point, edge_center_list [i]);
			if (distance < shortest) {
				shortest = distance;
				idx = i;
			}
		}

		return GetEdge (trans, idx, point);
	}
	
	private EdgeInfo GetEdge (Transform trans, int edge, Vector3 target)
	{
		float width = 1.0f;

		Vector3 pos = EDGE_CENTER_LIST [edge] * (width / 2);

		// TODO: why doesn't this work?
		// pos = trans.localToWorldMatrix * pos;
		// take care of scale
		pos = trans.TransformPoint (pos);

		// Debug.Log ("orig-axis: " + EDGE_AXIS_LIST [edge]);
		Vector3 axis = trans.TransformVector (EDGE_AXIS_LIST [edge]);
		// Vector3 axis = trans.TransformDirection(EDGE_AXIS_LIST[edge]);

		/**
		 *            
		 *      +----------+
		 *     /|         /|
		 *    / |        / |
		 *   +----------+  |
		 *   |  | O +   |  |
		 *   |  +-------|--+ B
		 *   | /        | /A      + D
		 *   |/         |/
		 *   +----------+ C
		 * 
		 * 
		 * 
		 */
		// V(OA)
		Vector3 v1 = pos - trans.position;
		// V(OD)
		Vector3 v2 = target - trans.position;
		// get the right direction first
		Vector3 dir = Vector3.Cross (v2, v1);
		// then adjust target axis
		if (Vector3.Dot (dir, axis) < 0) {
			// Debug.Log ("reverse is needed");
			axis = -axis;
		}

		return new EdgeInfo (pos, axis);
	}

	private IEnumerator CubeAnimationCoroutine ()
	{
		yield return new WaitForSeconds (0.05f);
	}

	void OnDrawGizmos ()
	{
		// DrawEdgeGizmos();
		// DrawPlaneGizmos();
	}

	private void DrawPlaneGizmos() {
		Gizmos.color = Color.red;
		Vector3 pos = Map2World(0, 0, 0);
		Gizmos.DrawSphere(pos, 0.05f);
	}

	private void DrawEdgeGizmos() {
		//		Vector3 mouse_pos = Input.mousePosition;
		//		mouse_pos = Camera.main.ScreenToWorldPoint (new Vector3(mouse_pos.x, mouse_pos.y, Camera.main.nearClipPlane));
		Gizmos.color = Color.yellow;
		//		Gizmos.DrawSphere(new Vector3(0.0f, 0.0f, 0.0f), 0.1f);
		Gizmos.DrawSphere (cube_.transform.position + new Vector3 (-1.0f, 0.0f, 0.5f), 0.05f);
		
		Gizmos.color = Color.red;
		EdgeInfo info = GetNearestEdge (
			cube_.transform,
			cube_.transform.position + new Vector3 (-1.0f, 0.0f, 0.5f));
		Gizmos.DrawSphere (info.center, 0.05f);
	}

	void OnGUI ()
	{
		float begin_x = 10;
		float begin_y = 10;
		float row_height = 20;
		float row_width = 1000;
		
//		// mouse position
//		Vector3 mouse_pos = Input.mousePosition;
//		mouse_pos = Camera.main.ScreenToWorldPoint (new Vector3 (mouse_pos.x, mouse_pos.y, Camera.main.nearClipPlane));
//		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
//		           string.Format ("mouse=({0,5:F}, {1,5:F}, {2,5:F})",
//		               mouse_pos.x, mouse_pos.y, mouse_pos.z));
//		begin_y += row_height;

		// cube position
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("cube=({0,5:F}, {1,5:F}, {2,5:F})",
		               cube_.transform.position.x, cube_.transform.position.y, cube_.transform.position.z));
		begin_y += row_height;

		// cube in map
		Position pos = World2Map(cube_.transform.position);
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("map pos=({0}, {1})", pos.row, pos.col));
		begin_y += row_height;
		
	}
}
