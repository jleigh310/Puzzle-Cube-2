package com.fivevsthree.puzzlecube.Callbacks;

public interface PuzzleCallback {
	/**
	 * Called when an animation rotation has started
	 */
	public void rotationStarted();

	/**
	 * Called when an animation rotation has completed
	 */
	public void rotationComplete(boolean solved);

	/**
	 * Called when the timer has changed
	 * 
	 * @param seconds
	 *            current time in seconds
	 */
	public void timerChanged(long seconds);

	/**
	 * Called when the move counter has changed
	 * 
	 * @param counter
	 *            current mvoe count
	 */
	public void moveCounterChanged(long counter);
}
