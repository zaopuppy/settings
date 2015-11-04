﻿using UnityEngine;
using System.Collections;

[RequireComponent(typeof(GameObject))]
public class InputController : MonoBehaviour {

	public GameObject cube_;

	// Use this for initialization
	void Start () {
	}
	
	// Update is called once per frame
	void Update () {
		if (Input.GetKeyDown(KeyCode.Space)) {
			Debug.Log ("trigger");
			// begin_time_ = Time.time;
			// StartCoroutine(AnimationCoroutineRotate());
			// StartCoroutine(AnimationCoroutine());
			StartCoroutine(AnimationCoroutineByEdge());
		}
		// DoAnimation();
	}

	private float begin_time_ = 0.0f;
 	private void DoAnimation() {
		float period = 1.0f;

		float passed_time = Time.time - begin_time_;
		// Debug.Log (passed_time);

		if (passed_time > period) {
			Debug.Log ("needless");
			return;
		}

		Quaternion rot = Quaternion.Euler(0, 0, 45.0f);
		// 0.0 ~ 1.0
		rot = Quaternion.Slerp(cube_.transform.rotation, rot, passed_time / period);
		cube_.transform.rotation = rot;
	}

	private IEnumerator AnimationCoroutine() {
		float period = 1.0f;

		Quaternion target_rot = cube_.transform.rotation * Quaternion.Euler(0, 0, 45.0f);
		float begin = Time.time;
		for (float passed = 0; passed <= period; passed = Time.time - begin) {
			// 0.0 ~ 1.0
			Quaternion rot = Quaternion.Slerp(cube_.transform.rotation, target_rot, passed / period);
			cube_.transform.rotation = rot;
			// yield return new WaitForSeconds(0.00f);
			yield return new WaitForSeconds(0.05f);
		}

		// TODO: a fix is needed (in case of error in numerical calculation)
	}

	private IEnumerator AnimationCoroutineRotate() {
		float period = 1.0f;

		float begin = Time.time;
		for (float passed = 0; passed <= period; passed = Time.time - begin) {
			// 0.0 ~ 1.0
			// Quaternion rot = Quaternion.Slerp(cube_.transform.rotation, target_rot, passed / period);
			// cube_.transform.RotateAround(info.center, info.axis, 5.0f);
			cube_.transform.Rotate(new Vector3(0, 0, 5.0f));
			yield return new WaitForSeconds(0.05f);
		}
		
		// TODO: a fix is needed (in case of error in numerical calculation)
	}

	/**
	 * rotate around the specific edge
	 */
	private IEnumerator AnimationCoroutineByEdge() {
		// Quaternion target_rot = cube_.transform.rotation * Quaternion.Euler(0, 0, 90.0f);
//		Quaternion orig_rotation = cube_.transform.rotation;

		// EdgeInfo info = GetEdge(cube_.transform, 0);
		EdgeInfo info = GetNearestEdge(
			cube_.transform,
			// cube_.transform.TransformPoint(new Vector3(0.5f, 0.0f, 0.5f)));
			cube_.transform.position + new Vector3(-1.0f, 0.0f, 0.5f));

		Debug.Log (cube_.transform.rotation + ", " + cube_.transform.rotation.eulerAngles + ", " + info.axis);

		float period = .5f;
		// for (float passed = 0; passed <= period; passed = Time.time - begin) {
		float delta = 90.0f/(period/0.05f);
		for (float rotated = 0.0f; rotated < 90.0f; rotated += delta) {
			// 0.0 ~ 1.0
			// Quaternion rot = Quaternion.Slerp(cube_.transform.rotation, target_rot, passed / period);
			cube_.transform.RotateAround(info.center, info.axis, delta);
			yield return new WaitForSeconds(0.05f);
		}

		// to make sure we don't get calculation error
//		cube_.transform.rotation = orig_rotation;
//		cube_.transform.RotateAround(info.center, info.axis, 90.0f);
	}
	
	private struct EdgeInfo {
		public Vector3 center;
		public Vector3 axis;

		public EdgeInfo(Vector3 center, Vector3 axis) {
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
		new Vector3(-1,  1, 0), new Vector3(0,  1, 1), new Vector3( 0,  1, -1), new Vector3(1,  1,  0),
		new Vector3(-1, -1, 0), new Vector3(0, -1, 1), new Vector3( 0, -1, -1), new Vector3(1, -1,  0),
		new Vector3(-1,  0, 1), new Vector3(1,  0, 1), new Vector3(-1,  0, -1), new Vector3(1,  0, -1),
	};

	private readonly Vector3[] EDGE_AXIS_LIST = {
		new Vector3(0, 0, 1), new Vector3(1, 0, 0), new Vector3(1, 0, 0), new Vector3(0, 0, 1),
		new Vector3(0, 0, 1), new Vector3(1, 0, 0), new Vector3(1, 0, 0), new Vector3(0, 0, 1),
		new Vector3(0, 1, 0), new Vector3(0, 1, 0), new Vector3(0, 1, 0), new Vector3(0, 1, 0),
	};

	private Vector3[] GetAllEdgeCenter(Transform trans) {
		int size = EDGE_CENTER_LIST.GetLength(0);
		Vector3[] l = new Vector3[size];

		for (int i = 0; i < EDGE_CENTER_LIST.GetLength(0); ++i) {
			Vector3 p = EDGE_CENTER_LIST[i];
			l[i] = trans.TransformPoint(p*0.5f);
		}

		return l;
	}

	private EdgeInfo GetNearestEdge(Transform trans, Vector3 point) {
		Vector3[] edge_center_list = GetAllEdgeCenter(trans);

		int idx = 0;
		float shortest = float.PositiveInfinity;
		// foreach (Vector3 p in edge_center_list) {
		for (int i = 0; i < edge_center_list.GetLength(0); ++i) {
			float distance = Vector3.Distance(point, edge_center_list[i]);
			if (distance < shortest) {
				shortest = distance;
				idx = i;
			}
		}

		return GetEdge(trans, idx, point);
	}
	
	private EdgeInfo GetEdge(Transform trans, int edge, Vector3 target) {
		float width = 1.0f;

		Vector3 pos = EDGE_CENTER_LIST[edge]*(width/2);

		// TODO: why doesn't this work?
		// pos = trans.localToWorldMatrix * pos;
		// take care of scale
		pos = trans.TransformPoint(pos);

		Debug.Log ("orig-axis: " + EDGE_AXIS_LIST[edge]);
		Vector3 axis = trans.TransformVector(EDGE_AXIS_LIST[edge]);
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
		Vector3 dir = Vector3.Cross(v2, v1);
		// then adjust target axis
		if (Vector3.Dot(dir, axis) < 0) {
			// Debug.Log ("reverse is needed");
			axis = -axis;
		}

		return new EdgeInfo(pos, axis);
	}

	private IEnumerator CubeAnimationCoroutine() {
		yield return new WaitForSeconds(0.05f);
	}

	void OnDrawGizmos() {
//		Vector3 mouse_pos = Input.mousePosition;
//		mouse_pos = Camera.main.ScreenToWorldPoint (new Vector3(mouse_pos.x, mouse_pos.y, Camera.main.nearClipPlane));
		Gizmos.color = Color.yellow;
//		Gizmos.DrawSphere(new Vector3(0.0f, 0.0f, 0.0f), 0.1f);
		Gizmos.DrawSphere(cube_.transform.position + new Vector3(-1.0f, 0.0f, 0.5f), 0.05f);

		Gizmos.color = Color.red;
		EdgeInfo info = GetNearestEdge(
			cube_.transform,
			cube_.transform.position + new Vector3(-1.0f, 0.0f, 0.5f));
		Gizmos.DrawSphere(info.center, 0.05f);
	}

	void OnGUI ()
	{
//		float begin_x = 10;
//		float begin_y = 10;
//		float row_height = 20;
//		float row_width = 1000;
//		
//		// mouse position
//		Vector3 mouse_pos = Input.mousePosition;
//		mouse_pos = Camera.main.ScreenToWorldPoint (new Vector3(mouse_pos.x, mouse_pos.y, Camera.main.nearClipPlane));
//		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
//		           string.Format ("mouse=({0,5:F}, {1,5:F}, {2,5:F})",
//		               mouse_pos.x, mouse_pos.y, mouse_pos.z));
//		begin_y += row_height;
//
//		// cube position
//		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
//		           string.Format ("cube=({0,5:F}, {1,5:F}, {2,5:F})",
//		               cube_.transform.position.x, cube_.transform.position.y, cube_.transform.position.z));
//		begin_y += row_height;
//
//		// edge info
//		EdgeInfo info = GetEdge(cube_.transform, 0);
//		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
//		           string.Format ("edge=({0,5:F}, {1,5:F}, {2,5:F})",
//		               info.center.x, info.center.y, info.center.z));
//		begin_y += row_height;
		
	}
}
