package com.fivevsthree.puzzlecube.Models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Models.Puzzle.RotationAnimation;

public class Score {

	public long seconds;
	public long moves;
	public String date;
	public RotationAnimation animation;

	public Date getDate() {
		DateFormat savedFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		try {
			return savedFormat.parse(date);
		} catch (ParseException e) {
			return new Date();
		}
	}

	public String getDateString() {
		DateFormat savedFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		try {
			return PuzzleCube.getDateFormat().format(savedFormat.parse(date));
		} catch (ParseException e) {
			return "";
		}
	}

	public String getTimeString() {
		// Calculate hours
		long h = seconds / 3600;

		// Calculate minutes
		long m = (seconds / 60) - (h * 60);

		// Calculate seconds
		long s = seconds - (m * 60) - (h * 3600);

		return String.format("%d:%02d:%02d", h, m, s);
	}

	public String getMovesString() {
		return String.valueOf(moves);
	}

}
