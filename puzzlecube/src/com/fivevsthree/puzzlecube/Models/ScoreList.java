package com.fivevsthree.puzzlecube.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.fivevsthree.puzzlecube.Models.Puzzle.RotationAnimation;

public class ScoreList implements Json.Serializable {

	private class ScoreComparator implements Comparator<Score> {

		@Override
		public int compare(Score o1, Score o2) {
			return (int) (o1.seconds - o2.seconds)
					+ (int) (o1.moves - o2.moves);
		}

	}

	private class ScoreSecondsComparator implements Comparator<Score> {

		@Override
		public int compare(Score o1, Score o2) {
			return (int) (o1.seconds - o2.seconds);
		}

	}

	private class ScoreMovesComparator implements Comparator<Score> {

		@Override
		public int compare(Score o1, Score o2) {
			return (int) (o1.moves - o2.moves);
		}

	}

	private ArrayList<Score> scoreList;

	public Score[] getScores() {
		Collections.sort(scoreList, new ScoreComparator());
		return scoreList.toArray(new Score[scoreList.size()]);
	}

	public ScoreList() {
		scoreList = new ArrayList<Score>();
	}

	public void addScore(long seconds, long moves, String date,
			RotationAnimation animation) {
		Score score = new Score();
		score.seconds = seconds;
		score.moves = moves;
		score.date = date;
		score.animation = animation;

		try {
			scoreList.add(score);
		} catch (Exception e) {
		}
	}

	public Score getBestSeconds() {
		if (!scoreList.isEmpty()) {
			Collections.sort(scoreList, new ScoreSecondsComparator());
			return scoreList.get(0);
		}

		return new Score();
	}

	public Score getBestMoves() {
		if (!scoreList.isEmpty()) {
			Collections.sort(scoreList, new ScoreMovesComparator());
			return scoreList.get(0);
		}

		return new Score();
	}

	public void clear() {
		scoreList.clear();
		scoreList.trimToSize();
	}

	@Override
	public void write(Json json) {
		json.writeValue("Scores", scoreList, ArrayList.class, Score.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonData) {
		ArrayList<Score> loadedScores = json.readValue("Scores",
				ArrayList.class, Score.class, jsonData);

		try {
			for (Score score : loadedScores) {
				scoreList.add(score);
			}
		} catch (Exception e) {
		}
	}
}
