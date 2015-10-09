using UnityEngine;
using System.Collections;

[RequireComponent(typeof(GameObject))]
public class Menu : MonoBehaviour
{

	public GameObject title_block_;

	// Use this for initialization
	void Start ()
	{
	}
	
	// Update is called once per frame
	void Update ()
	{
		if (Input.GetMouseButtonDown(0)) {
			Debug.Log (Input.mousePosition);
			Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
			RaycastHit hit;
			int layer_mask = 1 << 8;
			if (Physics.Raycast(ray, out hit, Mathf.Infinity, layer_mask)) {
				//
				Debug.Log ("hit!");
				Application.LoadLevel(1);
				return;
			}
		}

		title_block_.transform.Rotate(
			(Vector3.right+Vector3.up+Vector3.forward)*50*Time.deltaTime, Space.Self);
	}

	void OnGUI ()
	{
		float begin_x = 10;
		float begin_y = 10;
		float row_height = 20;
		float row_width = 1000;
		
		// mouse position
		// Vector3 mouse_pos = Camera.main.ScreenToWorldPoint (Input.mousePosition);
		Vector3 mouse_pos = Input.mousePosition;
		GUI.Label (new Rect (begin_x, begin_y, row_width, row_height),
		           string.Format ("mouse=({0,5:F}, {1,5:F}, {2,5:F})",
		               mouse_pos.x, mouse_pos.y, mouse_pos.z));
		begin_y += row_height;
		

	}

}
