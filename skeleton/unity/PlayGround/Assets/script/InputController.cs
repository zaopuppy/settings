using UnityEngine;
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
		Quaternion target_rot = cube_.transform.rotation * Quaternion.Euler(0, 0, 90.0f);

		EdgeInfo info = GetEdge(cube_.transform);
		float period = .5f;
		// for (float passed = 0; passed <= period; passed = Time.time - begin) {
		float delta = 90.0f/(period/0.05f);
		for (float rotated = 0.0f; rotated < 90.0f; rotated += delta) {
			// 0.0 ~ 1.0
			// Quaternion rot = Quaternion.Slerp(cube_.transform.rotation, target_rot, passed / period);
			cube_.transform.RotateAround(info.center, info.axis, delta);
			yield return new WaitForSeconds(0.05f);
		}

		// TODO: make sure the cube is rotated correctly
		cube_.transform.rotation = target_rot;
	}
	
	private struct EdgeInfo {
		public Vector3 center;
		public Vector3 axis;

		public EdgeInfo(Vector3 center, Vector3 axis) {
			this.center = center;
			this.axis = axis;
		}
	}
	
	private EdgeInfo GetEdge(Transform trans) {
		float width = 1.0f;
		float height = 1.0f;

		Vector3 pos = new Vector3(width/2, -height/2, 0);
		// TODO: why doesn't this work?
		// pos = trans.localToWorldMatrix * pos;
		// take care of scale
		pos = trans.TransformPoint(pos);

		Vector3 axis = new Vector3(0, 0, 1);

		return new EdgeInfo(pos, axis);
	}

	private IEnumerator CubeAnimationCoroutine() {
		yield return new WaitForSeconds(0.05f);
	}

	void OnGUI ()
	{
		float begin_x = 10;
		float begin_y = 10;
		float row_height = 20;
		float row_width = 1000;
		
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("cube=({0,5:F}, {1,5:F}, {2,5:F})",
		               cube_.transform.position.x, cube_.transform.position.y, cube_.transform.position.z));
		begin_y += row_height;
		// mouse position
		// Vector3 mouse_pos = Camera.main.ScreenToWorldPoint (Input.mousePosition);
		EdgeInfo info = GetEdge(cube_.transform);
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("edge=({0,5:F}, {1,5:F}, {2,5:F})",
		               info.center.x, info.center.y, info.center.z));
		begin_y += row_height;
		
	}
}
