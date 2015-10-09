using UnityEngine;
using System.Collections;

public class Timer {
	private long passed_time_ = 0;
	private long interval_;
	private bool time_up_ = false;
	// private ITask task_;


	public interface ITask {
		void run();
	}

	public Timer(long interval/*, ITask task*/) {
		this.interval_ = interval;
		// task_ = task;
	}

	public void update(long delta) {
		passed_time_ += delta;
		if (passed_time_ >= interval_) {
			time_up_ = true;
			passed_time_ = passed_time_ % interval_;
			// task_.run();
		} else {
			time_up_ = false;
		}
	}

	public bool isTimeUp() {
		return time_up_;
	}
}
